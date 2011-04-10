package com.sk89q.craftbook.mech;

// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.Mechanic;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.craftbook.util.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;

/**
 * Handler for cauldrons.
 * 
 * @author sk89q
 */
public class Cauldron extends Mechanic {
    public static class Factory extends AbstractMechanicFactory<Cauldron> {

        protected MechanismsPlugin plugin;
        protected CauldronCookbook recipes;

        public Factory(MechanismsPlugin plugin) {
            this.plugin = plugin;
            this.recipes = new CauldronCookbook();
        }

        @Override
        public Cauldron detect(BlockWorldVector pt) {
            Block block = pt.toBlock();
            // check if this looks at all like something we're interested in
            // first
            if (block.getTypeId() == BlockID.AIR)
                return null;
            return new Cauldron(this.recipes, pt, plugin);
        }
    }

    /**
     * Stores the recipes.
     */
    private CauldronCookbook recipes;
    private BlockWorldVector pt;
    private MechanismsPlugin plugin;

    /**
     * Construct the handler.
     * 
     * @param recipes
     * @param pt
     * @param plugin
     */
    public Cauldron(CauldronCookbook recipes, BlockWorldVector pt,
            MechanismsPlugin plugin) {
        super();
        this.recipes = recipes;
        this.pt = pt;
        this.plugin = plugin;
    }

    @Override
    public void onRightClick(PlayerInteractEvent event) {
        LocalPlayer localPlayer = plugin.wrap(event.getPlayer());
        
        if (!plugin.getLocalConfiguration().cauldronSettings.enable)
            return;
        
        if (!localPlayer.hasPermission("craftbook.mech.cauldron")) {
            localPlayer.printError("You don't have permission to use this.");
            return;
        }
        
        if (!BukkitUtil.toWorldVector(event.getClickedBlock()).equals(pt))
            return; // wth? our manager is insane
        if (event.getPlayer().getItemInHand().getTypeId() >= 255
                || event.getPlayer().getItemInHand().getType() == Material.AIR)
            preCauldron(event.getPlayer(), event.getPlayer().getWorld(), pt);
    }

    /**
     * Thrown when a suspected formation is not actually a valid cauldron.
     */
    private class NotACauldronException extends Exception {
        private static final long serialVersionUID = 3091428924893050849L;

        /**
         * Construct the exception with a message.
         * 
         * @param msg
         */
        public NotACauldronException(String msg) {
            super(msg);
        }
    }

    /**
     * Do cauldron.
     * 
     * @param pt
     * @param player
     * @param world
     */
    public void preCauldron(Player player, World world, BlockWorldVector pt) {
        double x = pt.getX();
        double y = pt.getY();
        double z = pt.getZ();

        int ix = pt.getBlockX();
        int iy = pt.getBlockY();
        int iz = pt.getBlockZ();

        double rootY = y;
        int below = world.getBlockTypeIdAt(ix, iy - 1, iz);
        int below2 = world.getBlockTypeIdAt(ix, iy - 2, iz);
        int s1 = world.getBlockTypeIdAt(ix + 1, iy, iz);
        int s3 = world.getBlockTypeIdAt(ix - 1, iy, iz);
        int s2 = world.getBlockTypeIdAt(ix, iy, iz + 1);
        int s4 = world.getBlockTypeIdAt(ix, iy, iz - 1);
        // stop strange lava ids
        if (below == 11)
            below = 10;
        if (below2 == 11)
            below2 = 10;
        // Preliminary check so we don't waste CPU cycles
        if ((below == BlockID.LAVA || below2 == BlockID.LAVA)
                && (s1 == BlockID.STONE || s2 == BlockID.STONE
                        || s3 == BlockID.STONE || s4 == BlockID.STONE)) {
            // Cauldron is 2 units deep
            if (below == BlockID.LAVA) {
                rootY++;
            }
            performCauldron(player, world, new BlockWorldVector(pt.getWorld(),
                    x, rootY, z));
        }
    }

    /**
     * Attempt to perform a cauldron recipe.
     * 
     * @param pt
     * @param player
     * @param recipes
     */
    private void performCauldron(Player player, World world, BlockWorldVector pt) {
        // Gotta start at a root Y then find our orientation
        int rootY = pt.getBlockY();

        // Used to store cauldron blocks -- walls are counted
        Map<BlockWorldVector, Integer> visited = new HashMap<BlockWorldVector, Integer>();

        try {
            // The following attempts to recursively find adjacent blocks so
            // that it can find all the blocks used within the cauldron
            findCauldronContents(world, pt, rootY - 1, rootY, visited);

            // We want cauldrons of a specific shape and size, and 24 is just
            // the right number of blocks that the cauldron we want takes up --
            // nice and cheap check
            if (visited.size() != 24) {
                throw new NotACauldronException("Cauldron is too small");
            }

            // Key is the block ID and the value is the amount
            Map<Integer, Integer> contents = new HashMap<Integer, Integer>();

            // Now we have to ignore stone blocks so that we get the real
            // contents of the cauldron
            for (Map.Entry<BlockWorldVector, Integer> entry : visited
                    .entrySet()) {
                if (entry.getValue() != BlockID.STONE) {
                    if (!contents.containsKey(entry.getValue())) {
                        contents.put(entry.getValue(), 1);
                    } else {
                        contents.put(entry.getValue(),
                                contents.get(entry.getValue()) + 1);
                    }
                }
            }

            // Find the recipe
            CauldronCookbook.Recipe recipe = recipes.find(contents);

            if (recipe != null) {

                String[] groups = recipe.getGroups();

                if (groups != null) {
                    boolean found = false;

                    for (String group : groups) {
                        found = true;
                        break;
                        // TODO: Add an isInGroup method
                        /*
                         * if (player.isInGroup(group)) { found = true; break; }
                         */
                    }

                    if (!found) {
                        player.sendMessage(ChatColor.DARK_RED
                                + "Doesn't seem as if you have the ability...");
                        return;
                    }
                }

                player.sendMessage(ChatColor.GOLD
                        + "In a poof of smoke, you've made " + recipe.getName()
                        + ".");

                List<Integer> ingredients = new ArrayList<Integer>(
                        recipe.getIngredients());

                List<BlockWorldVector> removeQueue = new ArrayList<BlockWorldVector>();

                // Get rid of the blocks in world
                for (Map.Entry<BlockWorldVector, Integer> entry : visited
                        .entrySet()) {
                    // This is not a fast operation, but we should not have
                    // too many ingredients
                    if (ingredients.contains(entry.getValue())) {
                        // Some blocks need to removed first otherwise they will
                        // drop an item, so let's remove those first
                        // if
                        // (!BlockID.isBottomDependentBlock(entry.getValue())) {
                        // removeQueue.add(entry.getKey());
                        // } else {
                        world.getBlockAt(entry.getKey().getBlockX(),
                                entry.getKey().getBlockY(),
                                entry.getKey().getBlockZ()).setType(
                                Material.AIR);
                        // }
                        ingredients.remove(entry.getValue());
                    }
                }

                for (BlockWorldVector v : removeQueue) {
                    world.getBlockAt(v.getBlockX(), v.getBlockY(),
                            v.getBlockZ()).setType(Material.AIR);
                }

                // Give results
                for (Integer id : recipe.getResults()) {
                    HashMap<Integer, ItemStack> map = player.getInventory()
                            .addItem(new ItemStack(id, 1));
                    for (Entry<Integer, ItemStack> i : map.entrySet()) {
                        world.dropItem(player.getLocation(), i.getValue());
                    }
                }
                player.updateInventory();
                // Didn't find a recipe
            } else {
                player.sendMessage(ChatColor.RED
                        + "Hmm, this doesn't make anything...");
            }
        } catch (NotACauldronException e) {
        }
    }

    /**
     * Recursively expand the search area so we can define the number of blocks
     * that are in the cauldron. The search will not exceed 24 blocks as no pot
     * will ever use up that many blocks. The Y are bounded both directions so
     * we don't ever search the lava or anything above, although in the case of
     * non-wall blocks, we also make sure that there is standing lava
     * underneath.
     * 
     * @param world 
     * @param pt
     * @param minY
     * @param maxY
     * @param visited
     * @throws Cauldron.NotACauldronException
     */
    public void findCauldronContents(World world, BlockWorldVector pt,
            int minY, int maxY, Map<BlockWorldVector, Integer> visited)
            throws NotACauldronException {

        // Don't want to go too low or high
        if (pt.getBlockY() < minY) {
            return;
        }
        if (pt.getBlockY() > maxY) {
            return;
        }

        // There is likely a leak in the cauldron (or this isn't a cauldron)
        if (visited.size() > 24) {
            throw new NotACauldronException("Cauldron has a leak");
        }

        // Prevent infinite looping
        if (visited.containsKey(pt)) {
            return;
        }

        int type = world.getBlockTypeIdAt(pt.getBlockX(), pt.getBlockY(),
                pt.getBlockZ());

        // Make water work reliably
        if (type == 9) {
            type = 8;
        }

        // Make lava work reliably
        if (type == 11) {
            type = 10;
        }

        visited.put(pt, type);

        // It's a wall -- we only needed to remember that we visited it but
        // we don't need to recurse
        if (type == BlockID.STONE) {
            return;
        }

        // Must have a lava floor
        BlockWorldVector lavaPos = recurse(0, pt.getBlockY() - minY + 1, 0, pt);
        if ((world.getBlockTypeIdAt(lavaPos.getBlockX(), lavaPos.getBlockY(),
                lavaPos.getBlockZ())) == BlockID.LAVA) {
            throw new NotACauldronException("Cauldron lacks lava below");
        }

        // Now we recurse!
        findCauldronContents(world, recurse(1, 0, 0, pt), minY, maxY, visited);
        findCauldronContents(world, recurse(-1, 0, 0, pt), minY, maxY, visited);
        findCauldronContents(world, recurse(0, 0, 1, pt), minY, maxY, visited);
        findCauldronContents(world, recurse(0, 0, -1, pt), minY, maxY, visited);
        findCauldronContents(world, recurse(0, 1, 0, pt), minY, maxY, visited);
        findCauldronContents(world, recurse(0, -1, 0, pt), minY, maxY, visited);
    }

    /**
     * Returns a new BlockWorldVector with i, j, and k added to pt's x, y and z.
     * 
     * @param i
     * @param j
     * @param k
     * @param pt
     * 
     */
    private BlockWorldVector recurse(int i, int j, int k, BlockWorldVector pt) {
        return new BlockWorldVector(pt.getWorld(), pt.getX() + i,
                pt.getY() + j, pt.getZ() + k);
    }

    @Override
    public void unload() {

    }

    @Override
    public boolean isActive() {
        return false;
    }
}
