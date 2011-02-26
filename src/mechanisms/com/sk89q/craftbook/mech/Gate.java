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

package com.sk89q.craftbook.mech;

import java.util.Set;
import java.util.HashSet;
import org.bukkit.World;
import org.bukkit.event.block.BlockRightClickEvent;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.Mechanic;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.craftbook.util.BlockWorldVector;
import com.sk89q.craftbook.util.WorldVector;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.blocks.BlockID;

/**
 * Handler for gates. Gates are merely fence blocks. When they are closed
 * or open, a nearby fence will be found, the algorithm will traverse to the
 * top-most connected fence block, and then proceed to recurse to the sides
 * up to a certain number of fences. To the fences that it gets to, it will
 * iterate over the blocks below to open or close the gate.
 *
 * @author sk89q
 */
public class Gate extends Mechanic {
    
    /**
     * Plugin.
     */
    protected MechanismsPlugin plugin;
    
    /**
     * Construct a gate for a location.
     * 
     * @param pt
     * @param plugin 
     */
    public Gate(BlockWorldVector pt, MechanismsPlugin plugin) {
        super(pt);
        this.plugin = plugin;
    }
    
    /**
     * Toggles the gate closest to a location.
     * 
     * @param pt
     * @param smallSearchSize
     * @return
     */
    public boolean toggleGates(WorldVector pt, boolean smallSearchSize) {
        World world = pt.getWorld();
        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();

        boolean foundGate = false;

        Set<BlockVector> visitedColumns = new HashSet<BlockVector>();

        if (smallSearchSize) {
            // Toggle nearby gates
            for (int x1 = x - 1; x1 <= x + 1; x1++) {
                for (int y1 = y - 2; y1 <= y + 1; y1++) {
                    for (int z1 = z - 1; z1 <= z + 1; z1++) {
                        if (recurseColumn(new WorldVector(world, x1, y1, z1),
                                visitedColumns, null)) {
                            foundGate = true;
                        }
                    }
                }
            }
        } else {
            // Toggle nearby gates
            for (int x1 = x - 3; x1 <= x + 3; x1++) {
                for (int y1 = y - 3; y1 <= y + 6; y1++) {
                    for (int z1 = z - 3; z1 <= z + 3; z1++) {
                        if (recurseColumn(new WorldVector(world, x1, y1, z1),
                                visitedColumns, null)) {
                            foundGate = true;
                        }
                    }
                }
            }
        }

        //bag.flushChanges();

        return foundGate;
    }

    /**
     * Set gate states of gates closest to a location.
     *
     * @param pt
     * @param close
     * @param smallSearchSize 
     * @return
     */
    public boolean setGateState(WorldVector pt, boolean close,
            boolean smallSearchSize) {

        World world = pt.getWorld();
        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();

        boolean foundGate = false;

        Set<BlockVector> visitedColumns = new HashSet<BlockVector>();

        if (smallSearchSize) {
            // Toggle nearby gates
            for (int x1 = x - 1; x1 <= x + 1; x1++) {
                for (int y1 = y - 2; y1 <= y + 1; y1++) {
                    for (int z1 = z - 1; z1 <= z + 1; z1++) {
                        if (recurseColumn(new WorldVector(world, x1, y1, z1),
                                visitedColumns, close)) {
                            foundGate = true;
                        }
                    }
                }
            }
        } else {
            // Toggle nearby gates
            for (int x1 = x - 3; x1 <= x + 3; x1++) {
                for (int y1 = y - 3; y1 <= y + 6; y1++) {
                    for (int z1 = z - 3; z1 <= z + 3; z1++) {
                        if (recurseColumn(new WorldVector(world, x1, y1, z1),
                                visitedColumns, close)) {
                            foundGate = true;
                        }
                    }
                }
            }
        }

        //bag.flushChanges();

        return foundGate;
    }

    /**
     * Toggles one column of gate.
     * 
     * @param pt
     * @param visitedColumns
     * @param close
     * @param bag
     * @param state
     * @return
     */
    private boolean recurseColumn(WorldVector pt,
            Set<BlockVector> visitedColumns, Boolean close) {
        
        World world = pt.getWorld();
        if (visitedColumns.size() > 14) { return false; }
        if (visitedColumns.contains(pt.setY(0).toBlockVector())) { return false; }
        if (world.getBlockTypeIdAt(BukkitUtil.toLocation(pt)) != BlockID.FENCE) {
            return false;
        }
        
        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();

        visitedColumns.add(pt.setY(0).toBlockVector());

        // Find the top most fence
        for (int y1 = y + 1; y1 <= y + 12; y1++) {
            if (world.getBlockTypeIdAt(x, y1, z) == BlockID.FENCE) {
                y = y1;
            } else {
                break;
            }
        }

        // The block above the gate cannot be air -- it has to be some
        // non-fence block
        if (world.getBlockTypeIdAt(x, y + 1, z) == 0) {
            return false;
        }

        if (close == null) {
            // Close the gate if the block below does not exist as a fence
            // block, otherwise open the gate
            close = world.getBlockTypeIdAt(x, y - 1, z) != BlockID.FENCE;
        }

        // Recursively go to connected fence blocks of the same level
        // and 'close' or 'open' them
        toggleColumn(new WorldVector(world, x, y, z), close, visitedColumns);

        return true;
    }

    /**
     * Actually does the closing/opening. Also recurses to nearby columns.
     * 
     * @param topPoint
     * @param close
     * @param visitedColumns
     */
    private void toggleColumn(WorldVector topPoint, boolean close,
            Set<BlockVector> visitedColumns)  {

        World world = topPoint.getWorld();
        int x = topPoint.getBlockX();
        int y = topPoint.getBlockY();
        int z = topPoint.getBlockZ();

        // If we want to close the gate then we replace air/water blocks
        // below with fence blocks; otherwise, we want to replace fence
        // blocks below with air
        int minY = Math.max(0, y - 12);
        for (int y1 = y - 1; y1 >= minY; y1--) {
            int cur = world.getBlockTypeIdAt(x, y1, z);

            // Allowing water allows the use of gates as flood gates
            if (cur != BlockID.WATER
                    && cur != BlockID.STATIONARY_WATER
                    && cur != BlockID.LAVA
                    && cur != BlockID.STATIONARY_LAVA
                    && cur != BlockID.FENCE
                    && cur != 0) {
                break;
            }

            //bag.setBlockID(w, x, y1, z, close ? BlockID.FENCE : 0);
            world.getBlockAt(x, y1, z).setTypeId(close ? BlockID.FENCE : 0);

            WorldVector pt = new WorldVector(world, x, y1, z);
            recurseColumn(new WorldVector(world, pt.add(1, 0, 0)), visitedColumns, close);
            recurseColumn(new WorldVector(world, pt.add(-1, 0, 0)), visitedColumns, close);
            recurseColumn(new WorldVector(world, pt.add(0, 0, 1)), visitedColumns, close);
            recurseColumn(new WorldVector(world, pt.add(0, 0, -1)), visitedColumns, close);
        }

        recurseColumn(new WorldVector(world, topPoint.add(1, 0, 0)), visitedColumns, close);
        recurseColumn(new WorldVector(world, topPoint.add(-1, 0, 0)), visitedColumns, close);
        recurseColumn(new WorldVector(world, topPoint.add(0, 0, 1)), visitedColumns, close);
        recurseColumn(new WorldVector(world, topPoint.add(0, 0, -1)), visitedColumns, close);
    }
    
    /**
     * Raised when a block is right clicked.
     * 
     * @param event
     */
    @Override
    public void onRightClick(BlockRightClickEvent event) {
        LocalPlayer player = plugin.wrap(event.getPlayer());
        if (toggleGates(getTriggerPositions().get(0), false)) {
            player.print("Gate toggled!");
        } else {
            player.printError("Failed to find a gate!");
        }
    }

    @Override
    public void unload() {
    }

    @Override
    public boolean isActive() {
        return false; // This keeps no state
    }
}
