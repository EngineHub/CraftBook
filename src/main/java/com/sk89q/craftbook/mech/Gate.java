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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.SourcedBlockRedstoneEvent;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.exceptions.InsufficientPermissionsException;
import com.sk89q.craftbook.util.exceptions.InvalidMechanismException;
import com.sk89q.craftbook.util.exceptions.ProcessedMechanismException;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.blocks.BlockID;
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
    private ChangedSign sign;

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
        if (id == BlockID.SIGN_POST || id == BlockID.WALL_SIGN)
            sign = BukkitUtil.toChangedSign(BukkitUtil.toBlock(pt));
    }

    /**
     * Toggles the gate closest to a location.
     *
     * @param player
     * @param pt
     * @param smallSearchSize
     * @param close null to toggle, true to close, false to open
     *
     * @return true if a gate was found and blocks were changed; false otherwise.
     */
    public boolean toggleGates(LocalPlayer player, WorldVector pt, boolean smallSearchSize, Boolean close) {

        LocalWorld world = pt.getWorld();
        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();

        boolean foundGate = false;

        Set<GateColumn> visitedColumns = new HashSet<GateColumn>();

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
            for (int x1 = x - CraftBookPlugin.inst().getConfiguration().gateSearchRadius; x1 <= x + CraftBookPlugin.inst().getConfiguration().gateSearchRadius; x1++) {
                for (int y1 = y - CraftBookPlugin.inst().getConfiguration().gateSearchRadius; y1 <= y + CraftBookPlugin.inst().getConfiguration().gateSearchRadius*2; y1++) {
                    for (int z1 = z - CraftBookPlugin.inst().getConfiguration().gateSearchRadius; z1 <= z + CraftBookPlugin.inst().getConfiguration().gateSearchRadius; z1++) {
                        if (recurseColumn(player, new WorldVector(world, x1, y1, z1), visitedColumns, close)) {
                            foundGate = true;
                        }
                    }
                }
            }
        }

        // bag.flushChanges();

        return foundGate && visitedColumns.size() > 0;
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
    private boolean recurseColumn(LocalPlayer player, WorldVector pt, Set<GateColumn> visitedColumns, Boolean close) {

        if (plugin.getConfiguration().gateLimitColumns && visitedColumns.size() > plugin.getConfiguration().gateColumnLimit)
            return false;

        World world = ((BukkitWorld) pt.getWorld()).getWorld();

        if (!isValidGateBlock(world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()), true)) return false;

        CraftBookPlugin.logDebugMessage("Found a possible gate column at " + pt.getX() + ":" + pt.getY() + ":" + pt.getZ(), "gates.search");

        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();

        GateColumn column = new GateColumn(pt.getWorld(), x, y, z);

        // The block above the gate cannot be air -- it has to be some
        // non-fence block
        if (world.getBlockTypeIdAt(x, column.getStartingY() + 1, z) == 0) return false;

        if (visitedColumns.contains(column)) return false;

        visitedColumns.add(column);

        if (close == null)
            close = !isValidGateBlock(world.getBlockAt(x, column.getStartingY() - 1, z), true);

        CraftBookPlugin.logDebugMessage("Valid column at " + pt.getX() + ":" + pt.getY() + ":" + pt.getZ() + " is being " + (close ? "closed" : "opened"), "gates.search");
        CraftBookPlugin.logDebugMessage("Column Top: " + column.getStartingY() + " End: " + column.getEndingY(), "gates.search");
        // Recursively go to connected fence blocks of the same level
        // and 'close' or 'open' them
        return toggleColumn(player, column, close, visitedColumns);
    }

    /**
     * Actually does the closing/opening. Also recurses to nearby columns.
     *
     * @param topPoint
     * @param close
     * @param visitedColumns
     */
    private boolean toggleColumn(LocalPlayer player, GateColumn column, boolean close, Set<GateColumn> visitedColumns) {

        // If we want to close the gate then we replace air/water blocks
        // below with fence blocks; otherwise, we want to replace fence
        // blocks below with air
        int ID = 0;
        byte data = 0;
        if (close) {
            ID = BukkitUtil.toBlock(column.getStartingPoint()).getTypeId();
            data = BukkitUtil.toBlock(column.getStartingPoint()).getData();
        }

        CraftBookPlugin.logDebugMessage("Setting column at " + pt.getX() + ":" + pt.getY() + ":" + pt.getZ() + " to " + ID + ":" + data, "gates.search");

        Block signBlock = BukkitUtil.toBlock(pt);
        ChangedSign sign = BukkitUtil.toChangedSign(signBlock);
        ChangedSign otherSign = null;

        if (sign != null) {
            Block ot = SignUtil.getNextSign(signBlock, sign.getLine(1), 4);
            if(ot != null)
                otherSign = BukkitUtil.toChangedSign(ot);
        }

        for (Vector bl : column.getRegion()) {

            Block block = BukkitUtil.toBlock(new BlockWorldVector(pt.getWorld(), bl));

            if (sign != null && sign.getLine(2).equalsIgnoreCase("NoReplace")) {
                // If NoReplace is on line 3 of sign, do not replace blocks.
                if (block.getTypeId() != 0 && !isValidGateBlock(block.getTypeId(), true))
                    break;
            } else // Allowing water allows the use of gates as flood gates
                if (!canPassThrough(block.getTypeId()))
                    break;

            // bag.setBlockID(w, x, y1, z, ID);
            if (plugin.getConfiguration().safeDestruction) {
                if (!close || hasEnoughBlocks(sign, otherSign)) {
                    if (!close && isValidGateBlock(block, true))
                        addBlocks(sign, 1);
                    else if (close && canPassThrough(block.getTypeId()) && isValidGateItem(new ItemStack(ID, 1), true))
                        removeBlocks(sign, 1);
                    block.setTypeIdAndData(ID, data, true);

                    setBlocks(sign, getBlocks(sign, otherSign));
                } else if (close && !hasEnoughBlocks(sign, otherSign) && isValidGateItem(new ItemStack(ID, 1), true))
                    if (player != null) {
                        player.printError("mech.not-enough-blocks");
                        return false;
                    }
            } else
                block.setTypeIdAndData(ID, data, true);

            CraftBookPlugin.logDebugMessage("Set block " + bl.getX() + ":" + bl.getY() + ":" + bl.getZ() + " to " + ID + ":" + data, "gates.search");

            WorldVector pt = new BlockWorldVector(column.getStartingPoint(), bl.getBlockX(), bl.getBlockY(), bl.getBlockZ());
            recurseColumn(player, new BlockWorldVector(column.getStartingPoint(), pt.add(1, 0, 0)), visitedColumns, close);
            recurseColumn(player, new BlockWorldVector(column.getStartingPoint(), pt.add(-1, 0, 0)), visitedColumns, close);
            recurseColumn(player, new BlockWorldVector(column.getStartingPoint(), pt.add(0, 0, 1)), visitedColumns, close);
            recurseColumn(player, new BlockWorldVector(column.getStartingPoint(), pt.add(0, 0, -1)), visitedColumns, close);
        }

        recurseColumn(player, new BlockWorldVector(column.getStartingPoint(), column.getStartingPoint().add(1, 0, 0)), visitedColumns, close);
        recurseColumn(player, new BlockWorldVector(column.getStartingPoint(), column.getStartingPoint().add(-1, 0, 0)), visitedColumns, close);
        recurseColumn(player, new BlockWorldVector(column.getStartingPoint(), column.getStartingPoint().add(0, 0, 1)), visitedColumns, close);
        recurseColumn(player, new BlockWorldVector(column.getStartingPoint(), column.getStartingPoint().add(0, 0, -1)), visitedColumns, close);

        recurseColumn(player, new BlockWorldVector(column.getStartingPoint(), column.getStartingPoint().add(1, 1, 0)), visitedColumns, close);
        recurseColumn(player, new BlockWorldVector(column.getStartingPoint(), column.getStartingPoint().add(-1, 1, 0)), visitedColumns, close);
        recurseColumn(player, new BlockWorldVector(column.getStartingPoint(), column.getStartingPoint().add(0, 1, 1)), visitedColumns, close);
        recurseColumn(player, new BlockWorldVector(column.getStartingPoint(), column.getStartingPoint().add(0, 1, -1)), visitedColumns, close);
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

        ChangedSign sign = BukkitUtil.toChangedSign(event.getClickedBlock());

        if (sign == null) return;

        if (plugin.getConfiguration().safeDestruction && getGateBlock() == player.getHeldItemType() && isValidGateBlock(getGateBlock(), false)) {

            if (!player.hasPermission("craftbook.mech.gate.restock")) {
                if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                    player.printError("mech.restock-permission");
                return;
            }

            int amount = 1;
            if (event.getPlayer().isSneaking())
                amount = Math.min(5, event.getPlayer().getItemInHand().getAmount());
            addBlocks(sign, amount);

            if (!(event.getPlayer().getGameMode() == GameMode.CREATIVE))
                if (event.getPlayer().getItemInHand().getAmount() <= amount)
                    event.getPlayer().setItemInHand(null);
                else
                    event.getPlayer().getItemInHand().setAmount(event.getPlayer().getItemInHand().getAmount() - amount);

            player.print("mech.restock");
            event.setCancelled(true);
            return;
        }

        if (!player.hasPermission("craftbook.mech.gate.use")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.use-permission");
            return;
        }

        if (toggleGates(player, pt, smallSearchSize, null))
            player.print("mech.gate.toggle");
        else
            player.printError("mech.gate.not-found");

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

        if (event.isMinor()) return;

        plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {

            @Override
            public void run() {

                toggleGates(null, pt, smallSearchSize, event.getNewCurrent() > 0);
            }
        }, 2);
    }

    public static class Factory extends AbstractMechanicFactory<Gate> {

        @Override
        public Gate detect(BlockWorldVector pt) {

            Block block = BukkitUtil.toWorld(pt).getBlockAt(BukkitUtil.toLocation(pt));
            if (block.getTypeId() == BlockID.WALL_SIGN || block.getTypeId() == BlockID.SIGN_POST) {
                ChangedSign sign = BukkitUtil.toChangedSign(block);
                if (sign.getLine(1).equalsIgnoreCase("[Gate]") || sign.getLine(1).equalsIgnoreCase("[DGate]"))
                    // this is a little funky because we don't actually look for the blocks that make up the movable
                    // parts of the gate until we're running the event later... so the factory can succeed even if
                    // the signpost doesn't actually operate any gates correctly. but it works!
                    return new Gate(pt, sign.getLine(1).equalsIgnoreCase("[DGate]"));
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
                        if (!isValidGateBlock(Integer.parseInt(line0)))
                            throw new NumberFormatException();
                    } catch (NumberFormatException e) {
                        throw new InvalidMechanismException("Line 1 needs to be a valid block id.");
                    }
                }
                sign.setLine(1, "[Gate]");
                if (sign.getLine(3).equalsIgnoreCase("infinite") && !player.hasPermission("craftbook.mech.gate.infinite"))
                    sign.setLine(3, "0");
                else if (!sign.getLine(3).equalsIgnoreCase("infinite"))
                    sign.setLine(3, "0");
                sign.update(false);
                player.print("mech.gate.create");
            } else if (sign.getLine(1).equalsIgnoreCase("[DGate]")) {
                if (!player.hasPermission("craftbook.mech.gate") && !player.hasPermission("craftbook.mech.dgate"))
                    throw new InsufficientPermissionsException();
                // get the material that this gate should toggle and verify it
                String line0 = sign.getLine(0).trim();
                if (line0 != null && !line0.isEmpty()) {
                    try {
                        if (!isValidGateBlock(Integer.parseInt(line0)))
                            throw new NumberFormatException();
                    } catch (NumberFormatException e) {
                        throw new InvalidMechanismException("Line 1 needs to be a valid block id.");
                    }
                }
                sign.setLine(1, "[DGate]");
                if (sign.getLine(3).equalsIgnoreCase("infinite") && !player.hasPermission("craftbook.mech.gate.infinite"))
                    sign.setLine(3, "0");
                else if (!sign.getLine(3).equalsIgnoreCase("infinite"))
                    sign.setLine(3, "0");
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

        ChangedSign sign = BukkitUtil.toChangedSign(b);
        int type;

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
        } else if(check && (type = getGateBlock()) != 0)
            return block == type;
        else
            return plugin.getConfiguration().gateBlocks.contains(block);
    }

    public boolean isValidGateItem(ItemStack block, boolean check) {

        return isValidGateItem(block.getTypeId(), check);
    }

    public boolean isValidGateItem(int block, boolean check) {

        Block b = BukkitUtil.toWorld(pt).getBlockAt(BukkitUtil.toLocation(pt));

        ChangedSign sign = BukkitUtil.toChangedSign(b);
        int type;

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

        ChangedSign sign = BukkitUtil.toChangedSign(event.getBlock());

        if (sign == null) return;

        int amount = getBlocks(sign);
        if (amount > 0) {
            int type = getGateBlock();
            if(type <= 0)
                type = BlockID.FENCE;
            ItemStack toDrop = new ItemStack(type, amount);
            event.getBlock().getWorld().dropItemNaturally(BlockUtil.getBlockCentre(event.getBlock()), toDrop);
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
            for (int x1 = x - CraftBookPlugin.inst().getConfiguration().gateSearchRadius; x1 <= x + CraftBookPlugin.inst().getConfiguration().gateSearchRadius; x1++) {
                for (int y1 = y - CraftBookPlugin.inst().getConfiguration().gateSearchRadius; y1 <= y + CraftBookPlugin.inst().getConfiguration().gateSearchRadius*2; y1++) {
                    for (int z1 = z - CraftBookPlugin.inst().getConfiguration().gateSearchRadius; z1 <= z + CraftBookPlugin.inst().getConfiguration().gateSearchRadius; z1++) {
                        if (getFirstBlock(new WorldVector(world, x1, y1, z1)) != 0) {
                            gateBlock = getFirstBlock(new WorldVector(world, x1, y1, z1));
                        }
                    }
                }
            }
        }

        if(plugin.getConfiguration().gateEnforceType && gateBlock != 0) {
            sign.setLine(0, String.valueOf(gateBlock));
            sign.update(false);
        }

        return gateBlock;
    }

    public int getFirstBlock(WorldVector pt) {

        World world = ((BukkitWorld) pt.getWorld()).getWorld();
        if (!isValidGateBlock(world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()), false)) return 0;

        return world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).getTypeId();
    }

    public boolean removeBlocks(ChangedSign s, int amount) {

        if (s.getLine(3).equalsIgnoreCase("infinite")) return true;
        int curBlocks = getBlocks(s) - amount;
        s.setLine(3, String.valueOf(curBlocks));
        s.update(false);
        return curBlocks >= 3;
    }

    public boolean addBlocks(ChangedSign s, int amount) {

        if (s.getLine(3).equalsIgnoreCase("infinite")) return true;
        int curBlocks = getBlocks(s) + amount;
        s.setLine(3, String.valueOf(curBlocks));
        s.update(false);
        return curBlocks >= 0;
    }

    public void setBlocks(ChangedSign s, int amount) {

        if (s.getLine(3).equalsIgnoreCase("infinite")) return;
        s.setLine(3, String.valueOf(amount));
        s.update(false);
    }

    public int getBlocks(ChangedSign s) {

        if (s.getLine(3).equalsIgnoreCase("infinite")) return 0;
        return getBlocks(s, null);
    }

    public int getBlocks(ChangedSign s, ChangedSign other) {

        if (s.getLine(3).equalsIgnoreCase("infinite") || other != null && other.getLine(3).equalsIgnoreCase("infinite"))
            return 0;
        int curBlocks = 0;
        try {
            curBlocks = Integer.parseInt(s.getLine(3));
            if(other != null) {
                try {
                    curBlocks += Integer.parseInt(other.getLine(3));
                    setBlocks(s, curBlocks);
                    setBlocks(other, 0);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            curBlocks = 0;
        }
        return curBlocks;
    }

    public boolean hasEnoughBlocks(ChangedSign s) {

        return s.getLine(3).equalsIgnoreCase("infinite") || getBlocks(s) > 0;
    }

    public boolean hasEnoughBlocks(ChangedSign s, ChangedSign other) {

        return s != null && s.getLine(3).equalsIgnoreCase("infinite") || other != null && other.getLine(3).equalsIgnoreCase("infinite") || getBlocks(s, other) > 0;
    }

    protected class GateColumn {

        private final BlockWorldVector bwv;

        public GateColumn(LocalWorld world, int x, int y, int z) {

            bwv = new BlockWorldVector(world, x, y, z);
        }

        public BlockWorldVector getStartingPoint() {

            return new BlockWorldVector(bwv.getWorld(), bwv.getBlockX(), getStartingY(), bwv.getBlockZ());
        }

        public BlockWorldVector getEndingPoint() {

            return new BlockWorldVector(bwv.getWorld(), bwv.getBlockX(), getEndingY(), bwv.getBlockZ());
        }

        public int getStartingY() {

            int curY = bwv.getBlockY();
            int maxY = Math.min(BukkitUtil.toWorld(bwv.getWorld()).getMaxHeight(), bwv.getBlockY() + CraftBookPlugin.inst().getConfiguration().gateColumnHeight);
            for (int y1 = bwv.getBlockY() + 1; y1 <= maxY; y1++) {
                if (isValidGateBlock(BukkitUtil.toWorld(bwv.getWorld()).getBlockAt(bwv.getBlockX(), y1, bwv.getBlockZ()), true))
                    curY = y1;
                else
                    break;
            }

            return curY;
        }

        public int getEndingY() {

            int minY = Math.max(0, bwv.getBlockY() - CraftBookPlugin.inst().getConfiguration().gateColumnHeight);
            for (int y = bwv.getBlockY(); y >= minY; y--)
                if (!canPassThrough(bwv.getWorld().getBlockType(new Vector(bwv.getX(), y, bwv.getZ())))) return y + 1;
            return 0;
        }

        public int getX() {

            return bwv.getBlockX();
        }

        public int getZ() {

            return bwv.getBlockZ();
        }

        public CuboidRegion getRegion() {

            return new CuboidRegion(getStartingPoint().subtract(0, 1, 0), getEndingPoint());
        }

        @Override
        public boolean equals(Object o) {

            if(!(o instanceof GateColumn)) return false;
            return ((GateColumn) o).getX() == getX() && ((GateColumn) o).getZ() == getZ();
        }

        @Override
        public int hashCode() {
            // Constants correspond to glibc's lcg algorithm parameters
            return (getX() * 1103515245 + 12345 ^ getZ() * 1103515245 + 12345) * 1103515245 + 12345;
        }
    }
}