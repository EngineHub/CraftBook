/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
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

package org.enginehub.craftbook.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.data.AnaloguePowerable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.RedstoneWire;
import org.bukkit.entity.Minecart;
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
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.mechanic.exception.InvalidMechanismException;
import org.enginehub.craftbook.mechanics.minecart.blocks.CartBlockMechanism;
import org.enginehub.craftbook.mechanics.minecart.blocks.CartMechanismBlocks;
import org.enginehub.craftbook.mechanics.minecart.events.CartBlockEnterEvent;
import org.enginehub.craftbook.mechanics.minecart.events.CartBlockImpactEvent;
import org.enginehub.craftbook.mechanics.minecart.events.CartBlockRedstoneEvent;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.HistoryHashMap;
import org.enginehub.craftbook.util.LocationUtil;
import org.enginehub.craftbook.util.SignUtil;
import org.enginehub.craftbook.util.events.SignClickEvent;
import org.enginehub.craftbook.util.events.SourcedBlockRedstoneEvent;

import java.util.Map;
import java.util.UUID;

/**
 * This adapter hooks a mechanic manager up to Bukkit.
 */
final class MechanicListenerAdapter implements Listener {

    private final Map<UUID, Long> signClickTimer = new HistoryHashMap<>(10);

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (SignClickEvent.getHandlerList().getRegisteredListeners().length == 0) {
            // Don't run this code when we have no listeners.
            return;
        }

        if (!EventUtil.passesFilter(event)) {
            return;
        }

        // TODO Determine if still necessary
        Block block;
        Action action = null;
        if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            block = event.getPlayer().getTargetBlock(null, 5);
            if (!block.getType().isAir()) {
                action = Action.RIGHT_CLICK_BLOCK;
            }
        } else {
            block = event.getClickedBlock();
            action = event.getAction();
        }

        if (block != null && SignUtil.isSign(block) && event.getHand() == EquipmentSlot.HAND) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().signClickTimeout > 0) {
                long lastClick = signClickTimer.computeIfAbsent(event.getPlayer().getUniqueId(), uuid -> 0L);
                if (lastClick > System.currentTimeMillis() - (CraftBook.getInstance().getPlatform().getConfiguration().signClickTimeout)) {
                    return;
                } else {
                    signClickTimer.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
                }
            }
            SignClickEvent ev = new SignClickEvent(event.getPlayer(), action, event.getItem(), block, event.getBlockFace());
            CraftBookPlugin.inst().getServer().getPluginManager().callEvent(ev);
            if (ev.isCancelled()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (SourcedBlockRedstoneEvent.getHandlerList().getRegisteredListeners().length == 0
            && CartBlockRedstoneEvent.getHandlerList().getRegisteredListeners().length == 0) {
            // Don't run this code when we have no listeners.
            return;
        }

        if (!EventUtil.passesFilter(event)) {
            return;
        }

        checkBlockChange(event.getBlock(), false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (SourcedBlockRedstoneEvent.getHandlerList().getRegisteredListeners().length == 0
            && CartBlockRedstoneEvent.getHandlerList().getRegisteredListeners().length == 0) {
            // Don't run this code when we have no listeners.
            return;
        }

        if (!EventUtil.passesFilter(event)) {
            return;
        }

        checkBlockChange(event.getBlock(), true);
    }

    private static void checkBlockChange(Block block, boolean build) {
        Material type = block.getType();
        int power = 0;

        if (type == Material.REDSTONE_BLOCK) {
            power = 15;
        } else if (type == Material.REDSTONE_WALL_TORCH || type == Material.REDSTONE_TORCH) {
            Lightable lightable = (Lightable) block.getBlockData();
            if (lightable.isLit()) {
                power = 15;
            }
        } else {
            BlockData blockData = block.getBlockData();

            if (blockData instanceof Powerable) {
                if (((Powerable) blockData).isPowered()) {
                    power = 15;
                }
            } else if (blockData instanceof AnaloguePowerable) {
                power = ((AnaloguePowerable) blockData).getPower();
            }
        }

        if (power != 0) {
            handleRedstoneForBlock(block, build ? 0 : power, build ? power : 0);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        if (SourcedBlockRedstoneEvent.getHandlerList().getRegisteredListeners().length == 0
            && CartBlockRedstoneEvent.getHandlerList().getRegisteredListeners().length == 0) {
            // Don't run this code when we have no listeners.
            return;
        }

        if (!EventUtil.passesFilter(event)) {
            return;
        }

        handleRedstoneForBlock(event.getBlock(), event.getOldCurrent(), event.getNewCurrent());
    }

    private static void handleRedstoneForBlock(Block block, int oldLevel, int newLevel) {
        boolean wasOn = oldLevel >= 1;
        boolean isOn = newLevel >= 1;
        boolean wasChange = wasOn != isOn;

        // For efficiency reasons, we're only going to consider changes between
        // off and on state, and ignore simple current changes (i.e. 15->13)
        if (!wasChange) {
            return;
        }

        // When this hook has been called, the level in the world has not
        // yet been updated, so we're going to do this very ugly thing of
        // faking the value with the new one whenever the data value of this
        // block is requested -- it is quite ugly
        switch (block.getType()) {
            case REDSTONE_WIRE:
                if (CraftBook.getInstance().getPlatform().getConfiguration().indirectRedstone) {
                    // power all blocks around the redstone wire on the same y level
                    // north/south
                    handleDirectWireInput(block.getRelative(BlockFace.NORTH), block, oldLevel, newLevel);
                    handleDirectWireInput(block.getRelative(BlockFace.SOUTH), block, oldLevel, newLevel);
                    // east/west
                    handleDirectWireInput(block.getRelative(BlockFace.EAST), block, oldLevel, newLevel);
                    handleDirectWireInput(block.getRelative(BlockFace.WEST), block, oldLevel, newLevel);
                } else {
                    RedstoneWire redstoneWire = (RedstoneWire) block.getBlockData();

                    // Redstone wire only connects to known redstone blocks, not signs etc.
                    int connectedFaces = 0;
                    for (BlockFace face : redstoneWire.getAllowedFaces()) {
                        if (redstoneWire.getFace(face) != RedstoneWire.Connection.NONE) {
                            connectedFaces++;
                        }
                    }

                    for (BlockFace face : redstoneWire.getAllowedFaces()) {
                        RedstoneWire.Connection connection = redstoneWire.getFace(face);
                        // TODO Re-test in 1.16
                        if (connection != RedstoneWire.Connection.NONE
                            || connectedFaces == 0
                            || connectedFaces == 1 && redstoneWire.getFace(face.getOppositeFace()) != RedstoneWire.Connection.NONE) {
                            handleDirectWireInput(block.getRelative(face), block, oldLevel, newLevel);
                        }
                    }
                }

                // Can be triggered from below
                handleDirectWireInput(block.getRelative(BlockFace.UP), block, oldLevel, newLevel);

                // Can be triggered from above (Eg, glass->glowstone like redstone lamps)
                handleDirectWireInput(block.getRelative(BlockFace.DOWN), block, oldLevel, newLevel);
                return;
            case REPEATER:
            case COMPARATOR:
            case OBSERVER:
                Directional diode = (Directional) block.getBlockData();
                BlockFace f = diode.getFacing().getOppositeFace();
                Block relativeBlock = block.getRelative(f);
                handleDirectWireInput(relativeBlock, block, oldLevel, newLevel);

                if (relativeBlock.getPistonMoveReaction() != PistonMoveReaction.BREAK) {
                    handleRedstoneForBlock(relativeBlock, oldLevel, newLevel);
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
                BlockFace face = button.getFacing().getOppositeFace();
                handleRedstoneForBlock(block.getRelative(face), oldLevel, newLevel);
                break;
            case POWERED_RAIL:
            case ACTIVATOR_RAIL:
                return;
        }

        // For redstone wires and repeaters, the code already exited this method
        // Non-wire blocks proceed

        handleDirectWireInput(block.getRelative(BlockFace.WEST), block, oldLevel, newLevel);
        handleDirectWireInput(block.getRelative(BlockFace.EAST), block, oldLevel, newLevel);
        handleDirectWireInput(block.getRelative(BlockFace.WEST).getRelative(BlockFace.DOWN), block, oldLevel, newLevel);
        handleDirectWireInput(block.getRelative(BlockFace.EAST).getRelative(BlockFace.DOWN), block, oldLevel, newLevel);
        handleDirectWireInput(block.getRelative(BlockFace.NORTH), block, oldLevel, newLevel);
        handleDirectWireInput(block.getRelative(BlockFace.SOUTH), block, oldLevel, newLevel);
        handleDirectWireInput(block.getRelative(BlockFace.NORTH).getRelative(BlockFace.DOWN), block, oldLevel, newLevel);
        handleDirectWireInput(block.getRelative(BlockFace.SOUTH).getRelative(BlockFace.DOWN), block, oldLevel, newLevel);

        // Can be triggered from below
        handleDirectWireInput(block.getRelative(BlockFace.UP), block, oldLevel, newLevel);

        // Can be triggered from above
        handleDirectWireInput(block.getRelative(BlockFace.DOWN), block, oldLevel, newLevel);
    }

    /**
     * Handle the direct wire input.
     *
     * @param block The block that was triggered
     * @param sourceBlock The source block
     * @param oldLevel The old power level
     * @param newLevel The new power level
     */
    private static void handleDirectWireInput(Block block, Block sourceBlock, int oldLevel, int newLevel) {
        if (block.getBlockKey() == sourceBlock.getBlockKey()) { //The same block, don't run.
            return;
        }
        final SourcedBlockRedstoneEvent event = new SourcedBlockRedstoneEvent(sourceBlock, block, oldLevel, newLevel);

        CraftBookPlugin.inst().getServer().getPluginManager().callEvent(event);

        if (CartBlockRedstoneEvent.getHandlerList().getRegisteredListeners().length != 0) {
            Bukkit.getServer().getScheduler().runTask(CraftBookPlugin.inst(), () -> {
                try {
                    CartMechanismBlocks cmb = CartMechanismBlocks.find(event.getBlock());
                    CartBlockRedstoneEvent ev =
                        new CartBlockRedstoneEvent(event.getBlock(), event.getSource(), event.getOldCurrent(), event.getNewCurrent(), cmb,
                            CartBlockMechanism.getCart(cmb.rail));
                    CraftBookPlugin.inst().getServer().getPluginManager().callEvent(ev);
                } catch (InvalidMechanismException ignored) {
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleMove(VehicleMoveEvent event) {
        if (CartBlockImpactEvent.getHandlerList().getRegisteredListeners().length == 0) {
            // Don't run this code when we have no listeners.
            return;
        }

        if (!EventUtil.passesFilter(event)) {
            return;
        }

        if (event.getVehicle() instanceof Minecart) {
            try {
                Minecart cart = (Minecart) event.getVehicle();
                CartMechanismBlocks cmb = CartMechanismBlocks.findByRail(event.getTo().getBlock());
                cmb.setFromBlock(event.getFrom().getBlock());
                Location from = event.getFrom();
                Location to = event.getTo();
                if (!LocationUtil.isWithinSphericalRadius(from, to, 2)) {//Further than max distance
                    return;
                }
                boolean minor = from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ();
                CartBlockImpactEvent ev = new CartBlockImpactEvent(cart, from, to, cmb, minor);
                CraftBookPlugin.inst().getServer().getPluginManager().callEvent(ev);
            } catch (InvalidMechanismException ignored) {
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (CartBlockEnterEvent.getHandlerList().getRegisteredListeners().length == 0) {
            // Don't run this code when we have no listeners.
            return;
        }

        if (!EventUtil.passesFilter(event)) {
            return;
        }

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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSignChange(SignChangeEvent event) {
        // TODO Determine if necessary
        for (int i = 0; i < 4; i++) {
            String line = event.getLine(i);
            if (line.startsWith("&0") || line.startsWith("\u00A70")) {
                line = line.substring(2);
                event.setLine(i, line);
            }
        }
    }
}