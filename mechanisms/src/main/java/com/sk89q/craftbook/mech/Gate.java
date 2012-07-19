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

import java.util.HashSet;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.InsufficientPermissionsException;
import com.sk89q.craftbook.InvalidMechanismException;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.ProcessedMechanismException;
import com.sk89q.craftbook.SourcedBlockRedstoneEvent;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;

/**
 * Handler for gates. Gates are merely fence blocks. When they are closed or
 * open, a nearby fence will be found, the algorithm will traverse to the
 * top-most connected fence block, and then proceed to recurse to the sides up
 * to a certain number of fences. To the fences that it gets to, it will iterate
 * over the blocks below to open or close the gate.
 * 
 * @author sk89q
 */
public class Gate extends AbstractMechanic {

    /**
     * Plugin.
     */
    protected MechanismsPlugin plugin;

    /**
     * Location of the gate.
     */
    protected BlockWorldVector pt;

    /**
     * Indicates a DGate.
     */
    protected boolean smallSearchSize;

    /**
     * Construct a gate for a location.
     * 
     * @param pt
     * @param plugin
     * @param smallSearchSize
     */
    public Gate(BlockWorldVector pt, MechanismsPlugin plugin,
            boolean smallSearchSize) {
        super();
        this.pt = pt;
        this.plugin = plugin;
        this.smallSearchSize = smallSearchSize;
    }

    /**
     * Toggles the gate closest to a location.
     * 
     * @param pt
     * @param smallSearchSize
     * @return true if a gate was found and blocks were changed; false
     *         otherwise.
     */
    public boolean toggleGates(LocalPlayer player, WorldVector pt, boolean smallSearchSize) {
        LocalWorld world = pt.getWorld();
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
                        if (recurseColumn(player, new WorldVector(world, x1, y1, z1),
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
                        if (recurseColumn(player, new WorldVector(world, x1, y1, z1),
                                visitedColumns, null)) {
                            foundGate = true;
                        }
                    }
                }
            }
        }

        // bag.flushChanges();

        return foundGate;
    }

    /**
     * Set gate states of gates closest to a location.
     * 
     * @param pt
     * @param close
     * @param smallSearchSize
     * @return true if a gate was found and blocks were changed; false
     *         otherwise.
     */
    public boolean setGateState(LocalPlayer player, WorldVector pt, boolean close,
            boolean smallSearchSize) {

        LocalWorld world = pt.getWorld();
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
                        if (recurseColumn(player, new WorldVector(world, x1, y1, z1),
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
                        if (recurseColumn(player, new WorldVector(world, x1, y1, z1),
                                visitedColumns, close)) {
                            foundGate = true;
                        }
                    }
                }
            }
        }

        // bag.flushChanges();

        return foundGate;
    }

    /**
     * Toggles one column of gate.
     * 
     * @param pt
     * @param visitedColumns
     * @param close
     * @return true if a gate column was found and blocks were changed; false
     *         otherwise.
     */
    private boolean recurseColumn(LocalPlayer player, WorldVector pt,
            Set<BlockVector> visitedColumns, Boolean close) {

        World world = ((BukkitWorld) pt.getWorld()).getWorld();
        if (visitedColumns.size() > 14) {
            return false;
        }
        if (visitedColumns.contains(pt.setY(0).toBlockVector())) {
            return false;
        }
        if (!isValidGateBlock(world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()))) {
            return false;
        }

        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();

        visitedColumns.add(pt.setY(0).toBlockVector());

        // Find the top most fence
        for (int y1 = y + 1; y1 <= y + 12; y1++) {
            if (isValidGateBlock(world.getBlockAt(x, y1, z))) {
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

            close = !isValidGateBlock(world.getBlockAt(x, y - 1, z));
        }

        // Recursively go to connected fence blocks of the same level
        // and 'close' or 'open' them
        toggleColumn(player, new BlockWorldVector(pt, x, y, z), close, visitedColumns);

        return true;
    }

    /**
     * Actually does the closing/opening. Also recurses to nearby columns.
     * 
     * @param topPoint
     * @param close
     * @param visitedColumns
     */
    private void toggleColumn(LocalPlayer player, WorldVector topPoint, boolean close,
            Set<BlockVector> visitedColumns) {

        World world = ((BukkitWorld) topPoint.getWorld()).getWorld();
        int x = topPoint.getBlockX();
        int y = topPoint.getBlockY();
        int z = topPoint.getBlockZ();

        int curBlocks = 0;

        // If we want to close the gate then we replace air/water blocks
        // below with fence blocks; otherwise, we want to replace fence
        // blocks below with air
        int minY = Math.max(0, y - 12);
        int ID = 0;
        if (close)
            ID = world.getBlockAt(x, y, z).getTypeId();
        for (int y1 = y - 1; y1 >= minY; y1--) {
            int cur = world.getBlockTypeIdAt(x, y1, z);

            Block block = BukkitUtil.toWorld(pt).getBlockAt(
                    BukkitUtil.toLocation(pt));

            Sign sign = null;

            if (block.getTypeId() == BlockID.WALL_SIGN) {
                BlockState state = block.getState();
                if (state instanceof Sign)
                    sign = (Sign) state;
            }

            if(sign!=null && sign.getLine(3).length() > 0) {
                try {
                    curBlocks = Integer.parseInt(sign.getLine(3));
                }
                catch(Exception e){
                    curBlocks = 0;
                    sign.setLine(3, "0");
                    sign.update();
                }
            }

            if(sign!= null && sign.getLine(2).equalsIgnoreCase("NoReplace")) {
                // If NoReplace is on line 3 of sign, do not replace blocks.
                if (cur != 0) {
                    break;
                }
            }
            else {
                // Allowing water allows the use of gates as flood gates
                if (cur != BlockID.WATER && cur != BlockID.STATIONARY_WATER
                        && cur != BlockID.LAVA && cur != BlockID.STATIONARY_LAVA
                        && cur != BlockID.FENCE
                        && cur != BlockID.NETHER_BRICK_FENCE && cur != BlockID.SNOW
                        && cur != BlockID.IRON_BARS && cur != BlockID.GLASS_PANE
                        && cur != BlockID.LONG_GRASS && cur != 0) {
                    break;
                }
            }

            // bag.setBlockID(w, x, y1, z, ID);
            if(plugin.getLocalConfiguration().mechSettings.stopDestruction) {
                if(ID == 0 || curBlocks > 0) {
                    if(ID == 0 && isValidGateBlock(world.getBlockAt(x, y1, z)))
                        curBlocks ++;
                    else if(world.getBlockAt(x, y1, z).getTypeId() == 0 && ID != 0)
                        curBlocks --;
                    world.getBlockAt(x, y1, z).setTypeId(ID);

                    sign.setLine(3, curBlocks + "");
                    sign.update();
                } else if(curBlocks == 0 && ID != 0) {
                    if(player!=null) {
                        player.printError("Not enough blocks to trigger mechanic!");
                        break;
                    }
                }
            }
            else
                world.getBlockAt(x, y1, z).setTypeId(ID);

            WorldVector pt = new BlockWorldVector(topPoint, x, y1, z);
            recurseColumn(player, new BlockWorldVector(topPoint, pt.add(1, 0, 0)),
                    visitedColumns, close);
            recurseColumn(player, new BlockWorldVector(topPoint, pt.add(-1, 0, 0)),
                    visitedColumns, close);
            recurseColumn(player, new BlockWorldVector(topPoint, pt.add(0, 0, 1)),
                    visitedColumns, close);
            recurseColumn(player, new BlockWorldVector(topPoint, pt.add(0, 0, -1)),
                    visitedColumns, close);
        }

        recurseColumn(player, new BlockWorldVector(topPoint, topPoint.add(1, 0, 0)),
                visitedColumns, close);
        recurseColumn(player, new BlockWorldVector(topPoint, topPoint.add(-1, 0, 0)),
                visitedColumns, close);
        recurseColumn(player, new BlockWorldVector(topPoint, topPoint.add(0, 0, 1)),
                visitedColumns, close);
        recurseColumn(player, new BlockWorldVector(topPoint, topPoint.add(0, 0, -1)),
                visitedColumns, close);

        recurseColumn(player, new BlockWorldVector(topPoint, topPoint.add(1, 1, 0)),
                visitedColumns, close);
        recurseColumn(player, new BlockWorldVector(topPoint, topPoint.add(-1, 1, 0)),
                visitedColumns, close);
        recurseColumn(player, new BlockWorldVector(topPoint, topPoint.add(0, 1, 1)),
                visitedColumns, close);
        recurseColumn(player, new BlockWorldVector(topPoint, topPoint.add(0, 1, -1)),
                visitedColumns, close);
    }

    /**
     * Raised when a block is right clicked.
     * 
     * @param event
     */
    @Override
    public void onRightClick(PlayerInteractEvent event) {
        if (!plugin.getLocalConfiguration().gateSettings.enable)
            return;

        LocalPlayer player = plugin.wrap(event.getPlayer());

        if(event.getPlayer().getItemInHand() != null) {
            if(isValidGateItem(event.getPlayer().getItemInHand())) {

                Sign sign = null;

                if (event.getClickedBlock().getTypeId() == BlockID.SIGN_POST || event.getClickedBlock().getTypeId() == BlockID.WALL_SIGN) {
                    BlockState state = event.getClickedBlock().getState();
                    if (state instanceof Sign)
                        sign = (Sign) state;
                }

                if(sign!=null) {
                    try {
                        int newBlocks = Integer.parseInt(sign.getLine(3)) + 1;
                        sign.setLine(3, newBlocks + "");
                        sign.update();
                    }
                    catch(Exception e) {
                        sign.setLine(3, "1");
                        sign.update();
                    }

                    if(event.getPlayer().getItemInHand().getAmount() <= 1) {
                        event.getPlayer().setItemInHand(new ItemStack(0,0));
                    }
                    else
                        event.getPlayer().getItemInHand().setAmount(event.getPlayer().getItemInHand().getAmount() - 1);

                    player.print("Gate Restocked!");
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (!player.hasPermission("craftbook.mech.gate.use")) {
            player.printError("mech.use-permission");
            return;
        }

        if (toggleGates(player, pt, smallSearchSize)) {
            player.print("mech.gate.toggle");
        } else {
            player.printError("mech.gate.not-found");
        }

        event.setCancelled(true);
    }

    /**
     * Raised when an input redstone current changes.
     * 
     * @param event
     */
    @Override
    public void onBlockRedstoneChange(final SourcedBlockRedstoneEvent event) {
        if (!plugin.getLocalConfiguration().gateSettings.enableRedstone)
            return;

        if (event.getNewCurrent() == event.getOldCurrent())
            return;

        plugin.getServer().getScheduler()
        .scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                setGateState(null, pt, event.getNewCurrent() > 0,
                        smallSearchSize);
            }
        }, 2);
    }

    @Override
    public void unload() {
    }

    @Override
    public boolean isActive() {
        return false; //Is not persistent
    }

    public static class Factory extends AbstractMechanicFactory<Gate> {

        protected MechanismsPlugin plugin;

        public Factory(MechanismsPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public Gate detect(BlockWorldVector pt) {
            Block block = BukkitUtil.toWorld(pt).getBlockAt(
                    BukkitUtil.toLocation(pt));
            if (block.getTypeId() == BlockID.WALL_SIGN) {
                BlockState state = block.getState();
                if (state instanceof Sign) {
                    Sign sign = (Sign) state;
                    if (sign.getLine(1).equalsIgnoreCase("[Gate]")
                            || sign.getLine(1).equalsIgnoreCase("[DGate]")) {
                        // this is a little funky because we don't actually look
                        // for the blocks
                        // that make up the movable parts of the gate until
                        // we're running the
                        // event later... so the factory can succeed even if the
                        // signpost doesn't
                        // actually operate any gates correctly. but it works!
                        return new Gate(pt, plugin, sign.getLine(1)
                                .equalsIgnoreCase("[DGate]"));
                    }
                }
            }

            return null;
        }

        /**
         * Detect the mechanic at a placed sign.
         * 
         * @throws ProcessedMechanismException
         */
        @Override
        public Gate detect(BlockWorldVector pt, LocalPlayer player, Sign sign)
                throws InvalidMechanismException, ProcessedMechanismException {
            if (sign.getLine(1).equalsIgnoreCase("[Gate]")) {
                if (!player.hasPermission("craftbook.mech.gate")) {
                    throw new InsufficientPermissionsException();
                }

                sign.setLine(1, "[Gate]");
                sign.setLine(3, "0");
                sign.update();
                player.print("mech.gate.create");
            } else if (sign.getLine(1).equalsIgnoreCase("[DGate]")) {
                if (!player.hasPermission("craftbook.mech.gate")) {
                    throw new InsufficientPermissionsException();
                }

                sign.setLine(1, "[DGate]");
                sign.setLine(3, "0");
                sign.update();
                player.print("mech.dgate.create");
            } else {
                return null;
            }

            throw new ProcessedMechanismException();
        }

    }

    public boolean isValidGateBlock(Block block) {
        return plugin.getLocalConfiguration().gateSettings.canUseBlock(block.getType());
    }

    public boolean isValidGateItem(ItemStack block) {
        return plugin.getLocalConfiguration().gateSettings.canUseBlock(block.getType());
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {

    }

    @Override
    public void unloadWithEvent(ChunkUnloadEvent event) {
    }
}