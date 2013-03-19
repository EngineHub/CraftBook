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

package com.sk89q.craftbook.mech;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.SourcedBlockRedstoneEvent;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.exceptions.InsufficientPermissionsException;
import com.sk89q.craftbook.util.exceptions.InvalidMechanismException;
import com.sk89q.craftbook.util.exceptions.ProcessedMechanismException;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;

/**
 * Handler for gates. Gates are merely fence blocks. When they are closed or open, a nearby fence will be found,
 * the algorithm will traverse to the
 * top-most connected fence block, and then proceed to recurse to the sides up to a certain number of fences. To the
 * fences that it gets to, it will
 * iterate over the blocks below to open or close the gate.
 *
 * @author sk89q
 */
public class Gate extends AbstractMechanic {

    private CraftBookPlugin plugin = CraftBookPlugin.inst();

    /**
     * Location of the gate.
     */
    private final BlockWorldVector pt;

    /**
     * Indicates a DGate.
     */
    private final boolean smallSearchSize;
    private Sign sign;

    /**
     * Construct a gate for a location.
     *
     * @param pt
     * @param smallSearchSize
     */
    public Gate(BlockWorldVector pt, boolean smallSearchSize) {

        super();
        this.pt = pt;
        this.smallSearchSize = smallSearchSize;

        int id = BukkitUtil.toBlock(pt).getTypeId();
        if (id == BlockID.SIGN_POST || id == BlockID.WALL_SIGN) {
            BlockState state = BukkitUtil.toBlock(pt).getState();
            if (state instanceof Sign) {
                sign = (Sign) state;
            }
        }
    }

    /**
     * Toggles the gate closest to a location.
     *
     * @param pt
     * @param smallSearchSize
     *
     * @return true if a gate was found and blocks were changed; false otherwise.
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
                        if (recurseColumn(player, new WorldVector(world, x1, y1, z1), visitedColumns, null)) {
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
                        if (recurseColumn(player, new WorldVector(world, x1, y1, z1), visitedColumns, null)) {
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
     *
     * @return true if a gate was found and blocks were changed; false otherwise.
     */
    public boolean setGateState(LocalPlayer player, WorldVector pt, boolean close, boolean smallSearchSize) {

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
                        if (recurseColumn(player, new WorldVector(world, x1, y1, z1), visitedColumns, close)) {
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
                        if (recurseColumn(player, new WorldVector(world, x1, y1, z1), visitedColumns, close)) {
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
     *
     * @return true if a gate column was found and blocks were changed; false otherwise.
     */
    private boolean recurseColumn(LocalPlayer player, WorldVector pt, Set<BlockVector> visitedColumns, Boolean close) {

        World world = ((BukkitWorld) pt.getWorld()).getWorld();
        if (plugin.getConfiguration().gateLimitColumns && visitedColumns.size() > plugin.getConfiguration()
                .gateColumnLimit)
            return false;
        if (visitedColumns.contains(pt.setY(0).toBlockVector())) return false;
        if (!isValidGateBlock(world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()), true)) return false;

        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();

        visitedColumns.add(pt.setY(0).toBlockVector());

        // Find the top most fence
        for (int y1 = y + 1; y1 <= y + 12; y1++) {
            if (isValidGateBlock(world.getBlockAt(x, y1, z), true)) {
                y = y1;
            } else {
                break;
            }
        }

        // The block above the gate cannot be air -- it has to be some
        // non-fence block
        if (world.getBlockTypeIdAt(x, y + 1, z) == 0) return false;

        if (close == null) {
            close = !isValidGateBlock(world.getBlockAt(x, y - 1, z), true);
        }

        // Recursively go to connected fence blocks of the same level
        // and 'close' or 'open' them
        return toggleColumn(player, new BlockWorldVector(pt, x, y, z), close, visitedColumns);
    }

    /**
     * Actually does the closing/opening. Also recurses to nearby columns.
     *
     * @param topPoint
     * @param close
     * @param visitedColumns
     */
    private boolean toggleColumn(LocalPlayer player, WorldVector topPoint, boolean close,
            Set<BlockVector> visitedColumns) {

        World world = ((BukkitWorld) topPoint.getWorld()).getWorld();
        int x = topPoint.getBlockX();
        int y = topPoint.getBlockY();
        int z = topPoint.getBlockZ();

        // If we want to close the gate then we replace air/water blocks
        // below with fence blocks; otherwise, we want to replace fence
        // blocks below with air
        int minY = Math.max(0, y - 12);
        int ID = 0;
        if (close) {
            ID = world.getBlockAt(x, y, z).getTypeId();
        }
        for (int y1 = y - 1; y1 >= minY; y1--) {
            int cur = world.getBlockTypeIdAt(x, y1, z);

            Block block = BukkitUtil.toWorld(pt).getBlockAt(BukkitUtil.toLocation(pt));

            Sign sign = null;
            Sign otherSign = null;

            if (block.getTypeId() == BlockID.WALL_SIGN || block.getTypeId() == BlockID.SIGN_POST) {
                BlockState state = block.getState();
                if (state instanceof Sign) {
                    sign = (Sign) state;
                }
            }

            if (sign != null) {
                otherSign = SignUtil.getNextSign(sign, sign.getLine(1), 4);
            }

            if (sign != null && sign.getLine(2).equalsIgnoreCase("NoReplace")) {
                // If NoReplace is on line 3 of sign, do not replace blocks.
                if (cur != 0 && !isValidGateBlock(cur, true)) {
                    break;
                }
            } else // Allowing water allows the use of gates as flood gates
                if (!canPassThrough(cur)) {
                    break;
                }

            // bag.setBlockID(w, x, y1, z, ID);
            if (plugin.getConfiguration().safeDestruction) {
                if (ID == 0 || hasEnoughBlocks(sign, otherSign)) {
                    if (ID == 0 && isValidGateBlock(world.getBlockAt(x, y1, z), true)) {
                        addBlocks(sign, 1);
                    } else if (ID != 0 && canPassThrough(world.getBlockAt(x, y1, z).getTypeId()) && isValidGateItem
                            (new ItemStack(ID, 1), true)) {
                        removeBlocks(sign, 1);
                    }
                    world.getBlockAt(x, y1, z).setTypeId(ID);

                    setBlocks(sign, getBlocks(sign, otherSign));
                } else if (!hasEnoughBlocks(sign, otherSign) && isValidGateItem(new ItemStack(ID, 1), true))
                    if (player != null) {
                        player.printError("mech.not-enough-blocks");
                        return false;
                    }
            } else {
                world.getBlockAt(x, y1, z).setTypeId(ID);
            }

            WorldVector pt = new BlockWorldVector(topPoint, x, y1, z);
            recurseColumn(player, new BlockWorldVector(topPoint, pt.add(1, 0, 0)), visitedColumns, close);
            recurseColumn(player, new BlockWorldVector(topPoint, pt.add(-1, 0, 0)), visitedColumns, close);
            recurseColumn(player, new BlockWorldVector(topPoint, pt.add(0, 0, 1)), visitedColumns, close);
            recurseColumn(player, new BlockWorldVector(topPoint, pt.add(0, 0, -1)), visitedColumns, close);
        }

        recurseColumn(player, new BlockWorldVector(topPoint, topPoint.add(1, 0, 0)), visitedColumns, close);
        recurseColumn(player, new BlockWorldVector(topPoint, topPoint.add(-1, 0, 0)), visitedColumns, close);
        recurseColumn(player, new BlockWorldVector(topPoint, topPoint.add(0, 0, 1)), visitedColumns, close);
        recurseColumn(player, new BlockWorldVector(topPoint, topPoint.add(0, 0, -1)), visitedColumns, close);

        recurseColumn(player, new BlockWorldVector(topPoint, topPoint.add(1, 1, 0)), visitedColumns, close);
        recurseColumn(player, new BlockWorldVector(topPoint, topPoint.add(-1, 1, 0)), visitedColumns, close);
        recurseColumn(player, new BlockWorldVector(topPoint, topPoint.add(0, 1, 1)), visitedColumns, close);
        recurseColumn(player, new BlockWorldVector(topPoint, topPoint.add(0, 1, -1)), visitedColumns, close);
        return true;
    }

    /**
     * Raised when a block is right clicked.
     *
     * @param event
     */
    @Override
    public void onRightClick(PlayerInteractEvent event) {

        if (!plugin.getConfiguration().gateEnabled) return;

        LocalPlayer player = plugin.wrapPlayer(event.getPlayer());

        Sign sign = null;

        if (event.getClickedBlock().getTypeId() == BlockID.SIGN_POST || event.getClickedBlock().getTypeId() ==
                BlockID.WALL_SIGN) {
            BlockState state = event.getClickedBlock().getState();
            if (state instanceof Sign) {
                sign = (Sign) state;
            }
        }
        if (sign == null) return;

        if (plugin.getConfiguration().safeDestruction && getGateBlock() == player.getTypeInHand() && isValidGateBlock(getGateBlock(), false)) {

            if (!player.hasPermission("craftbook.mech.gate.restock")) {
                player.printError("mech.restock-permission");
                return;
            }

            int amount = 1;
            if (event.getPlayer().isSneaking() && event.getPlayer().getItemInHand().getAmount() >= 5) {
                amount = 5;
            }
            addBlocks(sign, amount);

            if (!(event.getPlayer().getGameMode() == GameMode.CREATIVE))
                if (event.getPlayer().getItemInHand().getAmount() <= amount) {
                    event.getPlayer().setItemInHand(new ItemStack(0, 0));
                } else {
                    event.getPlayer().getItemInHand().setAmount(event.getPlayer().getItemInHand().getAmount() - amount);
                }

            player.print("mech.restock");
            event.setCancelled(true);
            return;
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

        if (!plugin.getConfiguration().gateAllowRedstone) return;

        if (event.getNewCurrent() == event.getOldCurrent()) return;

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

            @Override
            public void run() {

                setGateState(null, pt, event.getNewCurrent() > 0, smallSearchSize);
            }
        }, 2);
    }

    public static class Factory extends AbstractMechanicFactory<Gate> {

        public Factory() {

        }

        @Override
        public Gate detect(BlockWorldVector pt) {

            Block block = BukkitUtil.toWorld(pt).getBlockAt(BukkitUtil.toLocation(pt));
            if (block.getTypeId() == BlockID.WALL_SIGN || block.getTypeId() == BlockID.SIGN_POST) {
                BlockState state = block.getState();
                if (state instanceof Sign) {
                    Sign sign = (Sign) state;
                    if (sign.getLine(1).equalsIgnoreCase("[Gate]") || sign.getLine(1).equalsIgnoreCase("[DGate]"))
                        // this is a little funky because we don't actually look for the blocks that make up the movable
                        // parts of the gate until we're running the event later... so the factory can succeed even if
                        // the signpost doesn't actually operate any gates correctly. but it works!
                        return new Gate(pt, sign.getLine(1).equalsIgnoreCase("[DGate]"));
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
        public Gate detect(BlockWorldVector pt, LocalPlayer player,
                ChangedSign sign) throws InvalidMechanismException, ProcessedMechanismException {

            if (sign.getLine(1).equalsIgnoreCase("[Gate]")) {
                player.checkPermission("craftbook.mech.gate");
                // get the material that this gate should toggle and verify it
                String line0 = sign.getLine(0).trim();
                if (line0 != null && !line0.isEmpty()) {
                    try {
                        int iLine0 = Integer.parseInt(line0);
                        if (iLine0 != 0 && !isValidGateBlock(iLine0)) {
                            throw new NumberFormatException();
                        }
                    } catch (NumberFormatException e) {
                        throw new InvalidMechanismException("Line 1 needs to be a valid block id.");
                    }
                } else {
                }
                sign.setLine(1, "[Gate]");
                if (sign.getLine(3).equalsIgnoreCase("infinite") && !player.hasPermission("craftbook.mech.gate" + "" +
                        ".infinite")) {
                    sign.setLine(3, "0");
                } else if (!sign.getLine(3).equalsIgnoreCase("infinite")) {
                    sign.setLine(3, "0");
                }
                sign.update(false);
                player.print("mech.gate.create");
            } else if (sign.getLine(1).equalsIgnoreCase("[DGate]")) {
                if (!player.hasPermission("craftbook.mech.gate") && !player.hasPermission("craftbook.mech.dgate"))
                    throw new InsufficientPermissionsException();
                // get the material that this gate should toggle and verify it
                String line0 = sign.getLine(0).trim();
                if (line0 != null && !line0.isEmpty()) {
                    try {
                        if (!isValidGateBlock(Integer.parseInt(line0))) {
                            throw new NumberFormatException();
                        }
                    } catch (NumberFormatException e) {
                        throw new InvalidMechanismException("Line 1 needs to be a valid block id.");
                    }
                } else {
                }
                sign.setLine(1, "[DGate]");
                if (sign.getLine(3).equalsIgnoreCase("infinite") && !player.hasPermission("craftbook.mech.gate" + "" +
                        ".infinite")) {
                    sign.setLine(3, "0");
                } else if (!sign.getLine(3).equalsIgnoreCase("infinite")) {
                    sign.setLine(3, "0");
                }
                sign.update(false);
                player.print("mech.dgate.create");
            } else return null;

            throw new ProcessedMechanismException();
        }

        public boolean isValidGateBlock(int block) {

            return CraftBookPlugin.inst().getConfiguration().gateBlocks.contains(block);
        }
    }

    public boolean isValidGateBlock(Block block, boolean check) {

        return isValidGateBlock(block.getTypeId(), check);
    }

    public boolean isValidGateBlock(int block, boolean check) {

        Block b = BukkitUtil.toWorld(pt).getBlockAt(BukkitUtil.toLocation(pt));

        Sign sign = null;
        int type;

        if (b.getTypeId() == BlockID.WALL_SIGN || b.getTypeId() == BlockID.SIGN_POST) {
            BlockState state = b.getState();
            if (state instanceof Sign) {
                sign = (Sign) state;
            }
        }
        if (sign != null && !sign.getLine(0).isEmpty()) {
            try {
                int id = Integer.parseInt(sign.getLine(0));
                return block == id;
            } catch (Exception e) {
                if (check) {
                    type = getGateBlock();
                    if(type != 0)
                        return block == type;
                }
                return plugin.getConfiguration().gateBlocks.contains(block);
            }
        } else if(check && (type = getGateBlock()) != 0) {
            if(type != 0)
                return block == type;
        } else
            return plugin.getConfiguration().gateBlocks.contains(block);
        return false;
    }

    public boolean isValidGateItem(ItemStack block, boolean check) {

        return isValidGateItem(block.getTypeId(), check);
    }

    public boolean isValidGateItem(int block, boolean check) {

        Block b = BukkitUtil.toWorld(pt).getBlockAt(BukkitUtil.toLocation(pt));

        Sign sign = null;
        int type;

        if (b.getTypeId() == BlockID.WALL_SIGN || b.getTypeId() == BlockID.SIGN_POST) {
            BlockState state = b.getState();
            if (state instanceof Sign) {
                sign = (Sign) state;
            }
        }
        if (sign != null && !sign.getLine(0).isEmpty()) {
            try {
                int id = Integer.parseInt(sign.getLine(0));
                return block == id;
            } catch (Exception e) {
                if(check) {
                    type = getGateBlock();
                    if(type != 0)
                        return block == type;
                }
                return plugin.getConfiguration().gateBlocks.contains(block);
            }
        } else if(check && (type = getGateBlock()) != 0) {
            if(type != 0)
                return block == type;
        } else
            return plugin.getConfiguration().gateBlocks.contains(block);
        return false;
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {

        Sign sign = null;

        if (event.getBlock().getTypeId() == BlockID.WALL_SIGN || event.getBlock().getTypeId() == BlockID.SIGN_POST) {
            BlockState state = event.getBlock().getState();
            if (state instanceof Sign) {
                sign = (Sign) state;
            }
        }

        if (sign == null) return;

        if (hasEnoughBlocks(sign)) {
            int type = getGateBlock();
            if(type == 0)
                type = BlockID.FENCE;
            ItemStack toDrop = new ItemStack(type, getBlocks(sign));
            sign.getWorld().dropItemNaturally(sign.getLocation(), toDrop);
        }
    }

    private boolean canPassThrough(int t) {

        int[] passableBlocks = new int[9];
        passableBlocks[0] = BlockID.WATER;
        passableBlocks[1] = BlockID.STATIONARY_WATER;
        passableBlocks[2] = BlockID.LAVA;
        passableBlocks[3] = BlockID.STATIONARY_LAVA;
        passableBlocks[4] = BlockID.SNOW;
        passableBlocks[5] = BlockID.LONG_GRASS;
        passableBlocks[6] = BlockID.VINE;
        passableBlocks[7] = BlockID.DEAD_BUSH;
        passableBlocks[8] = BlockID.AIR;

        for (int aPassableBlock : passableBlocks) { if (aPassableBlock == t) return true; }

        return isValidGateBlock(t, true);
    }

    public int getGateBlock() {

        int gateBlock = 0;

        if (!sign.getLine(0).isEmpty()) {
            try {
                return Integer.parseInt(sign.getLine(0));
            } catch (Exception ignored) {
            }
        }
        LocalWorld world = pt.getWorld();
        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();

        if (smallSearchSize) {
            for (int x1 = x - 1; x1 <= x + 1; x1++) {
                for (int y1 = y - 2; y1 <= y + 1; y1++) {
                    for (int z1 = z - 1; z1 <= z + 1; z1++) {
                        if (getFirstBlock(new WorldVector(world, x1, y1, z1)) != 0) {
                            gateBlock = getFirstBlock(new WorldVector(world, x1, y1, z1));
                        }
                    }
                }
            }
        } else {
            for (int x1 = x - 3; x1 <= x + 3; x1++) {
                for (int y1 = y - 3; y1 <= y + 6; y1++) {
                    for (int z1 = z - 3; z1 <= z + 3; z1++) {
                        if (getFirstBlock(new WorldVector(world, x1, y1, z1)) != 0) {
                            gateBlock = getFirstBlock(new WorldVector(world, x1, y1, z1));
                        }
                    }
                }
            }
        }

        if(plugin.getConfiguration().gateEnforceType && gateBlock != 0) {
            sign.setLine(0, String.valueOf(gateBlock));
            sign.update();
        }

        return gateBlock;
    }

    public int getFirstBlock(WorldVector pt) {

        World world = ((BukkitWorld) pt.getWorld()).getWorld();
        if (!isValidGateBlock(world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()), false)) return 0;

        return world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).getTypeId();
    }

    public boolean removeBlocks(Sign s, int amount) {

        if (s.getLine(3).equalsIgnoreCase("infinite")) return true;
        int curBlocks = getBlocks(s) - amount;
        s.setLine(3, String.valueOf(curBlocks));
        s.update();
        return curBlocks >= 3;
    }

    public boolean addBlocks(Sign s, int amount) {

        if (s.getLine(3).equalsIgnoreCase("infinite")) return true;
        int curBlocks = getBlocks(s) + amount;
        s.setLine(3, String.valueOf(curBlocks));
        s.update();
        return curBlocks >= 0;
    }

    public void setBlocks(Sign s, int amount) {

        if (s.getLine(3).equalsIgnoreCase("infinite")) return;
        s.setLine(3, String.valueOf(amount));
        s.update();
    }

    public int getBlocks(Sign s) {

        if (s.getLine(3).equalsIgnoreCase("infinite")) return 0;
        return getBlocks(s, null);
    }

    public int getBlocks(Sign s, Sign other) {

        if (s.getLine(3).equalsIgnoreCase("infinite") || other != null && other.getLine(3).equalsIgnoreCase("infinite"))
            return 0;
        int curBlocks = 0;
        try {
            curBlocks = Integer.parseInt(s.getLine(3));
            try {
                curBlocks += Integer.parseInt(other.getLine(3));
                setBlocks(s, curBlocks);
                setBlocks(other, 0);
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            curBlocks = 0;
        }
        return curBlocks;
    }

    public boolean hasEnoughBlocks(Sign s) {

        return s.getLine(3).equalsIgnoreCase("infinite") || getBlocks(s) > 0;
    }

    public boolean hasEnoughBlocks(Sign s, Sign other) {

        return s != null && s.getLine(3).equalsIgnoreCase("infinite") || other != null && other.getLine(3)
                .equalsIgnoreCase("infinite")
                || getBlocks(s, other) > 0;
    }

    // TODO Use this to clean this mech up
    protected class GateColumn {

        private final BlockWorldVector bwv;

        public GateColumn(LocalWorld world, int x, int y, int z) {

            bwv = new BlockWorldVector(world, x, y, z);
        }

        public BlockVector getStartingPoint() {

            return bwv.toBlockVector();
        }

        public BlockVector getEndingPoint() {

            return new BlockVector(bwv.getBlockX(), getEndingY(), bwv.getBlockZ());
        }

        public int getStartingY() {

            return bwv.getBlockY();
        }

        public int getEndingY() {

            for (int y = bwv.getBlockY(); y > 0; y--) {
                if (!canPassThrough(bwv.getWorld().getBlockType(bwv.toBlockVector()))) return y + 1;
            }
            return 0;
        }

        public int getX() {

            return bwv.getBlockX();
        }

        public int getZ() {

            return bwv.getBlockZ();
        }

        public CuboidRegion getRegion() {

            return new CuboidRegion(getStartingPoint(), getEndingPoint());
        }
    }
}