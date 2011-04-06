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

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.event.player.*;
import com.sk89q.craftbook.*;
import com.sk89q.craftbook.bukkit.*;
import com.sk89q.craftbook.util.*;
import com.sk89q.worldedit.blocks.*;
import com.sk89q.worldedit.regions.*;

/**
 * The default bridge mechanism -- signposts on either side of a 3xN plane of
 * blocks.
 * 
 * @author hash
 * 
 */
public class Bridge extends Mechanic {
    public static class Factory implements MechanicFactory<Bridge> {
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
            Block block = pt.toBlock();
            // check if this looks at all like something we're interested in first
            if (block.getTypeId() != BlockID.SIGN_POST) return null;
            if (!((Sign)block.getState()).getLine(1).equalsIgnoreCase("[Bridge]")) return null;
            
            // okay, now we can start doing exploration of surrounding blocks
            // and if something goes wrong in here then we throw fits.
            return new Bridge(block, plugin);
        }
    }
    
    /**
     * @param trigger
     *            if you didn't already check if this is a signpost with appropriate
     *            text, you're going on Santa's naughty list.
     * @param plugin
     * @throws InvalidMechanismException
     */
    private Bridge(Block trigger, MechanismsPlugin plugin) throws InvalidMechanismException {
        super();
        
        if (!SignUtil.isCardinal(trigger)) throw new InvalidDirectionException();
        BlockFace dir = SignUtil.getFacing(trigger);
        
        this.trigger = trigger;
        this.global = plugin;
        this.settings = plugin.getLocalConfiguration().bridgeSettings; 
        
        // Attempt to detect whether the bridge is above or below the sign,
        // first assuming that the bridge is above
        Material mat;
        findBase: {
            proximalBaseCenter = trigger.getFace(BlockFace.UP);
            mat = proximalBaseCenter.getType();
            if (settings.canUseBlock(mat)) {
                if ((proximalBaseCenter.getFace(SignUtil.getLeft(trigger)).getType() == mat)
                 && (proximalBaseCenter.getFace(SignUtil.getRight(trigger)).getType()) == mat)
                    break findBase;     // yup, it's above
                // cant throw the invalid construction exception here
                // because there still might be a valid one below
            }
            proximalBaseCenter = trigger.getFace(BlockFace.DOWN);
            mat = proximalBaseCenter.getType();
            if (settings.canUseBlock(mat)) {
                if ((proximalBaseCenter.getFace(SignUtil.getLeft(trigger)).getType() == mat)
                 && (proximalBaseCenter.getFace(SignUtil.getRight(trigger)).getType()) == mat)
                    break findBase;     // it's below
                throw new InvalidConstructionException("Blocks adjacent to the bridge block must be of the same type.");
            } else {
                throw new UnacceptableMaterialException();
            }
        }
        
        // Find the other side
        farside = trigger.getFace(dir);
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
            
            farside = farside.getFace(dir);
        }
        if (farside.getType() != Material.SIGN_POST)
            throw new InvalidConstructionException("[Bridge] sign required on other side (or it was too far away).");
        
        // Check the other side's base blocks for matching type
        Block distalBaseCenter = farside.getFace(trigger.getFace(proximalBaseCenter));
        if ((distalBaseCenter.getType() != mat)
         || (distalBaseCenter.getFace(SignUtil.getLeft(trigger)).getType() != mat)
         || (distalBaseCenter.getFace(SignUtil.getRight(trigger)).getType() != mat))
            throw new InvalidConstructionException("The other side must be made with the same blocks.");
        
        // Select the togglable region
        toggle = new CuboidRegion(BukkitUtil.toVector(proximalBaseCenter),BukkitUtil.toVector(distalBaseCenter));
        toggle.contract(BukkitUtil.toVector(SignUtil.getBack(trigger)));
        toggle.contract(BukkitUtil.toVector(SignUtil.getFront(trigger)));
        toggle.expand(BukkitUtil.toVector(SignUtil.getLeft(trigger)));
        toggle.expand(BukkitUtil.toVector(SignUtil.getRight(trigger)));
        
        // Win!
    }
    
    private MechanismsPlugin global;
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
        if (!global.getLocalConfiguration().bridgeSettings.enable) return;
        
        if (!BukkitUtil.toWorldVector(event.getClickedBlock()).equals(BukkitUtil.toWorldVector(trigger))) return; //wth? our manager is insane
        flipState();
        //notify event.getPlayer();
    }
    
    @Override
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {
        if (!global.getLocalConfiguration().bridgeSettings.enableRedstone) return;
        
        if (!BukkitUtil.toWorldVector(event.getBlock()).equals(BukkitUtil.toWorldVector(trigger))) return; //wth? our manager is insane
        if (event.getNewCurrent() == event.getOldCurrent()) return;
        
        if (event.getNewCurrent() == 0) {
            global.getServer().getScheduler().scheduleSyncDelayedTask(global, new ToggleRegionOpen(), 2);
        } else {
            global.getServer().getScheduler().scheduleSyncDelayedTask(global, new ToggleRegionClosed(), 2);
        }
    }
    
    private void flipState() {
        // this is kinda funky, but we only check one position 
        // to see if the bridge is open and/or closable.
        // efficiency choice :/
        Block hinge = proximalBaseCenter.getFace(SignUtil.getFacing(trigger));
        
        // aaand we also only check if it's something we can 
        // smosh or not when deciding if we're open or closed.
        // there are no errors reported upon weird blocks like 
        // obsidian in the middle of a wooden bridge, just weird
        // results.
        if (canPassThrough(hinge.getType())) {
            new ToggleRegionClosed().run();
        } else {
            new ToggleRegionOpen().run();
        }
    }
    private class ToggleRegionOpen implements Runnable {
        public void run() {
            for (com.sk89q.worldedit.BlockVector bv : toggle) {     // this package specification is something that needs to be fixed in the overall scheme
                Block b = trigger.getWorld().getBlockAt(bv.getBlockX(), bv.getBlockY(), bv.getBlockZ());
                if (b.getType() == getBridgeMaterial() || canPassThrough(b.getType()))
                        b.setType(Material.AIR);
            }
        }
    }
    private class ToggleRegionClosed implements Runnable {
        public void run() {
            for (com.sk89q.worldedit.BlockVector bv : toggle) {     // this package specification is something that needs to be fixed in the overall scheme
                Block b = trigger.getWorld().getBlockAt(bv.getBlockX(), bv.getBlockY(), bv.getBlockZ());
                if (canPassThrough(b.getType()))
                        b.setType(getBridgeMaterial());
            }
        }
    }
    
    private Material getBridgeMaterial() {
        return proximalBaseCenter.getType();
    }
    
    

    /**
     * @return whether the door can pass through this BlockType (and displace it
     *         if needed).
     */
    private static boolean canPassThrough(Material t) {
        switch (t) {
            case AIR:
            case WATER:
            case STATIONARY_WATER:
            case LAVA:
            case STATIONARY_LAVA:
            case SNOW:
                return true;
            default:
                return false;
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
}
