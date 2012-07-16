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

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.InsufficientPermissionsException;
import com.sk89q.craftbook.InvalidMechanismException;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.MechanismsConfiguration;
import com.sk89q.craftbook.ProcessedMechanismException;
import com.sk89q.craftbook.SourcedBlockRedstoneEvent;
import com.sk89q.craftbook.bukkit.BukkitPlayer;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.RegionOperationException;

/**
 * The default bridge mechanism -- signposts on either side of a 3xN plane of
 * (or 1xN plane if 1 on second line) blocks.
 * 
 * @author hash
 * 
 */
public class Bridge extends AbstractMechanic {

    public static class Factory extends AbstractMechanicFactory<Bridge> {
        public Factory(MechanismsPlugin plugin) {
            this.plugin = plugin;
        }

        private MechanismsPlugin plugin;

        /**
         * Explore around the trigger to find a Bridge; throw if things look funny.
         * 
         * @param pt the trigger (should be a signpost)
         * @return a Bridge if we could make a valid one, or null if this looked
         *         nothing like a bridge.
         * @throws InvalidMechanismException
         *             if the area looked like it was intended to be a bridge, but
         *             it failed.
         */
        @Override
        public Bridge detect(BlockWorldVector pt) throws InvalidMechanismException {
            Block block = BukkitUtil.toBlock(pt);
            // check if this looks at all like something we're interested in first
            if (block.getTypeId() != BlockID.SIGN_POST) return null;
            if (!((Sign)block.getState()).getLine(1).equalsIgnoreCase("[Bridge]")) return null;

            // okay, now we can start doing exploration of surrounding blocks
            // and if something goes wrong in here then we throw fits.
            return new Bridge(block, plugin);
        }

        /**
         * Detect the mechanic at a placed sign.
         * 
         * @throws ProcessedMechanismException
         */
        @Override
        public Bridge detect(BlockWorldVector pt, LocalPlayer player, Sign sign)
                throws InvalidMechanismException, ProcessedMechanismException {
            if (sign.getLine(1).equalsIgnoreCase("[Bridge]")) {
                if (!player.hasPermission("craftbook.mech.bridge")) {
                    throw new InsufficientPermissionsException();
                }

                sign.setLine(1, "[Bridge]");
                player.print("mech.bridge.create");
            } else if (sign.getLine(1).equalsIgnoreCase("[Bridge End]")) {
                if (!player.hasPermission("craftbook.mech.bridge")) {
                    throw new InsufficientPermissionsException();
                }

                sign.setLine(1, "[Bridge End]");
                player.print("mech.bridge.end-create");
            } else {
                return null;
            }

            throw new ProcessedMechanismException();
        }
    }
    //Factory ends here

    /**
     * @param trigger
     *            if you didn't already check if this is a signpost with appropriate
     *            text, you're going on Santa's naughty list.
     * @param plugin
     * @throws InvalidMechanismException
     */
    @SuppressWarnings("deprecation")
    private Bridge(Block trigger, MechanismsPlugin plugin) throws InvalidMechanismException {
        super();

        if (!SignUtil.isCardinal(trigger)) throw new InvalidDirectionException();
        BlockFace dir = SignUtil.getFacing(trigger);

        this.trigger = trigger;
        this.plugin = plugin;
        this.settings = plugin.getLocalConfiguration().bridgeSettings;

        Sign s = (Sign)trigger.getState();

        // Attempt to detect whether the bridge is above or below the sign,
        // first assuming that the bridge is above
        Material mat;
        findBase: {
            proximalBaseCenter = trigger.getRelative(BlockFace.UP);
            mat = proximalBaseCenter.getType();
            if (settings.canUseBlock(mat)) {
                if ((proximalBaseCenter.getRelative(SignUtil.getLeft(trigger)).getType() == mat
                        && proximalBaseCenter.getRelative(SignUtil.getRight(trigger)).getType() == mat))
                    break findBase;     // yup, it's above
                // cant throw the invalid construction exception here
                // because there still might be a valid one below
            }
            proximalBaseCenter = trigger.getRelative(BlockFace.DOWN);
            mat = proximalBaseCenter.getType();
            if (settings.canUseBlock(mat)) {
                if ((proximalBaseCenter.getRelative(SignUtil.getLeft(trigger)).getType() == mat
                        && proximalBaseCenter.getRelative(SignUtil.getRight(trigger)).getType() == mat))
                    break findBase;     // it's below
                throw new InvalidConstructionException("mech.bridge.material");
            } else {
                throw new UnacceptableMaterialException();
            }
        }

        // Find the other side
        farside = trigger.getRelative(dir);
        for (int i = 0; i <= settings.maxLength; i++) {
            // about the loop index:
            // i = 0 is the first block after the proximal base
            // since we're allowed to have settings.maxLength toggle blocks,
            // i = settings.maxLength is actually the farthest place we're
            // allowed to find the distal signpost

            if (farside.getType() == Material.SIGN_POST) {
                String otherSignText = ((Sign)farside.getState()).getLines()[1];
                if ("[Bridge]".equalsIgnoreCase(otherSignText)) break;
                if ("[Bridge End]".equalsIgnoreCase(otherSignText)) break;
            }

            farside = farside.getRelative(dir);
        }
        if (farside.getType() != Material.SIGN_POST)
            throw new InvalidConstructionException("mech.bridge.other-sign");

        // Check the other side's base blocks for matching type
        Block distalBaseCenter = farside.getRelative(trigger.getFace(proximalBaseCenter));
        if ((distalBaseCenter.getType() != mat && distalBaseCenter.getData() != proximalBaseCenter.getData())
                || ((distalBaseCenter.getRelative(SignUtil.getLeft(trigger)).getType() != mat && distalBaseCenter.getRelative(SignUtil.getLeft(trigger)).getData() != proximalBaseCenter.getData())
                        || (distalBaseCenter.getRelative(SignUtil.getRight(trigger)).getType() != mat && distalBaseCenter.getRelative(SignUtil.getRight(trigger)).getData() != proximalBaseCenter.getData())))
            throw new InvalidConstructionException("mech.bridge.material");

        // Select the togglable region
        toggle = new CuboidRegion(BukkitUtil.toVector(proximalBaseCenter),BukkitUtil.toVector(distalBaseCenter));
        int left, right;
        try {
            left = Integer.parseInt(s.getLine(2));
        }
        catch(Exception e){
            left = 1;
        }
        try {
            right = Integer.parseInt(s.getLine(3));
        }
        catch(Exception e){
            right = 1;
        }
        //if(left > plugin.getLocalConfiguration().bridgeSettings.maxWidth) left = plugin.getLocalConfiguration().bridgeSettings.maxWidth;
        //if(right > plugin.getLocalConfiguration().bridgeSettings.maxWidth) right = plugin.getLocalConfiguration().bridgeSettings.maxWidth;

        if(left == 1)
            try {
                toggle.expand(BukkitUtil.toVector(SignUtil.getLeft(trigger)));
            } catch (RegionOperationException e) {
                e.printStackTrace();
            }
        else if(left>1) {
            for(int i = 0; i < left; i++)
            {
                try {
                    toggle.expand(BukkitUtil.toVector(SignUtil.getLeft(trigger)));
                } catch (RegionOperationException e) {
                    e.printStackTrace();
                }
            }
        }
        if(right == 1)
            try {
                toggle.expand(BukkitUtil.toVector(SignUtil.getRight(trigger)));
            } catch (RegionOperationException e) {
                e.printStackTrace();
            }
        else if(right>1) {
            for(int i = 0; i < right; i++)
            {
                try {
                    toggle.expand(BukkitUtil.toVector(SignUtil.getRight(trigger)));
                } catch (RegionOperationException e) {
                    e.printStackTrace();
                }
            }
        }

        toggle.contract(BukkitUtil.toVector(SignUtil.getBack(trigger)),
                BukkitUtil.toVector(SignUtil.getFront(trigger)));
    }

    private MechanismsPlugin plugin;
    private MechanismsConfiguration.BridgeSettings settings;

    /** The signpost we came from. */
    private Block trigger;
    /** The block that determines bridge type. */
    private Block proximalBaseCenter;
    /** The signpost on the other end. */
    private Block farside;
    /** The rectangle that we toggle. */
    private CuboidRegion toggle;
    // we don't store anything about the blocks on the ends because
    // we never poke them; just check that they're sane when we're building
    // the bridge.  if this were a PersistentMechanic, those six blocks
    // would be considered defining blocks, though.


    @Override
    public void onRightClick(PlayerInteractEvent event) {
        if (!plugin.getLocalConfiguration().bridgeSettings.enable) return;

        if (!BukkitUtil.toWorldVector(event.getClickedBlock()).equals(BukkitUtil.toWorldVector(trigger)))
            return; //wth? our manager is insane

        BukkitPlayer player = new BukkitPlayer(plugin, event.getPlayer());
        if ( !player.hasPermission("craftbook.mech.bridge.use")) {
            player.printError("mech.use-permission");
            return;
        }

        if(event.getPlayer().getItemInHand() != null) {
            if(getBridgeMaterial().getId() == event.getPlayer().getItemInHand().getTypeId()) {
                Sign sign = null;

                if (event.getClickedBlock().getTypeId() == BlockID.SIGN_POST || event.getClickedBlock().getTypeId() == BlockID.WALL_SIGN) {
                    BlockState state = event.getClickedBlock().getState();
                    if (state instanceof Sign)
                        sign = (Sign) state;
                }

                if(sign!=null) {
                    try {
                        int newBlocks = Integer.parseInt(sign.getLine(0)) + 1;
                        sign.setLine(0, newBlocks + "");
                        sign.update();
                    }
                    catch(Exception e) {
                        sign.setLine(0, "1");
                        sign.update();
                    }

                    event.getPlayer().getItemInHand().setAmount(event.getPlayer().getItemInHand().getAmount() - 1);
                    if(event.getPlayer().getItemInHand().getAmount() == 0)
                        event.getPlayer().getItemInHand().setTypeId(0);

                    player.print("Bridge Restocked!");
                    event.setCancelled(true);
                    return;
                }
            }
        }

        flipState(player);

        event.setCancelled(true);

        //notify event.getPlayer();
    }

    @Override
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {
        if (!plugin.getLocalConfiguration().bridgeSettings.enableRedstone) return;

        if (!BukkitUtil.toWorldVector(event.getBlock()).equals(BukkitUtil.toWorldVector(trigger))) return; //wth? our manager is insane
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
                int oldType = 0;
                if(b != null)
                    oldType = b.getTypeId();
                if (b.getType() == getBridgeMaterial() || canPassThrough(b.getTypeId())) {
                    b.setType(Material.AIR);
                    if(plugin.getLocalConfiguration().mechSettings.stopDestruction) {
                        Sign s = (Sign)trigger.getState();
                        int curBlocks = 0;
                        try {
                            curBlocks = Integer.parseInt(s.getLine(0));
                        }
                        catch(NumberFormatException e) {
                            curBlocks = 0;
                        }
                        if(oldType != 0)
                            curBlocks++;
                        s.setLine(0, curBlocks + "");
                        s.update();
                    }
                }
            }
        }
    }
    private class ToggleRegionClosed implements Runnable {

        LocalPlayer player;

        public ToggleRegionClosed(LocalPlayer player) {
            this.player = player;
        }

        @Override
        public void run() {
            for (BlockVector bv : toggle) {
                Block b = trigger.getWorld().getBlockAt(bv.getBlockX(), bv.getBlockY(), bv.getBlockZ());
                if (canPassThrough(b.getTypeId())) {
                    if(plugin.getLocalConfiguration().mechSettings.stopDestruction) {
                        Sign s = (Sign)trigger.getState();
                        int curBlocks = 0;
                        try {
                            curBlocks = Integer.parseInt(s.getLine(0));
                        }
                        catch(NumberFormatException e) {
                            curBlocks = 0;
                        }
                        if(curBlocks > 0) {
                            b.setType(getBridgeMaterial());
                            b.setData(getBridgeData());
                            curBlocks--;
                            s.setLine(0, curBlocks + "");
                            s.update();
                        }
                        else {
                            if(player!=null)
                                player.printError("Not enough blocks for mechanic to function!");
                            return;
                        }
                    }
                    else {
                        b.setType(getBridgeMaterial());
                        b.setData(getBridgeData());
                    }
                }
            }
        }
    }

    private Material getBridgeMaterial() {
        return proximalBaseCenter.getType();
    }

    private byte getBridgeData() {
        return proximalBaseCenter.getData();
    }

    /**
     * @return whether the door can pass through this BlockType (and displace it
     *         if needed).
     */
    private static boolean canPassThrough(int t) {
        if (t != BlockID.WATER
                && t != BlockID.STATIONARY_WATER
                && t != BlockID.LAVA
                && t != BlockID.STATIONARY_LAVA
                && t != BlockID.FENCE
                && t != BlockID.SNOW
                && t != BlockID.LONG_GRASS
                && t != BlockID.VINE
                && t != BlockID.DEAD_BUSH
                && t != 0) {
            return false;
        } else {
            return true;
        }
    }



    @Override
    public void unload() {
        /* we're not persistent */
    }

    @Override
    public boolean isActive() {
        /* we're not persistent */
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
    }
}
