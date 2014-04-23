// $Id$
/*
 * CraftBook Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

package com.sk89q.craftbook.bukkit;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.material.Attachable;
import org.bukkit.material.Directional;
import org.bukkit.material.PressureSensor;

import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.ProtectionUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.events.SignClickEvent;
import com.sk89q.craftbook.util.events.SourcedBlockRedstoneEvent;
import com.sk89q.craftbook.util.exceptions.InvalidMechanismException;
import com.sk89q.craftbook.vehicles.cart.blocks.CartBlockMechanism;
import com.sk89q.craftbook.vehicles.cart.blocks.CartMechanismBlocks;
import com.sk89q.craftbook.vehicles.cart.events.CartBlockEnterEvent;
import com.sk89q.craftbook.vehicles.cart.events.CartBlockImpactEvent;
import com.sk89q.craftbook.vehicles.cart.events.CartBlockRedstoneEvent;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.bukkit.BukkitUtil;

/**
 * This adapter hooks a mechanic manager up to Bukkit.
 *
 * @author sk89q
 */
public class MechanicListenerAdapter implements Listener {

    Set<String> signClickTimer = new HashSet<String>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(final PlayerInteractEvent event) {

        if (!EventUtil.passesFilter(event))
            return;

        Block block = null;
        Action action = null;
        if(event.getAction() == Action.RIGHT_CLICK_AIR) {
            block = event.getPlayer().getTargetBlock(null, 5);
            if(block != null && block.getType() != Material.AIR)
                action = Action.RIGHT_CLICK_BLOCK;
            else
                action = Action.RIGHT_CLICK_AIR;
        } else {
            block = event.getClickedBlock();
            action = event.getAction();
        }

        if(block != null && SignUtil.isSign(block)) {
            if(CraftBookPlugin.inst().getConfiguration().signClickTimeout > 0) {
                if(signClickTimer.contains(event.getPlayer().getName())) {
                    return;
                } else {
                    signClickTimer.add(event.getPlayer().getName());
                    Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new Runnable() {
                        @Override
                        public void run () {
                            signClickTimer.remove(event.getPlayer().getName());
                        }
                    }, CraftBookPlugin.inst().getConfiguration().signClickTimeout);
                }
            }
            SignClickEvent ev = new SignClickEvent(event.getPlayer(), action, event.getItem(), block, event.getBlockFace());
            CraftBookPlugin.inst().getServer().getPluginManager().callEvent(ev);
            if(ev.isCancelled())
                event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {

        if (!EventUtil.passesFilter(event))
            return;

        if(!(CraftBookPlugin.inst().getConfiguration().advancedBlockChecks && event.isCancelled())) {
            checkBlockChange(event.getPlayer(), event.getBlock(), false);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {

        if (!EventUtil.passesFilter(event))
            return;

        if(!(CraftBookPlugin.inst().getConfiguration().advancedBlockChecks && event.isCancelled())) {
            checkBlockChange(event.getPlayer(), event.getBlock(), true);
        }
    }

    public void checkBlockChange(Player player, Block block, boolean build) {
        switch(block.getType()) {

            case REDSTONE_TORCH_ON:
            case DIODE_BLOCK_ON:
            case REDSTONE_BLOCK:
            case REDSTONE_COMPARATOR_ON:
                if(CraftBookPlugin.inst().getConfiguration().pedanticBlockChecks && !ProtectionUtil.canBuild(player, block.getLocation(), build))
                    break;
                handleRedstoneForBlock(block, build ? 0 : 15, build ? 15 : 0);
                break;
            case REDSTONE_WIRE:
                if(CraftBookPlugin.inst().getConfiguration().pedanticBlockChecks && !ProtectionUtil.canBuild(player, block.getLocation(), build))
                    break;
                if(block.getData() > 0)
                    handleRedstoneForBlock(block, block.getData(), 0);
                break;
            case LEVER:
                if(CraftBookPlugin.inst().getConfiguration().pedanticBlockChecks && !ProtectionUtil.canBuild(player, block.getLocation(), build))
                    break;
                if(((org.bukkit.material.Lever) block.getState().getData()).isPowered())
                    handleRedstoneForBlock(block, build ? 0 : 15, build ? 15 : 0);
                break;
            case WOOD_BUTTON:
            case STONE_BUTTON:
                if(CraftBookPlugin.inst().getConfiguration().pedanticBlockChecks && !ProtectionUtil.canBuild(player, block.getLocation(), build))
                    break;
                if(((org.bukkit.material.Button) block.getState().getData()).isPowered())
                    handleRedstoneForBlock(block, build ? 0 : 15, build ? 15 : 0);
                break;
            case STONE_PLATE:
            case WOOD_PLATE:
            case GOLD_PLATE:
            case IRON_PLATE:
            case DETECTOR_RAIL:
                if(CraftBookPlugin.inst().getConfiguration().pedanticBlockChecks && !ProtectionUtil.canBuild(player, block.getLocation(), build))
                    break;
                if(block.getState().getData() instanceof PressureSensor && ((PressureSensor) block.getState().getData()).isPressed())
                    handleRedstoneForBlock(block, build ? 0 : 15, build ? 15 : 0);
                break;
            default:
                break;
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {

        if (!EventUtil.passesFilter(event))
            return;

        handleRedstoneForBlock(event.getBlock(), event.getOldCurrent(), event.getNewCurrent());
    }

    public void handleRedstoneForBlock(Block block, int oldLevel, int newLevel) {

        World world = block.getWorld();
        BlockWorldVector v = BukkitUtil.toWorldVector(block);

        // Give the method a BlockWorldVector instead of a Block
        boolean wasOn = oldLevel >= 1;
        boolean isOn = newLevel >= 1;
        boolean wasChange = wasOn != isOn;

        // For efficiency reasons, we're only going to consider changes between
        // off and on state, and ignore simple current changes (i.e. 15->13)
        if (!wasChange) return;

        LocalWorld w = BukkitUtil.getLocalWorld(world);
        int x = v.getBlockX();
        int y = v.getBlockY();
        int z = v.getBlockZ();

        // When this hook has been called, the level in the world has not
        // yet been updated, so we're going to do this very ugly thing of
        // faking the value with the new one whenever the data value of this
        // block is requested -- it is quite ugly
        switch(block.getTypeId()) {
            case BlockID.REDSTONE_WIRE:
                if (CraftBookPlugin.inst().getConfiguration().indirectRedstone) {

                    // power all blocks around the redstone wire on the same y level
                    // north/south
                    handleDirectWireInput(new WorldVector(w, x - 1, y, z), block, oldLevel, newLevel);
                    handleDirectWireInput(new WorldVector(w, x + 1, y, z), block, oldLevel, newLevel);
                    // east/west
                    handleDirectWireInput(new WorldVector(w, x, y, z - 1), block, oldLevel, newLevel);
                    handleDirectWireInput(new WorldVector(w, x, y, z + 1), block, oldLevel, newLevel);

                    // Can be triggered from below
                    handleDirectWireInput(new WorldVector(w, x, y + 1, z), block, oldLevel, newLevel);

                    // Can be triggered from above (Eg, glass->glowstone like redstone lamps)
                    handleDirectWireInput(new WorldVector(w, x, y - 1, z), block, oldLevel, newLevel);
                } else {

                    int above = world.getBlockTypeIdAt(x, y + 1, z);

                    int westSide = world.getBlockTypeIdAt(x, y, z + 1);
                    int westSideAbove = world.getBlockTypeIdAt(x, y + 1, z + 1);
                    int westSideBelow = world.getBlockTypeIdAt(x, y - 1, z + 1);
                    int eastSide = world.getBlockTypeIdAt(x, y, z - 1);
                    int eastSideAbove = world.getBlockTypeIdAt(x, y + 1, z - 1);
                    int eastSideBelow = world.getBlockTypeIdAt(x, y - 1, z - 1);

                    int northSide = world.getBlockTypeIdAt(x - 1, y, z);
                    int northSideAbove = world.getBlockTypeIdAt(x - 1, y + 1, z);
                    int northSideBelow = world.getBlockTypeIdAt(x - 1, y - 1, z);
                    int southSide = world.getBlockTypeIdAt(x + 1, y, z);
                    int southSideAbove = world.getBlockTypeIdAt(x + 1, y + 1, z);
                    int southSideBelow = world.getBlockTypeIdAt(x + 1, y - 1, z);

                    // Make sure that the wire points to only this block
                    if (!BlockType.isRedstoneBlock(westSide) && !BlockType.isRedstoneBlock(eastSide)
                            && (!BlockType.isRedstoneBlock(westSideAbove) || westSide == 0 || above != 0)
                            && (!BlockType.isRedstoneBlock(eastSideAbove) || eastSide == 0 || above != 0)
                            && (!BlockType.isRedstoneBlock(westSideBelow) || westSide != 0)
                            && (!BlockType.isRedstoneBlock(eastSideBelow) || eastSide != 0)) {
                        // Possible blocks north / south
                        handleDirectWireInput(new WorldVector(w, x - 1, y, z), block, oldLevel, newLevel);
                        handleDirectWireInput(new WorldVector(w, x + 1, y, z), block, oldLevel, newLevel);
                        handleDirectWireInput(new WorldVector(w, x - 1, y - 1, z), block, oldLevel, newLevel);
                        handleDirectWireInput(new WorldVector(w, x + 1, y - 1, z), block, oldLevel, newLevel);
                    }

                    if (!BlockType.isRedstoneBlock(northSide) && !BlockType.isRedstoneBlock(southSide)
                            && (!BlockType.isRedstoneBlock(northSideAbove) || northSide == 0 || above != 0)
                            && (!BlockType.isRedstoneBlock(southSideAbove) || southSide == 0 || above != 0)
                            && (!BlockType.isRedstoneBlock(northSideBelow) || northSide != 0)
                            && (!BlockType.isRedstoneBlock(southSideBelow) || southSide != 0)) {
                        // Possible blocks west / east
                        handleDirectWireInput(new WorldVector(w, x, y, z - 1), block, oldLevel, newLevel);
                        handleDirectWireInput(new WorldVector(w, x, y, z + 1), block, oldLevel, newLevel);
                        handleDirectWireInput(new WorldVector(w, x, y - 1, z - 1), block, oldLevel, newLevel);
                        handleDirectWireInput(new WorldVector(w, x, y - 1, z + 1), block, oldLevel, newLevel);
                    }

                    // Can be triggered from below
                    handleDirectWireInput(new WorldVector(w, x, y + 1, z), block, oldLevel, newLevel);

                    // Can be triggered from above
                    handleDirectWireInput(new WorldVector(w, x, y - 1, z), block, oldLevel, newLevel);
                }
                return;
            case BlockID.REDSTONE_REPEATER_OFF:
            case BlockID.REDSTONE_REPEATER_ON:
            case BlockID.COMPARATOR_OFF:
            case BlockID.COMPARATOR_ON:
                Directional diode = (Directional) block.getState().getData();
                BlockFace f = diode.getFacing();
                handleDirectWireInput(new WorldVector(w, x + f.getModX(), y, z + f.getModZ()), block, oldLevel, newLevel);
                if(block.getRelative(f).getTypeId() != 0) {
                    handleDirectWireInput(new WorldVector(w, x + f.getModX(), y - 1, z + f.getModZ()), block, oldLevel, newLevel);
                    handleDirectWireInput(new WorldVector(w, x + f.getModX(), y + 1, z + f.getModZ()), block, oldLevel, newLevel);
                    handleDirectWireInput(new WorldVector(w, x + f.getModX() + 1, y - 1, z + f.getModZ()), block, oldLevel, newLevel);
                    handleDirectWireInput(new WorldVector(w, x + f.getModX() - 1, y - 1, z + f.getModZ()), block, oldLevel, newLevel);
                    handleDirectWireInput(new WorldVector(w, x + f.getModX() + 1, y - 1, z + f.getModZ() + 1), block, oldLevel, newLevel);
                    handleDirectWireInput(new WorldVector(w, x + f.getModX() - 1, y - 1, z + f.getModZ() - 1), block, oldLevel, newLevel);
                }
                return;
            case BlockID.STONE_BUTTON:
            case BlockID.WOODEN_BUTTON:
            case BlockID.LEVER:
                Attachable button = (Attachable) block.getState().getData();
                if(button != null) {
                    BlockFace face = button.getAttachedFace();
                    if(face != null)
                        handleDirectWireInput(new WorldVector(w, x + face.getModX()*2, y, z + face.getModZ()*2), block, oldLevel, newLevel);
                }
        }

        // For redstone wires and repeaters, the code already exited this method
        // Non-wire blocks proceed

        handleDirectWireInput(new WorldVector(w, x - 1, y, z), block, oldLevel, newLevel);
        handleDirectWireInput(new WorldVector(w, x + 1, y, z), block, oldLevel, newLevel);
        handleDirectWireInput(new WorldVector(w, x - 1, y - 1, z), block, oldLevel, newLevel);
        handleDirectWireInput(new WorldVector(w, x + 1, y - 1, z), block, oldLevel, newLevel);
        handleDirectWireInput(new WorldVector(w, x, y, z - 1), block, oldLevel, newLevel);
        handleDirectWireInput(new WorldVector(w, x, y, z + 1), block, oldLevel, newLevel);
        handleDirectWireInput(new WorldVector(w, x, y - 1, z - 1), block, oldLevel, newLevel);
        handleDirectWireInput(new WorldVector(w, x, y - 1, z + 1), block, oldLevel, newLevel);

        // Can be triggered from below
        handleDirectWireInput(new WorldVector(w, x, y + 1, z), block, oldLevel, newLevel);

        // Can be triggered from above
        handleDirectWireInput(new WorldVector(w, x, y - 1, z), block, oldLevel, newLevel);
    }

    /**
     * Handle the direct wire input.
     *
     * @param pt
     * @param sourceBlock
     * @param oldLevel
     * @param newLevel
     */
    protected void handleDirectWireInput(WorldVector pt, Block sourceBlock, int oldLevel, int newLevel) {

        Block block = sourceBlock.getWorld().getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if(block.getLocation().equals(sourceBlock.getLocation())) //The same block, don't run.
            return;
        final SourcedBlockRedstoneEvent event = new SourcedBlockRedstoneEvent(sourceBlock, block, oldLevel, newLevel);

        CraftBookPlugin.inst().getServer().getPluginManager().callEvent(event);

        CraftBookPlugin.server().getScheduler().runTask(CraftBookPlugin.inst(), new Runnable() {

            @Override
            public void run () {
                try {
                    CartMechanismBlocks cmb = CartMechanismBlocks.find(event.getBlock());
                    CartBlockRedstoneEvent ev = new CartBlockRedstoneEvent(event.getBlock(), event.getSource(), event.getOldCurrent(), event.getNewCurrent(), cmb, CartBlockMechanism.getCart(cmb.rail));
                    CraftBookPlugin.inst().getServer().getPluginManager().callEvent(ev);
                } catch (InvalidMechanismException ignored) {
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleMove(VehicleMoveEvent event) {

        if(!CraftBookPlugin.inst().getConfiguration().enableVehicles) return;
        if (!EventUtil.passesFilter(event))
            return;

        if(event.getVehicle() instanceof Minecart) {
            try {
                Minecart cart = (Minecart) event.getVehicle();
                CartMechanismBlocks cmb = CartMechanismBlocks.findByRail(event.getTo().getBlock());
                cmb.setFromBlock(event.getFrom().getBlock());
                Location from = event.getFrom();
                Location to = event.getTo();
                if(LocationUtil.getDistanceSquared(from, to) > 2*2) //Further than max distance
                    return;
                boolean minor = from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ();
                CartBlockImpactEvent ev = new CartBlockImpactEvent(cart, from, to, cmb, minor);
                CraftBookPlugin.inst().getServer().getPluginManager().callEvent(ev);
            } catch (InvalidMechanismException ignored) {
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleEnter(VehicleEnterEvent event) {

        if(!CraftBookPlugin.inst().getConfiguration().enableVehicles) return;
        if (!EventUtil.passesFilter(event))
            return;

        if(!event.getVehicle().getWorld().isChunkLoaded(event.getVehicle().getLocation().getBlockX() >> 4, event.getVehicle().getLocation().getBlockZ() >> 4))
            return;

        if(event.getVehicle() instanceof Minecart) {
            try {
                Minecart cart = (Minecart) event.getVehicle();
                Block block = event.getVehicle().getLocation().getBlock();
                CartMechanismBlocks cmb = CartMechanismBlocks.findByRail(block);
                cmb.setFromBlock(block); // WAI
                CartBlockEnterEvent ev = new CartBlockEnterEvent(cart, event.getEntered(), cmb);
                CraftBookPlugin.inst().getServer().getPluginManager().callEvent(ev);
                if(ev.isCancelled())
                    event.setCancelled(true);
            } catch (InvalidMechanismException ignored) {
            }
        }
    }

    /**
     * Called when a chunk is loaded.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onChunkLoad(final ChunkLoadEvent event) {

        if (!EventUtil.passesFilter(event))
            return;

        CraftBookPlugin.server().getScheduler().runTaskLater(CraftBookPlugin.inst(), new Runnable() {

            @Override
            public void run() {

                CraftBookPlugin.inst().getSelfTriggerManager().registerSelfTrigger(event.getChunk());
            }
        }, 2);
    }

    /**
     * Called when a chunk is unloaded.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onChunkUnload(ChunkUnloadEvent event) {

        if (!EventUtil.passesFilter(event))
            return;

        CraftBookPlugin.inst().getSelfTriggerManager().unregisterSelfTrigger(event.getChunk());
    }
}