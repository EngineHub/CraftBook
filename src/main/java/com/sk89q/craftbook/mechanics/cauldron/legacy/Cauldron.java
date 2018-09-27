package com.sk89q.craftbook.mechanics.cauldron.legacy;

// $Id$
/*
 * CraftBook Copyright (C) 2010 sk89q <http://www.sk89q.com>
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.BukkitCraftBookPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.BlockSyntax;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ProtectionUtil;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Handler for cauldrons.
 *
 * @author sk89q
 * @deprecated Use {@link com.sk89q.craftbook.mechanics.cauldron.ImprovedCauldron} instead
 */
@Deprecated
public class Cauldron extends AbstractCraftBookMechanic {

    public boolean isACauldron(Block block) {

        Material below = block.getRelative(0, -1, 0).getType();
        Material below2 = block.getRelative(0, -2, 0).getType();
        Block s1 = block.getRelative(1, 0, 0);
        Block s3 = block.getRelative(-1, 0, 0);
        Block s2 = block.getRelative(0, 0, 1);
        Block s4 = block.getRelative(0, 0, -1);

        BlockStateHolder blockItem = cauldronBlock;

        // stop strange lava ids
        // Preliminary check so we don't waste CPU cycles
        return (below == Material.LAVA || below2 == Material.LAVA)
                && (blockItem.equalsFuzzy(BukkitAdapter.adapt(s1.getBlockData()))
                || blockItem.equalsFuzzy(BukkitAdapter.adapt(s2.getBlockData()))
                || blockItem.equalsFuzzy(BukkitAdapter.adapt(s3.getBlockData()))
                || blockItem.equalsFuzzy(BukkitAdapter.adapt(s4.getBlockData())));

    }

    protected CauldronCookbook recipes;

    @Override
    public boolean enable() {
        recipes = new CauldronCookbook();

        return recipes.size() > 0;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(PlayerInteractEvent event) {

        if (!EventUtil.passesFilter(event))
            return;

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND) return;
        if(!isACauldron(event.getClickedBlock())) return;

        CraftBookPlayer localPlayer = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (!localPlayer.hasPermission("craftbook.mech.cauldron")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                localPlayer.printError("mech.use-permission");
            return;
        }

        if(!ProtectionUtil.canUse(event.getPlayer(), event.getClickedBlock().getLocation(), event.getBlockFace(), event.getAction())) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                localPlayer.printError("area.use-permissions");
            return;
        }

        if (!localPlayer.isHoldingBlock()) {
            performCauldron(localPlayer, event.getPlayer().getWorld(), event.getClickedBlock().getRelative(0, event.getClickedBlock().getRelative(0, -1, 0).getType() == Material.LAVA ? 1 : 0, 0));
            event.setCancelled(true);
        }
    }

    /**
     * Attempt to perform a cauldron recipe.
     *
     * @param player
     * @param world
     * @param block
     */
    private void performCauldron(CraftBookPlayer player, World world, Block block) {

        // Gotta start at a root Y then find our orientation
        int rootY = block.getY();

        Player p = ((BukkitCraftBookPlayer)player).getPlayer();

        BlockStateHolder blockItem = cauldronBlock;

        // Used to store cauldron blocks -- walls are counted
        Map<Location, BlockStateHolder> visited = new HashMap<>();

        // The following attempts to recursively find adjacent blocks so
        // that it can find all the blocks used within the cauldron
        findCauldronContents(player, world, block, rootY - 1, rootY, visited);

        // We want cauldrons of a specific shape and size, and 24 is just
        // the right number of blocks that the cauldron we want takes up --
        // nice and cheap check
        if (visited.size() != 24) {
            player.printError("mech.cauldron.too-small");
            return;
        }

        // Key is the block ID and the value is the amount
        Map<BlockStateHolder, Integer> contents = new HashMap<>();

        // Now we have to ignore cauldron blocks so that we get the real
        // contents of the cauldron
        for (Map.Entry<Location, BlockStateHolder> entry : visited.entrySet()) {
            if (!entry.getValue().equals(blockItem))
                if (!contents.containsKey(entry.getValue())) {
                    contents.put(entry.getValue(), 1);
                } else {
                    contents.put(entry.getValue(), contents.get(entry.getValue()) + 1);
                }
        }

        CraftBookPlugin.logDebugMessage("Ingredients: " + contents.keySet().toString(), "legacy-cauldron.ingredients");

        // Find the recipe
        CauldronCookbook.Recipe recipe = recipes.find(contents);

        if (recipe != null) {

            String[] groups = recipe.getGroups();

            if (groups != null) {
                boolean found = false;

                for (String group : groups) {

                    if (CraftBookPlugin.inst().inGroup(p, group)) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    player.printError("mech.cauldron.legacy-not-in-group");
                    return;
                }
            }

            player.print(player.translate("mech.cauldron.legacy-create") + " " + recipe.getName() + ".");

            List<BlockStateHolder> ingredients = new ArrayList<>(recipe.getIngredients());

            //List<BlockWorldVector> removeQueue = new ArrayList<BlockWorldVector>();

            // Get rid of the blocks in world
            for (Map.Entry<Location, BlockStateHolder> entry : visited.entrySet())
                // This is not a fast operation, but we should not have
                // too many ingredients
            {
                if (ingredients.contains(entry.getValue())) {
                    // Some blocks need to removed first otherwise they will
                    // drop an item, so let's remove those first
                    // if
                    // (!BlockID.isBottomDependentBlock(entry.getValue())) {
                    // removeQueue.add(entry.getKey());
                    // } else {
                    world.getBlockAt(entry.getKey().getBlockX(), entry.getKey().getBlockY(),
                            entry.getKey().getBlockZ()).setType(Material.AIR);
                    // }
                    ingredients.remove(entry.getValue());
                }
            }

            /*
                for (BlockWorldVector v : removeQueue) {
                    world.getBlockAt(v.getBlockX(), v.getBlockY(), v.getBlockZ()).setTypeId(BlockID.AIR);
                }
             */

            // Give results
            for (BlockStateHolder id : recipe.getResults()) {
                HashMap<Integer, ItemStack> map = p.getInventory().addItem(new ItemStack(BukkitAdapter.adapt(id.getBlockType().getItemType()), 1));
                for (Entry<Integer, ItemStack> i : map.entrySet()) {
                    world.dropItem(p.getLocation(), i.getValue());
                }
            }
            p.updateInventory();
            // Didn't find a recipe
        } else {
            player.printError("mech.cauldron.legacy-not-a-recipe");
        }
    }

    /**
     * Recursively expand the search area so we can define the number of blocks that are in the cauldron. The search
     * will not exceed 24 blocks as no
     * pot will ever use up that many blocks. The Y are bounded both directions so we don't ever search the lava or
     * anything above, although in the
     * case of non-wall blocks, we also make sure that there is standing lava underneath.
     *
     * @param world
     * @param block
     * @param minY
     * @param maxY
     * @param visited
     */
    public void findCauldronContents(CraftBookPlayer player, World world, Block block, int minY, int maxY, Map<Location, BlockStateHolder> visited) {

        BlockStateHolder blockID = cauldronBlock;

        // Don't want to go too low or high
        if (block.getY() < minY) return;
        if (block.getY() > maxY) return;

        // There is likely a leak in the cauldron (or this isn't a cauldron)
        if (visited.size() > 24) {
            player.printError("mech.cauldron.leaky");
            return;
        }

        // Prevent infinite looping
        if (visited.containsKey(block.getLocation())) return;

        Material type = block.getType();

        visited.put(block.getLocation(), BukkitAdapter.adapt(block.getBlockData()));

        // It's a wall -- we only needed to remember that we visited it but
        // we don't need to recurse
        if (BukkitAdapter.equals(blockID.getBlockType(), type)) return;

        // Must have a lava floor
        Block lavaPos = recurse(0, block.getY() - minY + 1, 0, block);
        if (world.getBlockAt(lavaPos.getX(), lavaPos.getY(), lavaPos.getZ()).getType() == Material.LAVA) {
            player.printError("mech.cauldron.no-lava");
            return;
        }

        // Now we recurse!
        findCauldronContents(player, world, recurse(1, 0, 0, block), minY, maxY, visited);
        findCauldronContents(player, world, recurse(-1, 0, 0, block), minY, maxY, visited);
        findCauldronContents(player, world, recurse(0, 0, 1, block), minY, maxY, visited);
        findCauldronContents(player, world, recurse(0, 0, -1, block), minY, maxY, visited);
        findCauldronContents(player, world, recurse(0, 1, 0, block), minY, maxY, visited);
        findCauldronContents(player, world, recurse(0, -1, 0, block), minY, maxY, visited);
    }

    /**
     * Returns a new BlockWorldVector with i, j, and k added to pt's x, y and z.
     *
     * @param x
     * @param y
     * @param z
     * @param block
     */
    private Block recurse(int x, int y, int z, Block block) {

        return block.getRelative(x, y, z);
    }

    public BlockStateHolder cauldronBlock;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "block", "The block to use as the casing for the legacy cauldron.");
        cauldronBlock = BlockSyntax.getBlock(config.getString(path + "block", BlockTypes.STONE.getId()), true);
    }
}