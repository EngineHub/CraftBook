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

import com.sk89q.craftbook.BlockType;
import com.sk89q.craftbook.Colors;
import com.sk89q.craftbook.access.PlayerInterface;
import com.sk89q.craftbook.access.WorldInterface;
import com.sk89q.craftbook.util.BlockVector;
import com.sk89q.craftbook.util.Vector;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Handler for cauldrons.
 *
 * @author sk89q
 */
public class Cauldron {
    /**
     * Stores the recipes.
     */
    private CauldronCookbook recipes;

    /**
     * Construct the handler.
     * 
     * @param recipes
     */
    public Cauldron(CauldronCookbook recipes) {
        this.recipes = recipes;
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
     */
    public void preCauldron(PlayerInterface player, WorldInterface world, Vector pt) {
        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();

        int rootY = y;
        int below = world.getId(x, y - 1, z);
        int below2 = world.getId(x, y - 2, z);
        int s1 = world.getId(x + 1, y, z);
        int s3 = world.getId(x - 1, y, z);
        int s2 = world.getId(x, y, z + 1);
        int s4 = world.getId(x, y, z - 1);

        // Preliminary check so we don't waste CPU cycles
        if ((BlockType.isLava(below) || BlockType.isLava(below2))
                && (s1 == BlockType.STONE || s2 == BlockType.STONE
                || s3 == BlockType.STONE || s4 == BlockType.STONE)) {
            // Cauldron is 2 units deep
            if (BlockType.isLava(below)) {
                rootY++;
            }

            performCauldron(player, world, new BlockVector(x, rootY, z));
        }
    }

    /**
     * Attempt to perform a cauldron recipe.
     * 
     * @param pt
     * @param player
     * @param recipes
     */
    private void performCauldron(PlayerInterface player, WorldInterface world, BlockVector pt) {
        // Gotta start at a root Y then find our orientation
        int rootY = pt.getBlockY();

        // Used to store cauldron blocks -- walls are counted
        Map<BlockVector,Integer> visited = new HashMap<BlockVector,Integer>();

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
            Map<Integer,Integer> contents = new HashMap<Integer,Integer>();

            // Now we have to ignore stone blocks so that we get the real
            // contents of the cauldron
            for (Map.Entry<BlockVector,Integer> entry : visited.entrySet()) {
                if (entry.getValue() != BlockType.STONE) {
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
                        if (player.isInGroup(group)) {
                            found = true;
                            break;
                        }
                    }
                    
                    if (!found) {
                        player.sendMessage(Colors.DARK_RED + "Doesn't seem as if you have the ability...");
                        return;
                    }
                }
                
                player.sendMessage(Colors.GOLD + "In a poof of smoke, you've made "
                        + recipe.getName() + ".");

                List<Integer> ingredients =
                        new ArrayList<Integer>(recipe.getIngredients());
                
                List<BlockVector> removeQueue = new ArrayList<BlockVector>();

                // Get rid of the blocks in world
                for (Map.Entry<BlockVector,Integer> entry : visited.entrySet()) {
                    // This is not a fast operation, but we should not have
                    // too many ingredients
                    if (ingredients.contains(entry.getValue())) {
                        // Some blocks need to removed first otherwise they will
                        // drop an item, so let's remove those first
                        if (!BlockType.isBottomDependentBlock(entry.getValue())) {
                            removeQueue.add(entry.getKey());
                        } else {
                            world.setId(entry.getKey(), 0);
                        }
                        ingredients.remove(entry.getValue());
                    }
                }
                
                for (BlockVector v : removeQueue) {
                    world.setId(v, 0);
                }

                // Give results
                for (Integer id : recipe.getResults()) {
                    player.giveItem(id, 1);
                }
            // Didn't find a recipe
            } else {
                player.sendMessage(Colors.RED + "Hmm, this doesn't make anything...");
            }
        } catch (NotACauldronException e) {
        }
    }

    /**
     * Recursively expand the search area so we can define the number of
     * blocks that are in the cauldron. The search will not exceed 24 blocks
     * as no pot will ever use up that many blocks. The Y are bounded both
     * directions so we don't ever search the lava or anything above, although
     * in the case of non-wall blocks, we also make sure that there is standing
     * lava underneath.
     *
     * @param pt
     * @param minY
     * @param maxY
     * @param visited
     * @throws Cauldron.NotACauldronException
     */
    public void findCauldronContents(WorldInterface world, BlockVector pt, int minY, int maxY,
            Map<BlockVector,Integer> visited) throws NotACauldronException {

        // Don't want to go too low or high
        if (pt.getBlockY() < minY) { return; }
        if (pt.getBlockY() > maxY) { return; }

        // There is likely a leak in the cauldron (or this isn't a cauldron)
        if (visited.size() > 24) {
            throw new NotACauldronException("Cauldron has a leak");
        }

        // Prevent infinite looping
        if (visited.containsKey(pt)) { return; }

        int type = world.getId(pt);

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
        if (type == BlockType.STONE) { return; }

        // Must have a lava floor
        Vector lavaPos = pt.subtract(0, pt.getBlockY() - minY + 1, 0);
        if (!BlockType.isLava(world.getId(lavaPos))) {
            throw new NotACauldronException("Cauldron lacks lava below");
        }

        // Now we recurse!
        findCauldronContents(world, pt.add(1, 0, 0).toBlockVector(), minY, maxY, visited);
        findCauldronContents(world, pt.add(-1, 0, 0).toBlockVector(), minY, maxY, visited);
        findCauldronContents(world, pt.add(0, 0, 1).toBlockVector(), minY, maxY, visited);
        findCauldronContents(world, pt.add(0, 0, -1).toBlockVector(), minY, maxY, visited);
        findCauldronContents(world, pt.add(0, 1, 0).toBlockVector(), minY, maxY, visited);
        findCauldronContents(world, pt.add(0, -1, 0).toBlockVector(), minY, maxY, visited);
    }
}
