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

import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.CuboidRegion;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Door.
 *
 * @author turtle9598
 */
public class Door extends AbstractMechanic {
    
    public static class Factory extends AbstractMechanicFactory<Door> {
        public Factory(MechanismsPlugin plugin) {
            this.plugin = plugin;
        }
        
        private MechanismsPlugin plugin;     
        
                /**
         * Detect the mechanic at a placed sign.
         * 
         * @throws ProcessedMechanismException 
         */
        @Override
        public Door detect(BlockWorldVector pt, LocalPlayer player, Sign sign)
                throws InvalidMechanismException, ProcessedMechanismException {
            if (sign.getLine(1).equalsIgnoreCase("[Door Down]")) {
                if (!player.hasPermission("craftbook.mech.door")) {
                    throw new InsufficientPermissionsException();
                }
                
                sign.setLine(1, "[Door Down]");
                player.print("Door created.");
            } else if (sign.getLine(1).equalsIgnoreCase("[Door Up]")) {
                if (!player.hasPermission("craftbook.mech.door")) {
                    throw new InsufficientPermissionsException();
                }
                
                sign.setLine(1, "[Door Up]");
                player.print("Door created.");
            } else {
                return null;
            }
            
            throw new ProcessedMechanismException();
        }
     
        /**
         * Explore around the trigger to find a Door; throw if things look funny.
         * 
         * @param pt the trigger (should be a signpost)
         * @return a Door if we could make a valid one, or null if this looked
         *         nothing like a door.
         * @throws InvalidMechanismException
         *             if the area looked like it was intended to be a door, but
         *             it failed.
         */
        @Override
        public Door detect(BlockWorldVector pt) throws InvalidMechanismException {
            Block block = BukkitUtil.toBlock(pt);
            // check if this looks at all like something we're interested in first
            if (block.getTypeId() != BlockID.SIGN_POST) return null;
            if (!((Sign)block.getState()).getLine(1).contains("Door")) return null;       
            
            // okay, now we can start doing exploration of surrounding blocks
            // and if something goes wrong in here then we throw fits.
            return new Door(block, plugin);
        }
    }
    
    /**
     * @param trigger
     *            if you didn't already check if this is a signpost with appropriate
     *            text, you're going on Santa's naughty list.
     * @param plugin
     * @throws InvalidMechanismException
     */
    private Door(Block trigger, MechanismsPlugin plugin) throws InvalidMechanismException {
        super();
        
        // check and set some properties
        if (!SignUtil.isCardinal(trigger)) throw new InvalidDirectionException();
        
        this.trigger = trigger;
        this.plugin = plugin;
        this.settings = plugin.getLocalConfiguration().doorSettings; 
        
        Material mat;
        findBase: {
            if (((Sign)trigger.getState()).getLine(1).equalsIgnoreCase("[Door Up]")) {
                proximalBaseCenter = trigger.getFace(BlockFace.UP);
            } else if (((Sign)trigger.getState()).getLine(1).equalsIgnoreCase("[Door Down]")) {
                proximalBaseCenter = trigger.getFace(BlockFace.DOWN);
            }    
            mat = proximalBaseCenter.getType();
            if (settings.canUseBlock(mat)) {
                if ((proximalBaseCenter.getFace(SignUtil.getLeft(trigger)).getType() == mat)
                 && (proximalBaseCenter.getFace(SignUtil.getRight(trigger)).getType()) == mat)
                    break findBase;
                throw new InvalidConstructionException("Blocks adjacent to the door block must be of the same type.");
            } else {
                throw new UnacceptableMaterialException();
            }
        }
        // Find the other side
        if (((Sign)trigger.getState()).getLine(1).equalsIgnoreCase("[Door Up]")) {
            otherSide = trigger.getFace(BlockFace.UP);
        } else if (((Sign)trigger.getState()).getLine(1).equalsIgnoreCase("[Door Down]")) {
            otherSide = trigger.getFace(BlockFace.DOWN);
        }
        for (int i = 0; i <= settings.maxLength; i++) {
            // about the loop index:
            // i = 0 is the first block after the proximal base
            // since we're allowed to have settings.maxLength toggle blocks,
            // i = settings.maxLength is actually the farthest place we're 
            // allowed to find the distal signpost
            
            if (otherSide.getType() == Material.SIGN_POST) {
                String otherSignText = ((Sign)otherSide.getState()).getLines()[1];
                if ("[Door Down]".equalsIgnoreCase(otherSignText)) break;
                if ("[Door Up]".equalsIgnoreCase(otherSignText)) break;
            }
            
            if (((Sign)trigger.getState()).getLine(1).equalsIgnoreCase("[Door Up]")) {
                otherSide = otherSide.getFace(BlockFace.UP);
            } else if (((Sign)trigger.getState()).getLine(1).equalsIgnoreCase("[Door Down]")) {
                otherSide = otherSide.getFace(BlockFace.DOWN);
            }    
        }
       
        if (otherSide.getType() != Material.SIGN_POST)
            throw new InvalidConstructionException("Door sign required on other side (or it was too far away).");
        // Check the other side's base blocks for matching type
        Block distalBaseCenter = null;
        
        if (((Sign)otherSide.getState()).getLine(1).equalsIgnoreCase("[Door Up]")) {
            distalBaseCenter = otherSide.getFace(BlockFace.UP);
        } else if (((Sign)otherSide.getState()).getLine(1).equalsIgnoreCase("[Door Down]")) {
            distalBaseCenter = otherSide.getFace(BlockFace.DOWN);
        }    
        
        if ((distalBaseCenter.getType() != mat)
         || (distalBaseCenter.getFace(SignUtil.getLeft(trigger)).getType() != mat)
         || (distalBaseCenter.getFace(SignUtil.getRight(trigger)).getType() != mat))
            throw new InvalidConstructionException("The other side must be made with the same blocks.");
        
        // Select the togglable region
        toggle = new CuboidRegion(BukkitUtil.toVector(proximalBaseCenter),BukkitUtil.toVector(distalBaseCenter));
        toggle.expand(BukkitUtil.toVector(SignUtil.getLeft(trigger)));
        toggle.expand(BukkitUtil.toVector(SignUtil.getRight(trigger)));
        toggle.contract(BukkitUtil.toVector(BlockFace.UP));
        toggle.contract(BukkitUtil.toVector(BlockFace.DOWN));
        
        // Onward to victory!


    }
    
    @Override
    public void onRightClick(PlayerInteractEvent event) {
        if (!plugin.getLocalConfiguration().doorSettings.enable) return;
        
        if (!BukkitUtil.toWorldVector(event.getClickedBlock()).equals(BukkitUtil.toWorldVector(trigger))) 
            return; 
        
        BukkitPlayer player = new BukkitPlayer(plugin, event.getPlayer());
        if ( !player.hasPermission("craftbook.mech.door.use")) {
            player.printError("You don't have permission to use doors.");
            return;
        }
        
        flipState();
    }
    
    @Override
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {
        if (!plugin.getLocalConfiguration().doorSettings.enableRedstone) return;
        
        if (!BukkitUtil.toWorldVector(event.getBlock()).equals(BukkitUtil.toWorldVector(trigger))) return;
        if (event.getNewCurrent() == event.getOldCurrent()) return;
        
        if (event.getNewCurrent() == 0) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new ToggleRegionOpen(), 2);
        } else {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new ToggleRegionClosed(), 2);
        }
    }
    
    private void flipState() {
        // this is kinda funky, but we only check one position 
        // to see if the door is open and/or closable.
        // efficiency choice :/
        Block hinge = null;
                
        if (((Sign)trigger.getState()).getLine(1).equalsIgnoreCase("[Door Up]")) {
            hinge = proximalBaseCenter.getFace(BlockFace.UP);
        } else {
            hinge = proximalBaseCenter.getFace(BlockFace.DOWN);
        }

        // aaand we also only check if it's something we can 
        // smosh or not when deciding if we're open or closed.
        // there are no errors reported upon weird blocks like 
        // obsidian in the middle of a wooden door, just weird
        // results.
        if (canPassThrough(hinge.getTypeId())) {
            new ToggleRegionClosed().run();
        } else {
            new ToggleRegionOpen().run();
        }
    }
    
    private class ToggleRegionOpen implements Runnable {
        public void run() {
            for (com.sk89q.worldedit.BlockVector bv : toggle) {     // this package specification is something that needs to be fixed in the overall scheme
                Block b = trigger.getWorld().getBlockAt(bv.getBlockX(), bv.getBlockY(), bv.getBlockZ());
                if (b.getType() == getDoorMaterial() || canPassThrough(b.getTypeId()))
                        b.setType(Material.AIR);
            }
        }
    }
    private class ToggleRegionClosed implements Runnable {
        public void run() {
            for (com.sk89q.worldedit.BlockVector bv : toggle) {     // this package specification is something that needs to be fixed in the overall scheme
                Block b = trigger.getWorld().getBlockAt(bv.getBlockX(), bv.getBlockY(), bv.getBlockZ());
                if (canPassThrough(b.getTypeId()))
                        b.setType(getDoorMaterial());
            }
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
     * Toggles the door closest to a location.
     *
     * @param pt
     * @param direction
     * @param bag
     * @return
     */
/*    public boolean setState(Boolean toOpen)
            throws InvalidDirectionException,
            UnacceptableMaterialException, InvalidConstructionException {
        
        Direction direction = BlockFace.;
        boolean upwards = isUpwards();

        Vector sideDir = null;
        Vector vertDir = upwards ? new Vector(0, 1, 0) : new Vector(0, -1, 0);

        if (direction == Direction.NORTH_SOUTH) {
            sideDir = new Vector(1, 0, 0);
        } else if(direction == Direction.WEST_EAST) {
            sideDir = new Vector(0, 0, 1);
        }
        
        int type = CraftBook.getBlockID(pt.add(vertDir));

        // Check construction
        if (!canUseBlock(type)) {
            throw new UnacceptableTypeException();
        }
        
        // Check sides
        if (CraftBook.getBlockID(pt.add(vertDir).add(sideDir)) != type
                || CraftBook.getBlockID(pt.add(vertDir).subtract(sideDir)) != type) {
            throw new InvalidConstructionException(
                    "The blocks for the door to the sides have to be the same.");
        }
        
        // Detect whether the door needs to be opened
        if (toOpen == null) {
            toOpen = !canPassThrough(CraftBook.getBlockID(pt.add(vertDir.multiply(2))));
        }
        
        Vector cur = pt.add(vertDir.multiply(2));
        boolean found = false;
        int dist = 0;
        
        // Find the other side
        for (int i = 0; i < maxLength + 2; i++) {
            int id = CraftBook.getBlockID(cur);

            if (id == BlockType.SIGN_POST) {
                SignUtil otherSignText = CraftBook.getSignText(cur);
                
                if (otherSignText != null) {
                    String line2 = otherSignText;

                    if (line2.equalsIgnoreCase("[Door Up]")
                            || line2.equalsIgnoreCase("[Door Down]")
                            || line2.equalsIgnoreCase("[Door End]")) {
                        found = true;
                        dist = i - 1;
                        break;
                    }
                }
            }

            cur = cur.add(vertDir);
        }

        // Failed to find the other side!
        if (!found) {
            throw new InvalidConstructionException(
                    "[Door] sign required on other side (or it was too far away).");
        }

        Vector otherSideBlockPt = pt.add(vertDir.multiply(dist + 2));

        // Check the other side to see if it's built correctly
        if (Material.getMaterial(otherSideBlockPt) != type
                || Material.getMaterial(otherSideBlockPt.add(sideDir)) != type
                || Material.getMaterial(otherSideBlockPt.subtract(sideDir)) != type) {
            throw new InvalidConstructionException(
            "The other side must be made with the same blocks.");
        }

        if (toOpen) {
            clearColumn(pt.add(vertDir.multiply(2)), vertDir, type, dist, bag);
            clearColumn(pt.add(vertDir.multiply(2).add(sideDir)), vertDir, type, dist, bag);
            clearColumn(pt.add(vertDir.multiply(2).subtract(sideDir)), vertDir, type, dist, bag);
        } else {
            setColumn(pt.add(vertDir.multiply(2)), vertDir, type, dist, bag);
            setColumn(pt.add(vertDir.multiply(2).add(sideDir)), vertDir, type, dist, bag);
            setColumn(pt.add(vertDir.multiply(2).subtract(sideDir)), vertDir, type, dist, bag);
        }

        bag.flushChanges();
        
        return true;
    }

    /**
     * Clears a row.
     *
     * @param origin
     * @param change
     * @param dist
     */
/*    private static void clearColumn(Vector origin, Vector change, int type, int dist, BlockBag bag)
            throws BlockSourceException {
        for (int i = 0; i < dist; i++) {
            Vector p = origin.add(change.multiply(i));
            int t = CraftBook.getBlockID(p);
            if (t == type) {
                bag.setBlockID(p, 0);
            } else if (t != 0) {
                break;
            }
        }
    }

    /**
     * Clears a row.
     *
     * @param origin
     * @param change
     * @param dist
     */
/*    private static void setColumn(Vector origin, Vector change, int type, int dist, BlockBag bag)
            throws BlockSourceException {
        for (int i = 0; i < dist; i++) {
            Vector p = origin.add(change.multiply(i));
            int t = CraftBook.getBlockID(p);
            if (canPassThrough(t)) {
                bag.setBlockID(p, type);
            } else if (t != type) {
                break;
            }
        }
    }
     * 
     */
    
    private Material getDoorMaterial() {
        return proximalBaseCenter.getType();
    }
    
    private MechanismsPlugin plugin;
    private MechanismsConfiguration.DoorSettings settings;
    
    /** The signpost we came from. */
    private Block trigger;
    /** The block that determines door type. */
    private Block proximalBaseCenter;
    /** The signpost on the other end. */
    private Block otherSide;
    /** The rectangle that we toggle. */
    private CuboidRegion toggle;
    // we don't store anything about the blocks on the ends because 
    // we never poke them; just check that they're sane when we're building
    // the door.  if this were a PersistentMechanic, those six blocks
    // would be considered defining blocks, though.
    
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
            && t != 0) {
            return false;
        } else {
            return true;
        }
    }
    
    /**
     * Thrown when the sign is an invalid direction.
     */
    private static class InvalidDirectionException extends InvalidMechanismException {
        private static final long serialVersionUID = -3183606604247616362L;
    }
    
    /**
     * Thrown when the door type is unacceptable.
     */
    private static class UnacceptableMaterialException extends InvalidMechanismException {
        private static final long serialVersionUID = 8340723004466483212L;
    }
    
    /**
     * Thrown when the door type is not constructed correctly.
     */
    private static class InvalidConstructionException extends InvalidMechanismException {
        private static final long serialVersionUID = 4943494589521864491L;

        /**
         * Construct the object.
         * 
         * @param msg
         */
        public InvalidConstructionException(String msg) {
            super(msg);
            }
        }
}

