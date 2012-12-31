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

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.bukkit.BukkitPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.exceptions.InvalidMechanismException;
import com.sk89q.craftbook.util.exceptions.ProcessedMechanismException;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * The default bridge mechanism -- signposts on either side of a 3xN plane of (or 1xN plane if 1 on second line) blocks.
 *
 * @author hash
 */
public class Bridge extends AbstractMechanic {

    public static class Factory extends AbstractMechanicFactory<Bridge> {

        public Factory() {

        }

        /**
         * Explore around the trigger to find a Bridge; throw if things look funny.
         *
         * @param pt the trigger (should be a signpost)
         *
         * @return a Bridge if we could make a valid one, or null if this looked nothing like a bridge.
         *
         * @throws InvalidMechanismException if the area looked like it was intended to be a bridge, but it failed.
         */
        @Override
        public Bridge detect(BlockWorldVector pt) throws InvalidMechanismException {

            Block block = BukkitUtil.toBlock(pt);
            // check if this looks at all like something we're interested in first
            if (block.getTypeId() != BlockID.SIGN_POST) return null;
            if (!BukkitUtil.toChangedSign(block).getLine(1).equalsIgnoreCase("[Bridge]")) return null;

            // okay, now we can start doing exploration of surrounding blocks
            // and if something goes wrong in here then we throw fits.
            return new Bridge(block);
        }

        /**
         * Detect the mechanic at a placed sign.
         *
         * @throws ProcessedMechanismException
         */
        @Override
        public Bridge detect(BlockWorldVector pt, LocalPlayer player,
                             ChangedSign sign) throws InvalidMechanismException,
                ProcessedMechanismException {

            if (sign.getLine(1).equalsIgnoreCase("[Bridge]")) {
                player.checkPermission("craftbook.mech.bridge");

                sign.setLine(1, "[Bridge]");
                if (sign.getLine(0).equalsIgnoreCase("infinite") && !player.hasPermission("craftbook.mech.bridge" + "" +
                        ".infinite")) {
                    sign.setLine(0, "0");
                } else if (!sign.getLine(0).equalsIgnoreCase("infinite")) {
                    sign.setLine(0, "0");
                }
                sign.update(false);
                player.print("mech.bridge.create");
            } else if (sign.getLine(1).equalsIgnoreCase("[Bridge End]")) {
                player.checkPermission("craftbook.mech.bridge");

                sign.setLine(1, "[Bridge End]");
                if (sign.getLine(0).equalsIgnoreCase("infinite") && !player.hasPermission("craftbook.mech.bridge" + "" +
                        ".infinite")) {
                    sign.setLine(0, "0");
                } else if (!sign.getLine(0).equalsIgnoreCase("infinite")) {
                    sign.setLine(0, "0");
                }
                sign.update(false);
                player.print("mech.bridge.end-create");
            } else return null;

            throw new ProcessedMechanismException();
        }
    }

    // Factory ends here

    /**
     * @param trigger if you didn't already check if this is a signpost with appropriate text,
     *                you're going on Santa's naughty list.
     *
     * @throws InvalidMechanismException
     */
    private Bridge(Block trigger) throws InvalidMechanismException {

        super();

        if (!SignUtil.isCardinal(trigger)) throw new InvalidDirectionException();
        BlockFace dir = SignUtil.getFacing(trigger);

        this.trigger = trigger;

        // Attempt to detect whether the bridge is above or below the sign,
        // first assuming that the bridge is above
        int mat;
        findBase:
        {
            proximalBaseCenter = trigger.getRelative(BlockFace.UP);
            mat = proximalBaseCenter.getTypeId();
            if (plugin.getConfiguration().bridgeBlocks.contains(mat) && isValidBridge(proximalBaseCenter, mat,
                    BukkitUtil.toChangedSign(trigger))) {
                break findBase; // On Top
            }

            // If we've reached this point nothing was found on the top, check the bottom
            proximalBaseCenter = trigger.getRelative(BlockFace.DOWN);
            mat = proximalBaseCenter.getTypeId();
            if (plugin.getConfiguration().bridgeBlocks.contains(mat)) {
                if (isValidBridge(proximalBaseCenter, mat, BukkitUtil.toChangedSign(trigger))) {
                    break findBase; // it's below
                } else throw new InvalidConstructionException("mech.bridge.material");
            } else throw new UnacceptableMaterialException("mech.bridge.unusable");
        }

        // Find the other side
        farSide = trigger.getRelative(dir);
        for (int i = 0; i <= plugin.getConfiguration().bridgeMaxLength; i++) {
            // about the loop index:
            // i = 0 is the first block after the proximal base
            // since we're allowed to have settings.maxLength toggle blocks,
            // i = settings.maxLength is actually the farthest place we're
            // allowed to find the distal signpost

            if (farSide.getTypeId() == BlockID.SIGN_POST) {
                String otherSignText = BukkitUtil.toChangedSign(farSide).getLine(1);
                if ("[Bridge]".equalsIgnoreCase(otherSignText) || "[Bridge End]".equalsIgnoreCase(otherSignText)) {
                    break;
                }
            }

            farSide = farSide.getRelative(dir);
        }
        if (farSide.getTypeId() != BlockID.SIGN_POST) throw new InvalidConstructionException("mech.bridge.other-sign");

        // Check the other side's base blocks for matching type
        Block distalBaseCenter = farSide.getRelative(trigger.getFace(proximalBaseCenter));
        if (distalBaseCenter.getTypeId() != mat && distalBaseCenter.getData() != proximalBaseCenter.getData()
                || distalBaseCenter.getRelative(SignUtil.getLeft(trigger)).getTypeId() != mat
                && distalBaseCenter.getRelative(SignUtil.getLeft(trigger)).getData() != proximalBaseCenter.getData()
                || distalBaseCenter.getRelative(SignUtil.getRight(trigger)).getTypeId() != mat
                && distalBaseCenter.getRelative(SignUtil.getRight(trigger)).getData() != proximalBaseCenter.getData())
            throw new InvalidConstructionException("mech.bridge.material");

        // Select the togglable region
        toggle = new CuboidRegion(BukkitUtil.toVector(proximalBaseCenter), BukkitUtil.toVector(distalBaseCenter));
        int left, right;
        try {
            left = Integer.parseInt(BukkitUtil.toChangedSign(trigger).getLine(2));
            if (left < 0) {
                left = 0; // No negatives please
            }
        } catch (Exception ignored) {
            left = 1;
        }
        try {
            right = Integer.parseInt(BukkitUtil.toChangedSign(trigger).getLine(3));
            if (right < 0) {
                right = 0; // No negatives please
            }
        } catch (Exception ignored) {
            right = 1;
        }

        // Check Width
        if (left > plugin.getConfiguration().bridgeMaxWidth) {
            left = plugin.getConfiguration().bridgeMaxWidth;
        }
        if (right > plugin.getConfiguration().bridgeMaxWidth) {
            right = plugin.getConfiguration().bridgeMaxWidth;
        }

        // Expand Left
        for (int i = 0; i < left; i++) {
            toggle.expand(BukkitUtil.toVector(SignUtil.getLeft(trigger)), new Vector(0, 0, 0));
        }

        // Expand Right
        for (int i = 0; i < right; i++) {
            toggle.expand(BukkitUtil.toVector(SignUtil.getRight(trigger)), new Vector(0, 0, 0));
        }

        // Don't toggle the end points
        toggle.contract(BukkitUtil.toVector(SignUtil.getBack(trigger)), BukkitUtil.toVector(SignUtil.getFront
                (trigger)));
    }

    private CraftBookPlugin plugin = CraftBookPlugin.inst();

    /**
     * The signpost we came from.
     */
    private Block trigger;
    /**
     * The block that determines bridge type.
     */
    private Block proximalBaseCenter;
    /**
     * The signpost on the other end.
     */
    private Block farSide;
    /**
     * The rectangle that we toggle.
     */
    private CuboidRegion toggle;

    // we don't store anything about the blocks on the ends because
    // we never poke them; just check that they're sane when we're building
    // the bridge. if this were a PersistentMechanic, those six blocks
    // would be considered defining blocks, though.

    @Override
    public void onRightClick(PlayerInteractEvent event) {

        if (!plugin.getConfiguration().bridgeEnabled) return;
        if (!BukkitUtil.toWorldVector(event.getClickedBlock()).equals(BukkitUtil.toWorldVector(trigger)))
            return; // wth? our manager is insane

        LocalPlayer player = new BukkitPlayer(plugin, event.getPlayer());
        if (!player.hasPermission("craftbook.mech.bridge.use")) {
            player.printError("mech.use-permission");
            return;
        }

        ChangedSign sign = null;

        if (event.getClickedBlock().getTypeId() == BlockID.SIGN_POST || event.getClickedBlock().getTypeId() ==
                BlockID.WALL_SIGN) {
            sign = BukkitUtil.toChangedSign(event.getClickedBlock());
        }

        if (sign != null && !sign.getLine(0).equalsIgnoreCase("infinite"))
            if (event.getPlayer().getItemInHand() != null)
                if (getBridgeMaterial() == event.getPlayer().getItemInHand().getTypeId()) {

                    if (!player.hasPermission("craftbook.mech.bridge.restock")) {
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
                            event.getPlayer().getItemInHand().setAmount(event.getPlayer().getItemInHand().getAmount()
                                    - amount);
                        }

                    player.print("mech.restock");
                    event.setCancelled(true);
                    return;
                }

        flipState(player);

        event.setCancelled(true);

        player.print("mech.bridge.toggle");
    }

    @Override
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {

        if (!plugin.getConfiguration().bridgeAllowRedstone) return;
        if (!BukkitUtil.toWorldVector(event.getBlock()).equals(BukkitUtil.toWorldVector(trigger))) return;
        if (event.getNewCurrent() == event.getOldCurrent()) return;

        if (event.getNewCurrent() == 0) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new ToggleRegionOpen(), 2);
        } else {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new ToggleRegionClosed(null), 2);
        }
    }

    private void flipState(LocalPlayer player) {
        // this is kinda funky, but we only check one position
        // to see if the bridge is open and/or closable.
        // efficiency choice :/
        Block hinge = proximalBaseCenter.getRelative(SignUtil.getFacing(trigger));

        // aaand we also only check if it's something we can
        // smosh or not when deciding if we're open or closed.
        // there are no errors reported upon weird blocks like
        // obsidian in the middle of a wooden bridge, just weird
        // results.
        if (canPassThrough(hinge.getTypeId())) {
            new ToggleRegionClosed(player).run();
        } else {
            new ToggleRegionOpen().run();
        }
    }

    private class ToggleRegionOpen implements Runnable {

        @Override
        public void run() {

            for (BlockVector bv : toggle) {
                Block b = trigger.getWorld().getBlockAt(bv.getBlockX(), bv.getBlockY(), bv.getBlockZ());
                int oldType = b.getTypeId();
                if (b.getTypeId() == getBridgeMaterial() || canPassThrough(b.getTypeId())) {
                    b.setTypeId(BlockID.AIR);
                    if (plugin.getConfiguration().safeDestruction) {
                        ChangedSign s = BukkitUtil.toChangedSign(trigger);
                        if (oldType != 0) {
                            addBlocks(s, 1);
                        }
                    }
                }
            }
        }
    }

    private class ToggleRegionClosed implements Runnable {

        final LocalPlayer player;

        public ToggleRegionClosed(LocalPlayer player) {

            this.player = player;
        }

        @Override
        public void run() {

            for (BlockVector bv : toggle) {
                Block b = trigger.getWorld().getBlockAt(bv.getBlockX(), bv.getBlockY(), bv.getBlockZ());
                if (canPassThrough(b.getTypeId())) {
                    if (plugin.getConfiguration().safeDestruction) {
                        ChangedSign s = BukkitUtil.toChangedSign(trigger);
                        if (hasEnoughBlocks(s)) {
                            b.setTypeId(getBridgeMaterial());
                            b.setData(getBridgeData());
                            removeBlocks(s, 1);
                        } else {
                            if (player != null) {
                                player.printError("mech.not-enough-blocks");
                            }
                            return;
                        }
                    }
                } else {
                    b.setTypeId(getBridgeMaterial());
                    b.setData(getBridgeData());
                }
            }
        }
    }

    private int getBridgeMaterial() {

        return proximalBaseCenter.getTypeId();
    }

    private byte getBridgeData() {

        return proximalBaseCenter.getData();
    }

    public boolean isValidBridge(Block baseCenter, int mat, ChangedSign s) throws InvalidMechanismException {

        if (!s.getLine(2).equals("0")) {
            if (!(proximalBaseCenter.getRelative(SignUtil.getLeft(trigger)).getTypeId() == mat)) return false;
        } else if (!(proximalBaseCenter.getTypeId() == mat)) return false;

        if (!s.getLine(3).equals("0")) {
            if (!(proximalBaseCenter.getRelative(SignUtil.getRight(trigger)).getTypeId() == mat)) return false;
        } else if (!(proximalBaseCenter.getTypeId() == mat)) return false;
        return true;
    }

    /**
     * @return whether the bridge can pass through this BlockType (and displace it if needed).
     */
    private static boolean canPassThrough(int t) {

        int[] passableBlocks = new int[10];
        passableBlocks[0] = BlockID.WATER;
        passableBlocks[1] = BlockID.STATIONARY_WATER;
        passableBlocks[2] = BlockID.LAVA;
        passableBlocks[3] = BlockID.STATIONARY_LAVA;
        passableBlocks[4] = BlockID.FENCE;
        passableBlocks[5] = BlockID.SNOW;
        passableBlocks[6] = BlockID.LONG_GRASS;
        passableBlocks[7] = BlockID.VINE;
        passableBlocks[8] = BlockID.DEAD_BUSH;
        passableBlocks[9] = BlockID.AIR;

        for (int aPassableBlock : passableBlocks) { if (aPassableBlock == t) return true; }

        return false;
    }

    /**
     * Thrown when the sign is an invalid direction.
     */
    private static class InvalidDirectionException extends InvalidMechanismException {

        private static final long serialVersionUID = -8169241147023551662L;
    }

    /**
     * Thrown when the bridge type is unacceptable.
     */
    private static class UnacceptableMaterialException extends InvalidMechanismException {

        public UnacceptableMaterialException(String msg) {

            super(msg);
        }

        private static final long serialVersionUID = -2856504362189922160L;
    }

    /**
     * Thrown when the bridge type is not constructed correctly.
     */
    private static class InvalidConstructionException extends InvalidMechanismException {

        private static final long serialVersionUID = 8758644926222590049L;

        public InvalidConstructionException(String msg) {

            super(msg);
        }
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {

        ChangedSign sign = null;

        if (event.getBlock().getTypeId() == BlockID.WALL_SIGN || event.getBlock().getTypeId() == BlockID.SIGN_POST) {
            sign = BukkitUtil.toChangedSign(event.getBlock());
        }

        if (sign == null) return;

        if (hasEnoughBlocks(sign)) {
            ItemStack toDrop = new ItemStack(getBridgeMaterial(), getBlocks(sign), getBridgeData());
            BukkitUtil.toWorld(sign.getLocalWorld()).dropItemNaturally(BukkitUtil.toLocation(sign.getBlockVector()),
                    toDrop);
        }
    }

    public boolean removeBlocks(ChangedSign s, int amount) {

        if (s.getLine(0).equalsIgnoreCase("infinite")) return true;
        int curBlocks = getBlocks(s) - amount;
        s.setLine(0, String.valueOf(curBlocks));
        s.update(false);
        return curBlocks >= 0;
    }

    public boolean addBlocks(ChangedSign s, int amount) {

        if (s.getLine(0).equalsIgnoreCase("infinite")) return true;
        int curBlocks = getBlocks(s) + amount;
        s.setLine(0, String.valueOf(curBlocks));
        s.update(false);
        return curBlocks >= 0;
    }

    public int getBlocks(ChangedSign s) {

        if (s.getLine(0).equalsIgnoreCase("infinite")) return 0;
        int curBlocks;
        try {
            curBlocks = Integer.parseInt(s.getLine(0));
        } catch (NumberFormatException e) {
            curBlocks = 0;
        }
        return curBlocks;
    }

    public boolean hasEnoughBlocks(ChangedSign s) {

        return s.getLine(0).equalsIgnoreCase("infinite") || getBlocks(s) > 0;
    }
}