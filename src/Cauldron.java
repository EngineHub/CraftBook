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

import com.sk89q.craftbook.*;
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
    public void preCauldron(Vector pt, Player player) {
        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();

        int rootY = y;
        int below = CraftBook.getBlockID(x, y - 1, z);
        int below2 = CraftBook.getBlockID(x, y - 2, z);
        int s1 = CraftBook.getBlockID(x + 1, y, z);
        int s2 = CraftBook.getBlockID(x, y,  + 1);
        int s3 = CraftBook.getBlockID(x - 1, y, z);
        int s4 = CraftBook.getBlockID(x, y, z - 1);

        // Preliminary check so we don't waste CPU cycles
        if (below == BlockType.STATIONARY_LAVA || below2 == BlockType.STATIONARY_LAVA
                || s1 == BlockType.STONE || s2 == BlockType.STONE
                || s3 == BlockType.STONE || s4 == BlockType.STONE) {
            // Cauldron is 2 units deep
            if (below == BlockType.STATIONARY_LAVA) {
                rootY++;
            }

            performCauldron(new BlockVector(x, rootY, z), player);
        }
    }

    /**
     * Attempt to perform a cauldron recipe.
     * 
     * @param pt
     * @param player
     * @param recipes
     */
    private void performCauldron(BlockVector pt, Player player) {
        // Gotta start at a root Y then find our orientation
        int rootY = pt.getBlockY();

        // Used to store cauldron blocks -- walls are counted
        Map<BlockVector,Integer> visited = new HashMap<BlockVector,Integer>();

        try {
            // The following attempts to recursively find adjacent blocks so
            // that it can find all the blocks used within the cauldron
            findCauldronContents(pt, rootY - 1, rootY, visited);

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
            CauldronRecipe recipe = recipes.find(contents);

            if (recipe != null) {
                player.sendMessage(Colors.Gold + "In a poof of smoke, you've made "
                        + recipe.getName() + ".");

                List<Integer> ingredients =
                        new ArrayList<Integer>(recipe.getIngredients());

                // Get rid of the blocks in world
                for (Map.Entry<BlockVector,Integer> entry : visited.entrySet()) {
                    // This is not a fast operation, but we should not have
                    // too many ingredients
                    if (ingredients.contains(entry.getValue())) {
                        CraftBook.setBlockID(entry.getKey(), 0);
                        ingredients.remove(entry.getValue());
                    }
                }

                // Give results
                for (Integer id : recipe.getResults()) {
                    player.giveItem(id, 1);
                }
            // Didn't find a recipe
            } else {
                player.sendMessage(Colors.Red + "Hmm, this doesn't make anything...");
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
    public void findCauldronContents(BlockVector pt, int minY, int maxY,
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

        int type = CraftBook.getBlockID(pt);
        
        visited.put(pt, type);

        // It's a wall -- we only needed to remember that we visited it but
        // we don't need to recurse
        if (type == BlockType.STONE) { return; }

        // Must have a stationary lava floor
        Vector lavaPos = pt.subtract(0, pt.getBlockY() - minY + 1, 0);
        if (CraftBook.getBlockID(lavaPos) != BlockType.STATIONARY_LAVA) {
            throw new NotACauldronException("Cauldron lacks stationary lava below");
        }

        // Now we recurse!
        findCauldronContents(pt.add(1, 0, 0).toBlockVector(), minY, maxY, visited);
        findCauldronContents(pt.add(-1, 0, 0).toBlockVector(), minY, maxY, visited);
        findCauldronContents(pt.add(0, 0, 1).toBlockVector(), minY, maxY, visited);
        findCauldronContents(pt.add(0, 0, -1).toBlockVector(), minY, maxY, visited);
        findCauldronContents(pt.add(0, 1, 0).toBlockVector(), minY, maxY, visited);
        findCauldronContents(pt.add(0, -1, 0).toBlockVector(), minY, maxY, visited);
    }
}
