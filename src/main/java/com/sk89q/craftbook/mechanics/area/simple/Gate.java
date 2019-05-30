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

package com.sk89q.craftbook.mechanics.area.simple;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.util.BlockSyntax;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ProtectionUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.events.SignClickEvent;
import com.sk89q.craftbook.util.events.SourcedBlockRedstoneEvent;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.blocks.Blocks;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Handler for gates. Gates are merely fence blocks. When they are closed or open, a nearby fence will be found,
 * the algorithm will traverse to the
 * top-most connected fence block, and then proceed to recurse to the sides up to a certain number of fences. To the
 * fences that it gets to, it will
 * iterate over the blocks below to open or close the gate.
 *
 * @author sk89q
 */
public class Gate extends AbstractCraftBookMechanic {

    /**get
     * Toggles the gate closest to a location.
     *
     * @param player The player
     * @param block The base block
     * @param smallSearchSize Small or large search radius
     * @param close null to toggle, true to close, false to open
     *
     * @return true if a gate was found and blocks were changed; false otherwise.
     */
    public boolean toggleGates(CraftBookPlayer player, Block block, boolean smallSearchSize, Boolean close) {

        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        boolean foundGate = false;

        Set<GateColumn> visitedColumns = new HashSet<>();

        ChangedSign sign = CraftBookBukkitUtil.toChangedSign(block);

        if (smallSearchSize) {
            // Toggle nearby gates
            for (int x1 = x - 1; x1 <= x + 1; x1++) {
                for (int y1 = y - 2; y1 <= y + 1; y1++) {
                    for (int z1 = z - 1; z1 <= z + 1; z1++) {
                        if (recurseColumn(player, sign, block.getWorld().getBlockAt(x1, y1, z1), visitedColumns, close, true)) {
                            foundGate = true;
                        }
                    }
                }
            }
        } else {
            // Toggle nearby gates
            for (int x1 = x - searchRadius; x1 <= x + searchRadius; x1++) {
                for (int y1 = y - searchRadius; y1 <= y + searchRadius*2; y1++) {
                    for (int z1 = z - searchRadius; z1 <= z + searchRadius; z1++) {
                        if (recurseColumn(player, sign, block.getWorld().getBlockAt(x1, y1, z1), visitedColumns, close, false)) {
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
     * @param player The Player
     * @param sign The sign block.
     * @param block A part of the column.
     * @param visitedColumns Previously visited columns.
     * @param close Should close or open.
     *
     * @return true if a gate column was found and blocks were changed; false otherwise.
     */
    private boolean recurseColumn(CraftBookPlayer player, ChangedSign sign, Block block, Set<GateColumn> visitedColumns, Boolean close, boolean smallSearchSize) {

        if (limitColumns && visitedColumns.size() > columnLimit)
            return false;

        if (!isValidGateBlock(sign, smallSearchSize, BukkitAdapter.adapt(block.getBlockData()), true)) return false;

        CraftBookPlugin.logDebugMessage("Found a possible gate column at " + block.getX() + ':' + block.getY() + ':' + block.getZ(), "gates.search");

        int x = block.getX();
        int z = block.getZ();

        GateColumn column = new GateColumn(sign, block, smallSearchSize);

        // The block above the gate cannot be air -- it has to be some
        // non-fence block
        if (BlockUtil.isAir(block.getWorld().getBlockAt(x, column.getStartingY() + 1, z).getType())) return false;

        if (visitedColumns.contains(column)) return false;

        visitedColumns.add(column);

        if (close == null)
            close = !isValidGateBlock(sign, smallSearchSize, BukkitAdapter.adapt(block.getWorld().getBlockAt(x, column.getStartingY() - 1, z).getBlockData()), true);

        CraftBookPlugin.logDebugMessage("Valid column at " + block.getX() + ':' + block.getY() + ':' + block.getZ() + " is being " + (close ? "closed" : "opened"), "gates.search");
        CraftBookPlugin.logDebugMessage("Column Top: " + column.getStartingY() + " End: " + column.getEndingY(), "gates.search");
        // Recursively go to connected fence blocks of the same level
        // and 'close' or 'open' them
        return toggleColumn(player, sign, block, column, close, visitedColumns, smallSearchSize);
    }

    /**
     * Actually does the closing/opening. Also recurses to nearby columns.
     *
     * @param player The player.
     * @param sign The sign block.
     * @param block The top point of the gate.
     * @param close To open or close.
     * @param visitedColumns Previously searched columns.
     */
    private boolean toggleColumn(CraftBookPlayer player, ChangedSign sign, Block block, GateColumn column, boolean close, Set<GateColumn> visitedColumns, boolean smallSearchSize) {

        // If we want to close the gate then we replace air/water blocks
        // below with fence blocks; otherwise, we want to replace fence
        // blocks below with air
        BlockData item;
        if (close)
            item = column.getStartingPoint().getBlockData();
        else
            item = Material.AIR.createBlockData();

        CraftBookPlugin.logDebugMessage("Setting column at " + block.getX() + ':' + block.getY() + ':' + block.getZ() + " to " + item.toString(), "gates.search");

        if(sign == null) {
            CraftBookPlugin.logDebugMessage("Invalid Sign!", "gates.search");
            return false;
        }

        ChangedSign otherSign = null;

        Block ot = SignUtil.getNextSign(CraftBookBukkitUtil.toSign(sign).getBlock(), sign.getLine(1), 4);
        if(ot != null) {
            otherSign = CraftBookBukkitUtil.toChangedSign(ot);
        }

        for (BlockVector3 bl : column.getRegion()) {
            Block blo = CraftBookBukkitUtil.toLocation(block.getWorld(), bl.toVector3()).getBlock();

            if (sign.getLine(2).equalsIgnoreCase("NoReplace")) {
                // If NoReplace is on line 3 of sign, do not replace blocks.
                if (blo.getType() != Material.AIR && !isValidGateBlock(sign, smallSearchSize, BukkitAdapter.adapt(blo.getBlockData()), true))
                    break;
            } else // Allowing water allows the use of gates as flood gates
                if (!canPassThrough(sign, smallSearchSize, BukkitAdapter.adapt(blo.getBlockData())))
                    break;

            // bag.setBlockID(w, x, y1, z, ID);
            if (CraftBookPlugin.inst().getConfiguration().safeDestruction) {
                boolean hasBlocks = hasEnoughBlocks(sign, otherSign);
                if (!close || hasBlocks) {
                    if (!close && isValidGateBlock(sign, smallSearchSize, BukkitAdapter.adapt(blo.getBlockData()), true)) {
                        addBlocks(sign, 1);
                    } else if (close && canPassThrough(sign, smallSearchSize, BukkitAdapter.adapt(blo.getBlockData())) && isValidGateBlock(sign,
                            smallSearchSize, BukkitAdapter.adapt(item), true) && item.getMaterial() != blo.getType()) {
                        removeBlocks(sign, 1);
                    }
                    blo.setBlockData(item, true);
                } else if (isValidGateBlock(sign, smallSearchSize, BukkitAdapter.adapt(item), true))
                    if (player != null) {
                        player.printError("mech.not-enough-blocks");
                        return false;
                    }
            } else
                blo.setBlockData(item, true);

            CraftBookPlugin.logDebugMessage("Set block " + bl.getX() + ':' + bl.getY() + ':' + bl.getZ() + " to " + item.toString(), "gates.search");

            recurseColumn(player, sign, blo.getRelative(1, 0, 0), visitedColumns, close, smallSearchSize);
            recurseColumn(player, sign, blo.getRelative(-1, 0, 0), visitedColumns, close, smallSearchSize);
            recurseColumn(player, sign, blo.getRelative(0, 0, 1), visitedColumns, close, smallSearchSize);
            recurseColumn(player, sign, blo.getRelative(0, 0, -1), visitedColumns, close, smallSearchSize);
        }

        recurseColumn(player, sign, column.getStartingPoint().getRelative(1, 0, 0), visitedColumns, close, smallSearchSize);
        recurseColumn(player, sign, column.getStartingPoint().getRelative(-1, 0, 0), visitedColumns, close, smallSearchSize);
        recurseColumn(player, sign, column.getStartingPoint().getRelative(0, 0, 1), visitedColumns, close, smallSearchSize);
        recurseColumn(player, sign, column.getStartingPoint().getRelative(0, 0, -1), visitedColumns, close, smallSearchSize);

        recurseColumn(player, sign, column.getStartingPoint().getRelative(1, 1, 0), visitedColumns, close, smallSearchSize);
        recurseColumn(player, sign, column.getStartingPoint().getRelative(-1, 1, 0), visitedColumns, close, smallSearchSize);
        recurseColumn(player, sign, column.getStartingPoint().getRelative(0, 1, 1), visitedColumns, close, smallSearchSize);
        recurseColumn(player, sign, column.getStartingPoint().getRelative(0, 1, -1), visitedColumns, close, smallSearchSize);
        return true;
    }

    /**
     * Raised when a block is right clicked.
     *
     * @param event SignClickEvent
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(SignClickEvent event) {
        if (!EventUtil.passesFilter(event))
            return;

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        ChangedSign sign = event.getSign();

        if (!sign.getLine(1).equals("[Gate]") && !sign.getLine(1).equals("[DGate]")) return;

        boolean smallSearchSize = sign.getLine(1).equals("[DGate]");

        BlockState gateBlock = getGateBlock(sign, smallSearchSize);

        if (CraftBookPlugin.inst().getConfiguration().safeDestruction) {
            if (player.getItemInHand(HandSide.MAIN_HAND).getType().hasBlockType()) {
                BlockType heldType = player.getItemInHand(HandSide.MAIN_HAND).getType().getBlockType();
                if ((gateBlock == null || gateBlock.getBlockType().getMaterial().isAir() || gateBlock.getBlockType() == heldType)
                        && isValidGateBlock(sign, smallSearchSize, heldType.getDefaultState(), false)) {
                    if (!player.hasPermission("craftbook.mech.gate.restock")) {
                        if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                            player.printError("mech.restock-permission");
                        return;
                    }

                    int amount = 1;
                    if (event.getPlayer().isSneaking())
                        amount = Math.min(5, event.getPlayer().getItemInHand().getAmount());
                    addBlocks(sign, amount);

                    if (enforceType) {
                        BlockType blockType = player.getItemInHand(HandSide.MAIN_HAND).getType().getBlockType();
                        sign.setLine(0, BlockSyntax.toMinifiedId(blockType));
                        sign.update(false);
                    }

                    if (!(event.getPlayer().getGameMode() == GameMode.CREATIVE))
                        if (event.getPlayer().getItemInHand().getAmount() <= amount)
                            event.getPlayer().setItemInHand(null);
                        else
                            event.getPlayer().getItemInHand().setAmount(event.getPlayer().getItemInHand().getAmount() - amount);

                    player.print("mech.restock");
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (!player.hasPermission("craftbook.mech.gate.use")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.use-permission");
            return;
        }

        if(!ProtectionUtil.canUse(event.getPlayer(), event.getClickedBlock().getLocation(), event.getBlockFace(), event.getAction())) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("area.use-permissions");
            return;
        }

        if (toggleGates(player, event.getClickedBlock(), smallSearchSize, null))
            player.print("mech.gate.toggle");
        else
            player.printError("mech.gate.not-found");

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(final SourcedBlockRedstoneEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (!allowRedstone) return;

        if (event.isMinor()) return;

        if (!SignUtil.isSign(event.getBlock())) return;

        final ChangedSign sign = CraftBookBukkitUtil.toChangedSign(event.getBlock());
        if (!sign.getLine(1).equals("[Gate]") && !sign.getLine(1).equals("[DGate]")) return;

        CraftBookPlugin.inst().getServer().getScheduler().runTaskLater(CraftBookPlugin.inst(),
                () -> toggleGates(null, event.getBlock(), sign.getLine(1).equals("[DGate]"), event.getNewCurrent() > 0), 2);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(!event.getLine(1).equalsIgnoreCase("[Gate]") && !event.getLine(1).equalsIgnoreCase("[DGate]")) return;

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (event.getLine(1).equalsIgnoreCase("[Gate]")) {
            if(!player.hasPermission("craftbook.mech.gate")) {
                if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                    player.printError("mech.create-permission");
                SignUtil.cancelSign(event);
                return;
            }
            // get the material that this gate should toggle and verify it
            String line0 = event.getLine(0).trim();
            if (!line0.isEmpty()) {
                if (!isValidGateBlock(BlockSyntax.getBlock(line0, true))) {
                    player.printError("Line 1 needs to be a valid block id.");
                    SignUtil.cancelSign(event);
                    return;
                }
            }
            event.setLine(1, "[Gate]");
            if (event.getLine(3).equalsIgnoreCase("infinite") && !player.hasPermission("craftbook.mech.gate.infinite"))
                event.setLine(3, "0");
            else if (!event.getLine(3).equalsIgnoreCase("infinite"))
                event.setLine(3, "0");
            player.print("mech.gate.create");
        } else if (event.getLine(1).equalsIgnoreCase("[DGate]")) {
            if (!player.hasPermission("craftbook.mech.gate") && !player.hasPermission("craftbook.mech.dgate")) {
                if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                    player.printError("mech.create-permission");
                SignUtil.cancelSign(event);
                return;
            }
            // get the material that this gate should toggle and verify it
            String line0 = event.getLine(0).trim();
            if (!line0.isEmpty()) {
                if (!isValidGateBlock(BlockSyntax.getBlock(line0, true))) {
                    player.printError("mech.gate.valid-item");
                    SignUtil.cancelSign(event);
                    return;
                }
            }
            event.setLine(1, "[DGate]");
            if (event.getLine(3).equalsIgnoreCase("infinite") && !player.hasPermission("craftbook.mech.gate.infinite"))
                event.setLine(3, "0");
            else if (!event.getLine(3).equalsIgnoreCase("infinite"))
                event.setLine(3, "0");
            player.print("mech.dgate.create");
        }
    }

    public boolean isValidGateBlock(BlockStateHolder block) {
        return Blocks.containsFuzzy(blocks, block);
    }

    /**
     * Checks if a block can be used in gate.
     * 
     * @param sign The sign block.
     * @param smallSearchSize Search small or large.
     * @param block The block to check.
     * @param check Should search.
     * @return
     */
    public boolean isValidGateBlock(ChangedSign sign, boolean smallSearchSize, BlockStateHolder<?> block, boolean check) {
        BlockState type;

        if (sign != null && !sign.getLine(0).isEmpty()) {
            try {
                BlockStateHolder<?> def = BlockSyntax.getBlock(sign.getLine(0), true);
                return block.equalsFuzzy(def);
            } catch (Exception e) {
                if (check) {
                    type = getGateBlock(sign, smallSearchSize);
                    if(type == null || type.getBlockType().getMaterial().isAir())
                        return block.getBlockType().getMaterial().isAir();
                }
                return isValidGateBlock(block);
            }
        } else if(check && (type = getGateBlock(sign, smallSearchSize)) != null)
            return block.equalsFuzzy(type);
        else
            return isValidGateBlock(block);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (!SignUtil.isSign(event.getBlock())) return;

        final ChangedSign sign = CraftBookBukkitUtil.toChangedSign(event.getBlock());
        if (!sign.getLine(1).equals("[Gate]") && !sign.getLine(1).equals("[DGate]")) return;

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if(!ProtectionUtil.canBuild(event.getPlayer(), event.getBlock().getLocation(), false)) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("area.break-permissions");
            return;
        }

        int amount = getBlocks(sign);
        if (amount > 0) {
            BlockState type = getGateBlock(sign, sign.getLine(1).equals("[DGate]"));
            if(type == null || type.getBlockType().getMaterial().isAir())
                type = BlockTypes.OAK_FENCE.getFuzzyMatcher();
            ItemStack toDrop = new ItemStack(BukkitAdapter.adapt(type.getBlockType()), amount);
            event.getBlock().getWorld().dropItemNaturally(BlockUtil.getBlockCentre(event.getBlock()), toDrop);
        }
    }

    private boolean canPassThrough(ChangedSign sign, boolean smallSearchSize, BlockStateHolder t) {

        if (!t.getBlockType().getMaterial().isMovementBlocker()) {
            return true;
        }

        return isValidGateBlock(sign, smallSearchSize, t, true);
    }

    public BlockState getGateBlock(ChangedSign sign, boolean smallSearchSize) {
        BlockState gateBlock = null;

        if (sign != null) {
            if(!sign.getLine(0).isEmpty()) {
                try {
                    return BlockSyntax.getBlock(sign.getLine(0), true).toImmutableState();
                } catch (Exception ignored) {
                }
            }

            int x = sign.getX();
            int y = sign.getY();
            int z = sign.getZ();

            if (smallSearchSize) {
                for (int x1 = x - 1; x1 <= x + 1; x1++) {
                    for (int y1 = y - 2; y1 <= y + 1; y1++) {
                        for (int z1 = z - 1; z1 <= z + 1; z1++) {
                            if (getFirstBlock(sign, sign.getBlock().getWorld().getBlockAt(x1, y1, z1), true) != null) {
                                gateBlock = BukkitAdapter.adapt(getFirstBlock(sign, sign.getBlock().getWorld().getBlockAt(x1, y1, z1), true).getBlockData());
                                break;
                            }
                        }
                    }
                }
            } else {
                for (int x1 = x - searchRadius; x1 <= x + searchRadius; x1++) {
                    for (int y1 = y - searchRadius; y1 <= y + searchRadius*2; y1++) {
                        for (int z1 = z - searchRadius; z1 <= z + searchRadius; z1++) {
                            if (getFirstBlock(sign, sign.getBlock().getWorld().getBlockAt(x1, y1, z1), false) != null) {
                                gateBlock = BukkitAdapter.adapt(getFirstBlock(sign, sign.getBlock().getWorld().getBlockAt(x1, y1, z1), false).getBlockData());
                                break;
                            }
                        }
                    }
                }
            }

            if(enforceType && gateBlock != null && !gateBlock.getBlockType().getMaterial().isAir()) {
                sign.setLine(0, BlockSyntax.toMinifiedId(gateBlock.getBlockType()));
                sign.update(false);
            }
        }

        return gateBlock;
    }

    public Block getFirstBlock(ChangedSign sign, Block block, boolean smallSearchSize) {

        if (!isValidGateBlock(sign, smallSearchSize, BukkitAdapter.adapt(block.getBlockData()), false)) return null;

        return block;
    }

    public void removeBlocks(ChangedSign s, int amount) {

        if (s.getLine(3).equalsIgnoreCase("infinite")) return;
        setBlocks(s, getBlocks(s) - amount);
    }

    public void addBlocks(ChangedSign s, int amount) {

        if (s.getLine(3).equalsIgnoreCase("infinite")) return;
        setBlocks(s, getBlocks(s) + amount);
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
        int curBlocks;
        try {
            curBlocks = Integer.parseInt(s.getLine(3));
            if(other != null && other.getLine(0).equals(s.getLine(0))) {
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

        if(other == null)
            return hasEnoughBlocks(s);
        else
            return s.getLine(3).equalsIgnoreCase("infinite") || other.getLine(3).equalsIgnoreCase("infinite") || getBlocks(s, other) > 0;
    }

    protected class GateColumn {

        private final ChangedSign sign;
        private final Block block;
        private final boolean smallSearchSize;

        private int minY = -1, maxY = -1;
        private int remainingColumnHeight;

        public GateColumn(ChangedSign sign, Block block, boolean smallSearchSize) {

            this.sign = sign;
            this.block = block;
            this.smallSearchSize = smallSearchSize;

            remainingColumnHeight = columnHeight;
        }

        public Block getStartingPoint() {

            return block.getWorld().getBlockAt(block.getX(), getStartingY(), block.getZ());
        }

        public Block getEndingPoint() {

            return block.getWorld().getBlockAt(block.getX(), getEndingY(), block.getZ());
        }

        public int getStartingY() {

            if(maxY == -1) {
                int max = Math.min(block.getWorld().getMaxHeight()-1, block.getY() + remainingColumnHeight);
                for (int y1 = block.getY() + 1; y1 <= max; y1++) {
                    if(remainingColumnHeight <= 0) break;
                    if (isValidGateBlock(sign, smallSearchSize, BukkitAdapter.adapt(block.getWorld().getBlockAt(block.getX(), y1, block.getZ()).getBlockData()), true)) {
                        maxY = y1;
                        remainingColumnHeight --;
                    } else
                        break;
                }

                if(maxY == -1) maxY = block.getY();
            }

            return maxY;
        }

        public int getEndingY() {

            if(minY == -1) {
                int min = Math.max(0, block.getY() - remainingColumnHeight);
                for (int y = block.getY(); y >= min; y--) {
                    if(remainingColumnHeight <= 0) break;
                    BlockState currentBlock = BukkitAdapter.adapt(block.getWorld().getBlockAt(block.getX(), y, block.getZ()).getBlockData());
                    if (canPassThrough(sign, smallSearchSize, currentBlock)
                            || isValidGateBlock(sign, smallSearchSize, currentBlock, true)) {
                        minY = y;
                        remainingColumnHeight --;
                    } else
                        break;
                }
                if(minY == -1) minY = block.getY();
            }

            return minY;
        }

        public int getX() {

            return block.getX();
        }

        public int getZ() {

            return block.getZ();
        }

        public CuboidRegion getRegion() {
            return new CuboidRegion(
                    BukkitAdapter.adapt(getStartingPoint().getRelative(0, -1, 0).getLocation()).toVector().toBlockPoint(),
                    BukkitAdapter.adapt(getEndingPoint().getLocation()).toVector().toBlockPoint()
            );
        }

        @Override
        public boolean equals(Object o) {

            return o instanceof GateColumn && ((GateColumn) o).getX() == getX() && ((GateColumn) o).getZ() == getZ() && block.getWorld().getName().equals(((GateColumn) o).block.getWorld().getName());
        }

        @Override
        public int hashCode() {
            // Constants correspond to glibc's lcg algorithm parameters
            return (getX() * 1103515245 + 12345 ^ getZ() * 1103515245 + 12345) * 1103515245 + 12345;
        }
    }

    private boolean allowRedstone;
    private boolean limitColumns;
    private int columnLimit;
    private List<BaseBlock> blocks;
    private boolean enforceType;
    private int columnHeight;
    private int searchRadius;

    public List<String> getDefaultBlocks() {
        List<String> materials = new ArrayList<>();
        materials.add(BlockTypes.ACACIA_FENCE.getId());
        materials.add(BlockTypes.BIRCH_FENCE.getId());
        materials.add(BlockTypes.JUNGLE_FENCE.getId());
        materials.add(BlockTypes.OAK_FENCE.getId());
        materials.add(BlockTypes.SPRUCE_FENCE.getId());
        materials.add(BlockTypes.DARK_OAK_FENCE.getId());
        materials.add(BlockTypes.NETHER_BRICK_FENCE.getId());
        materials.add(BlockTypes.IRON_BARS.getId());
        materials.add(BlockTypes.GLASS_PANE.getId());
        return materials;
    }

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "allow-redstone", "Allows the gate mechanic to be toggled via redstone.");
        allowRedstone = config.getBoolean(path + "allow-redstone", true);

        config.setComment(path + "limit-columns", "Limit the amount of columns a gate can toggle.");
        limitColumns = config.getBoolean(path + "limit-columns", true);

        config.setComment(path + "max-columns", "If limit-columns is enabled, the maximum number of columns that a gate can toggle.");
        columnLimit = config.getInt(path + "max-columns", 14);

        config.setComment(path + "blocks", "The list of blocks that a gate can use.");
        blocks = BlockSyntax.getBlocks(config.getStringList(path + "blocks", getDefaultBlocks()), true);

        config.setComment(path + "enforce-type", "Make sure gates are only able to toggle a specific material type. This prevents transmutation.");
        enforceType = config.getBoolean(path + "enforce-type", true);

        config.setComment(path + "max-column-height", "The max height of a column.");
        columnHeight = config.getInt(path + "max-column-height", 12);

        config.setComment(path + "gate-search-radius", "The radius around the sign the gate checks for fences in. Note: This is doubled upwards.");
        searchRadius = config.getInt(path + "gate-search-radius", 3);
    }
}
