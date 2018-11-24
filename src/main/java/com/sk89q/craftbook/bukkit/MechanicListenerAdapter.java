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

import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.mechanics.minecart.blocks.CartBlockMechanism;
import com.sk89q.craftbook.mechanics.minecart.blocks.CartMechanismBlocks;
import com.sk89q.craftbook.mechanics.minecart.events.CartBlockEnterEvent;
import com.sk89q.craftbook.mechanics.minecart.events.CartBlockImpactEvent;
import com.sk89q.craftbook.mechanics.minecart.events.CartBlockRedstoneEvent;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.ProtectionUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.events.SignClickEvent;
import com.sk89q.craftbook.util.events.SourcedBlockRedstoneEvent;
import com.sk89q.craftbook.util.exceptions.InvalidMechanismException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.AnaloguePowerable;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.RedstoneWire;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashSet;
import java.util.Set;

/**
 * This adapter hooks a mechanic manager up to Bukkit.
 *
 * @author sk89q
 */
final class MechanicListenerAdapter implements Listener {

    private Set<String> signClickTimer = new HashSet<>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(final PlayerInteractEvent event) {

        if (!EventUtil.passesFilter(event))
            return;

        Block block = null;
        Action action = null;
        if(event.getAction() == Action.RIGHT_CLICK_AIR) {
            try {
                block = event.getPlayer().getTargetBlock(null, 5);
                if(block != null && block.getType() != Material.AIR)
                    action = Action.RIGHT_CLICK_BLOCK;
                else
                    action = Action.RIGHT_CLICK_AIR;
            } catch(Exception e) {
                //Bukkit randomly errors. Catch the error.
            }
        } else {
            block = event.getClickedBlock();
            action = event.getAction();
        }

        if(block != null && SignUtil.isSign(block) && event.getHand() == EquipmentSlot.HAND) {
            if(CraftBookPlugin.inst().getConfiguration().signClickTimeout > 0) {
                if(signClickTimer.contains(event.getPlayer().getName())) {
                    return;
                } else {
                    signClickTimer.add(event.getPlayer().getName());
                    Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), () -> signClickTimer.remove(event.getPlayer().getName()), CraftBookPlugin.inst().getConfiguration().signClickTimeout);
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

    private static void checkBlockChange(Player player, Block block, boolean build) {
        switch(block.getType()) {
            case REDSTONE_TORCH:
            case REDSTONE_WALL_TORCH:
            case REDSTONE_BLOCK:
                if(CraftBookPlugin.inst().getConfiguration().pedanticBlockChecks && !ProtectionUtil.canBuild(player, block.getLocation(), build))
                    break;
                handleRedstoneForBlock(block, build ? 0 : 15, build ? 15 : 0);
                break;
            case ACACIA_BUTTON:
            case BIRCH_BUTTON:
            case DARK_OAK_BUTTON:
            case JUNGLE_BUTTON:
            case OAK_BUTTON:
            case SPRUCE_BUTTON:
            case STONE_BUTTON:
            case LEVER:
            case DETECTOR_RAIL:
            case STONE_PRESSURE_PLATE:
            case ACACIA_PRESSURE_PLATE:
            case BIRCH_PRESSURE_PLATE:
            case DARK_OAK_PRESSURE_PLATE:
            case JUNGLE_PRESSURE_PLATE:
            case OAK_PRESSURE_PLATE:
            case SPRUCE_PRESSURE_PLATE:
            case COMPARATOR:
            case REPEATER:
                if(CraftBookPlugin.inst().getConfiguration().pedanticBlockChecks && !ProtectionUtil.canBuild(player, block.getLocation(), build))
                    break;
                Powerable powerable = (Powerable) block.getBlockData();
                if(powerable.isPowered())
                    handleRedstoneForBlock(block, build ? 0 : 15, build ? 15 : 0);
                break;
            case HEAVY_WEIGHTED_PRESSURE_PLATE:
            case LIGHT_WEIGHTED_PRESSURE_PLATE:
            case REDSTONE_WIRE:
                if(CraftBookPlugin.inst().getConfiguration().pedanticBlockChecks && !ProtectionUtil.canBuild(player, block.getLocation(), build))
                    break;
                AnaloguePowerable analoguePowerable = (AnaloguePowerable) block.getBlockData();
                if(analoguePowerable.getPower() > 0) {
                    handleRedstoneForBlock(block, build ? 0 : analoguePowerable.getPower(), build ? analoguePowerable.getPower() : 0);
                }
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

    private static void handleRedstoneForBlock(Block block, int oldLevel, int newLevel) {

        World world = block.getWorld();

        // Give the method a BlockWorldVector instead of a Block
        boolean wasOn = oldLevel >= 1;
        boolean isOn = newLevel >= 1;
        boolean wasChange = wasOn != isOn;

        // For efficiency reasons, we're only going to consider changes between
        // off and on state, and ignore simple current changes (i.e. 15->13)
        if (!wasChange) return;

        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        // When this hook has been called, the level in the world has not
        // yet been updated, so we're going to do this very ugly thing of
        // faking the value with the new one whenever the data value of this
        // block is requested -- it is quite ugly
        switch(block.getType()) {
            case REDSTONE_WIRE:
                if (CraftBookPlugin.inst().getConfiguration().indirectRedstone) {

                    // power all blocks around the redstone wire on the same y level
                    // north/south
                    handleDirectWireInput(x - 1, y, z, block, oldLevel, newLevel);
                    handleDirectWireInput(x + 1, y, z, block, oldLevel, newLevel);
                    // east/west
                    handleDirectWireInput(x, y, z - 1, block, oldLevel, newLevel);
                    handleDirectWireInput(x, y, z + 1, block, oldLevel, newLevel);

                    // Can be triggered from below
                    handleDirectWireInput(x, y + 1, z, block, oldLevel, newLevel);

                    // Can be triggered from above (Eg, glass->glowstone like redstone lamps)
                    handleDirectWireInput(x, y - 1, z, block, oldLevel, newLevel);
                } else {

                    Material above = world.getBlockAt(x, y + 1, z).getType();

                    Material westSide = world.getBlockAt(x, y, z + 1).getType();
                    Material westSideAbove = world.getBlockAt(x, y + 1, z + 1).getType();
                    Material westSideBelow = world.getBlockAt(x, y - 1, z + 1).getType();
                    Material eastSide = world.getBlockAt(x, y, z - 1).getType();
                    Material eastSideAbove = world.getBlockAt(x, y + 1, z - 1).getType();
                    Material eastSideBelow = world.getBlockAt(x, y - 1, z - 1).getType();

                    Material northSide = world.getBlockAt(x - 1, y, z).getType();
                    Material northSideAbove = world.getBlockAt(x - 1, y + 1, z).getType();
                    Material northSideBelow = world.getBlockAt(x - 1, y - 1, z).getType();
                    Material southSide = world.getBlockAt(x + 1, y, z).getType();
                    Material southSideAbove = world.getBlockAt(x + 1, y + 1, z).getType();
                    Material southSideBelow = world.getBlockAt(x + 1, y - 1, z).getType();

                    // Make sure that the wire points to only this block
                    if (!CraftBookBukkitUtil.isRedstoneBlock(westSide) && !CraftBookBukkitUtil.isRedstoneBlock(eastSide)
                            && (!CraftBookBukkitUtil.isRedstoneBlock(westSideAbove) || westSide == Material.AIR || above != Material.AIR)
                            && (!CraftBookBukkitUtil.isRedstoneBlock(eastSideAbove) || eastSide == Material.AIR || above != Material.AIR)
                            && (!CraftBookBukkitUtil.isRedstoneBlock(westSideBelow) || westSide != Material.AIR)
                            && (!CraftBookBukkitUtil.isRedstoneBlock(eastSideBelow) || eastSide != Material.AIR)) {
                        // Possible blocks north / south
                        handleDirectWireInput(x - 1, y, z, block, oldLevel, newLevel);
                        handleDirectWireInput(x + 1, y, z, block, oldLevel, newLevel);
                        handleDirectWireInput(x - 1, y - 1, z, block, oldLevel, newLevel);
                        handleDirectWireInput(x + 1, y - 1, z, block, oldLevel, newLevel);
                    }

                    if (!CraftBookBukkitUtil.isRedstoneBlock(northSide) && !CraftBookBukkitUtil.isRedstoneBlock(southSide)
                            && (!CraftBookBukkitUtil.isRedstoneBlock(northSideAbove) || northSide == Material.AIR || above != Material.AIR)
                            && (!CraftBookBukkitUtil.isRedstoneBlock(southSideAbove) || southSide == Material.AIR || above != Material.AIR)
                            && (!CraftBookBukkitUtil.isRedstoneBlock(northSideBelow) || northSide != Material.AIR)
                            && (!CraftBookBukkitUtil.isRedstoneBlock(southSideBelow) || southSide != Material.AIR)) {
                        // Possible blocks west / east
                        handleDirectWireInput(x, y, z - 1, block, oldLevel, newLevel);
                        handleDirectWireInput(x, y, z + 1, block, oldLevel, newLevel);
                        handleDirectWireInput(x, y - 1, z - 1, block, oldLevel, newLevel);
                        handleDirectWireInput(x, y - 1, z + 1, block, oldLevel, newLevel);
                    }

                    // Can be triggered from below
                    handleDirectWireInput(x, y + 1, z, block, oldLevel, newLevel);

                    // Can be triggered from above
                    handleDirectWireInput(x, y - 1, z, block, oldLevel, newLevel);
                }
                return;
            case REPEATER:
            case COMPARATOR:
                Directional diode = (Directional) block.getBlockData();
                BlockFace f = diode.getFacing();
                handleDirectWireInput(x + f.getModX(), y, z + f.getModZ(), block, oldLevel, newLevel);
                if(block.getRelative(f).getType() != Material.AIR) {
                    handleDirectWireInput(x + f.getModX(), y - 1, z + f.getModZ(), block, oldLevel, newLevel);
                    handleDirectWireInput(x + f.getModX(), y + 1, z + f.getModZ(), block, oldLevel, newLevel);
                    handleDirectWireInput(x + f.getModX() + 1, y - 1, z + f.getModZ(), block, oldLevel, newLevel);
                    handleDirectWireInput(x + f.getModX() - 1, y - 1, z + f.getModZ(), block, oldLevel, newLevel);
                    handleDirectWireInput(x + f.getModX() + 1, y - 1, z + f.getModZ() + 1, block, oldLevel, newLevel);
                    handleDirectWireInput(x + f.getModX() - 1, y - 1, z + f.getModZ() - 1, block, oldLevel, newLevel);
                }
                return;
            case ACACIA_BUTTON:
            case BIRCH_BUTTON:
            case DARK_OAK_BUTTON:
            case JUNGLE_BUTTON:
            case OAK_BUTTON:
            case SPRUCE_BUTTON:
            case STONE_BUTTON:
            case LEVER:
                Directional button = (Directional) block.getBlockData();
                if(button != null) {
                    BlockFace face = button.getFacing().getOppositeFace();
                    if(face != null)
                        handleDirectWireInput(x + face.getModX()*2, y + face.getModY()*2, z + face.getModZ()*2, block, oldLevel, newLevel);
                }
                break;
            case POWERED_RAIL:
            case ACTIVATOR_RAIL:
                return;
        }

        // For redstone wires and repeaters, the code already exited this method
        // Non-wire blocks proceed

        handleDirectWireInput(x - 1, y, z, block, oldLevel, newLevel);
        handleDirectWireInput(x + 1, y, z, block, oldLevel, newLevel);
        handleDirectWireInput(x - 1, y - 1, z, block, oldLevel, newLevel);
        handleDirectWireInput(x + 1, y - 1, z, block, oldLevel, newLevel);
        handleDirectWireInput(x, y, z - 1, block, oldLevel, newLevel);
        handleDirectWireInput(x, y, z + 1, block, oldLevel, newLevel);
        handleDirectWireInput(x, y - 1, z - 1, block, oldLevel, newLevel);
        handleDirectWireInput(x, y - 1, z + 1, block, oldLevel, newLevel);

        // Can be triggered from below
        handleDirectWireInput(x, y + 1, z, block, oldLevel, newLevel);

        // Can be triggered from above
        handleDirectWireInput(x, y - 1, z, block, oldLevel, newLevel);
    }

    /**
     * Handle the direct wire input.
     *
     * @param x
     * @param y
     * @param z
     * @param sourceBlock
     * @param oldLevel
     * @param newLevel
     */
    private static void handleDirectWireInput(int x, int y, int z, Block sourceBlock, int oldLevel, int newLevel) {

        Block block = sourceBlock.getWorld().getBlockAt(x, y, z);
        if(CraftBookBukkitUtil.equals(sourceBlock.getLocation(), block.getLocation())) //The same block, don't run.
            return;
        final SourcedBlockRedstoneEvent event = new SourcedBlockRedstoneEvent(sourceBlock, block, oldLevel, newLevel);

        CraftBookPlugin.inst().getServer().getPluginManager().callEvent(event);

        if(CraftBookPlugin.inst().useLegacyCartSystem) {
            CraftBookPlugin.server().getScheduler().runTask(CraftBookPlugin.inst(), () -> {
                try {
                    CartMechanismBlocks cmb = CartMechanismBlocks.find(event.getBlock());
                    CartBlockRedstoneEvent ev = new CartBlockRedstoneEvent(event.getBlock(), event.getSource(), event.getOldCurrent(), event.getNewCurrent(), cmb, CartBlockMechanism.getCart(cmb.rail));
                    CraftBookPlugin.inst().getServer().getPluginManager().callEvent(ev);
                } catch (InvalidMechanismException ignored) {
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleMove(VehicleMoveEvent event) {

        if (!EventUtil.passesFilter(event))
            return;

        if(CraftBookPlugin.inst().useLegacyCartSystem) {
            if (event.getVehicle() instanceof Minecart) {
                try {
                    Minecart cart = (Minecart) event.getVehicle();
                    CartMechanismBlocks cmb = CartMechanismBlocks.findByRail(event.getTo().getBlock());
                    cmb.setFromBlock(event.getFrom().getBlock());
                    Location from = event.getFrom();
                    Location to = event.getTo();
                    if (LocationUtil.getDistanceSquared(from, to) > 2 * 2) //Further than max distance
                        return;
                    boolean minor = from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ();
                    CartBlockImpactEvent ev = new CartBlockImpactEvent(cart, from, to, cmb, minor);
                    CraftBookPlugin.inst().getServer().getPluginManager().callEvent(ev);
                } catch (InvalidMechanismException ignored) {
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleEnter(VehicleEnterEvent event) {

        if (!EventUtil.passesFilter(event))
            return;

        if(!event.getVehicle().getWorld().isChunkLoaded(event.getVehicle().getLocation().getBlockX() >> 4, event.getVehicle().getLocation().getBlockZ() >> 4))
            return;

        if(CraftBookPlugin.inst().useLegacyCartSystem) {
            if (event.getVehicle() instanceof Minecart) {
                try {
                    Minecart cart = (Minecart) event.getVehicle();
                    Block block = event.getVehicle().getLocation().getBlock();
                    CartMechanismBlocks cmb = CartMechanismBlocks.findByRail(block);
                    cmb.setFromBlock(block); // WAI
                    CartBlockEnterEvent ev = new CartBlockEnterEvent(cart, event.getEntered(), cmb);
                    CraftBookPlugin.inst().getServer().getPluginManager().callEvent(ev);
                    if (ev.isCancelled())
                        event.setCancelled(true);
                } catch (InvalidMechanismException ignored) {
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSignChange(SignChangeEvent event) {
        for (int i = 0; i < 4; i++) {
            String line = event.getLine(i);
            if (line.startsWith("&0") || line.startsWith("\u00A70")) {
                line = line.substring(2);
                event.setLine(i, line);
            }
        }
    }
}