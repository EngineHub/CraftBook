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

package org.enginehub.craftbook.mechanics;

import com.comphenix.protocol.AsynchronousManager;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.async.AsyncListenerHandler;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.collect.Sets;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.blocks.Blocks;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.util.BlockSyntax;
import org.enginehub.craftbook.util.BlockUtil;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.LocationUtil;
import org.enginehub.craftbook.util.ProtectionUtil;
import org.enginehub.craftbook.util.SignUtil;
import org.enginehub.craftbook.util.TernaryState;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class Chairs extends AbstractCraftBookMechanic {

    private final Map<UUID, ChairData> chairs = new HashMap<>();
    private NamespacedKey chairDataKey;

    private AsyncListenerHandler positionLookHandler;
    private AsyncListenerHandler vehicleSteerHandler;

    @Override
    public void enable() {
        chairDataKey = new NamespacedKey(CraftBookPlugin.inst(), "is_chair");

        Bukkit.getScheduler().runTaskTimer(CraftBookPlugin.inst(), new ChairChecker(), 20L, 20L);

        AsynchronousManager packetManager = ProtocolLibrary
            .getProtocolManager()
            .getAsynchronousManager();

        vehicleSteerHandler = packetManager.registerAsyncHandler(new VehicleSteerPacketHandler());
        positionLookHandler = packetManager.registerAsyncHandler(new PositionLookPacketHandler());

        vehicleSteerHandler.syncStart();
        positionLookHandler.syncStart();
    }

    @Override
    public void disable() {
        chairs.clear();

        AsynchronousManager packetManager = ProtocolLibrary
            .getProtocolManager()
            .getAsynchronousManager();

        packetManager.unregisterAsyncHandler(positionLookHandler);
        packetManager.unregisterAsyncHandler(vehicleSteerHandler);
    }

    private Entity createChairEntity(Block block) {
        ArmorStand chairEntity = block.getWorld().spawn(BlockUtil.getBlockCentre(block).subtract(0, 1.5, 0), ArmorStand.class);

        chairEntity.getPersistentDataContainer().set(chairDataKey, PersistentDataType.BYTE, (byte) 1);

        chairEntity.setTicksLived(1);
        chairEntity.setInvulnerable(true);
        chairEntity.setGravity(false);
        chairEntity.setSilent(true);
        chairEntity.setVisible(false);

        return chairEntity;
    }

    private void addChair(Player player, Block block, Location chairLoc) {
        Entity ar = null;
        boolean isNew = false;
        if (chairs.containsKey(player.getUniqueId())) {
            ar = chairs.get(player.getUniqueId()).chairEntity;
        }

        if (ar == null || ar.isDead() || !ar.isValid()) {
            ar = createChairEntity(block);
            isNew = true;
        }

        if (chairLoc != null) {
            ar.getLocation().setYaw(chairLoc.getYaw());
        }

        Entity far = ar;
        if (ar.isEmpty() && isNew) {
            Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), () -> {
                if (chairLoc != null)
                    player.teleport(chairLoc);
                far.addPassenger(player);
            });
        } else if (ar.isEmpty()) {
            removeChair(player);
            return;
        }

        ChairData chairData;
        if (chairs.containsKey(player.getUniqueId())) {
            chairData = chairs.get(player.getUniqueId());
            chairData.chairEntity = ar;
        } else {
            chairData = new ChairData(ar, block, player.getLocation().clone());
        }

        chairs.put(player.getUniqueId(), chairData);
    }

    private void removeChair(Player player) {
        final ChairData chairData = chairs.get(player.getUniqueId());
        final Entity chairEntity = chairData.chairEntity;
        if (chairEntity != null) {
            chairEntity.eject();
            player.eject();
            chairEntity.remove();
            if (exitToLastPosition && chairData.playerExitPoint != null) {
                Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), () -> {
                    player.teleport(chairData.playerExitPoint);
                    player.setSneaking(false);
                }, 5L);
            }
        }

        chairs.remove(player.getUniqueId());
    }

    private boolean hasSign(Block block) {
        return hasSign(block, Sets.newHashSet(), block);
    }

    private boolean hasSign(Block block, Collection<Location> searched, Block original) {
        boolean found = false;

        for (BlockFace face : LocationUtil.getDirectFaces()) {
            Block otherBlock = block.getRelative(face);

            if (searched.contains(otherBlock.getLocation())) {
                continue;
            }
            searched.add(otherBlock.getLocation());

            if (found) {
                break;
            }

            if (block.getLocation().distanceSquared(original.getLocation()) > Math.pow(maxSignDistance, 2)) {
                continue;
            }

            if (SignUtil.isSign(otherBlock) && SignUtil.getFront(otherBlock) == face) {
                found = true;
                break;
            }

            if (BlockUtil.areBlocksIdentical(block, otherBlock)) {
                found = hasSign(otherBlock, searched, original);
            }
        }

        return found;
    }

    private boolean hasChair(CraftBookPlayer player) {
        return chairs.containsKey(player.getUniqueId());
    }

    private boolean hasChair(Block block) {
        for (ChairData data : chairs.values()) {
            if (block.equals(data.location)) {
                return true;
            }
        }

        return false;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        if (hasChair(event.getBlock())) {
            CraftBookPlugin.inst().wrapPlayer(event.getPlayer()).printError(TranslatableComponent.of("craftbook.chairs.block-in-use"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDisconnect(PlayerQuitEvent event) {
        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if (hasChair(player)) {
            removeChair(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onKick(PlayerKickEvent event) {
        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if (hasChair(player)) {
            removeChair(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChunkUnload(ChunkUnloadEvent event) {
        // Cleanup missed chair entities.
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity.getType() == EntityType.ARMOR_STAND
                && entity.getPersistentDataContainer().has(chairDataKey, PersistentDataType.BYTE)) {
                entity.remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(PlayerInteractEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        if (event.getHand() != EquipmentSlot.HAND || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getClickedBlock() == null || !Blocks.containsFuzzy(allowedBlocks, BukkitAdapter.adapt(event.getClickedBlock().getBlockData()))) {
            return;
        }

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (!allowSneaking.doesPass(player.isSneaking()) || (!allowHeldBlock && player.isHoldingBlock())) {
            return;
        }

        if (requireSign && !hasSign(event.getClickedBlock())) {
            return;
        }

        if (!player.hasPermission("craftbook.chairs.use")) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                player.printError("mech.use-permission");
            }
            return;
        }
        if (!ProtectionUtil.canUse(event.getPlayer(), event.getClickedBlock().getLocation(), event.getBlockFace(), event.getAction())) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                player.printError("area.use-permissions");
            }
            return;
        }

        // At this point they probably intend to use the chair
        event.setCancelled(true);

        if (event.getPlayer().getLocation().distanceSquared(event.getClickedBlock().getLocation().add(0.5, 0.5, 0.5)) > Math.pow(maxClickRadius, 2)) {
            player.printError(TranslatableComponent.of("craftbook.chairs.too-far-away"));
            return;
        }

        if (hasChair(player)) {
            ChairData data = chairs.get(player.getUniqueId());
            if (!event.getClickedBlock().equals(data.location)) {
                // Remove their first chair
                removeChair(event.getPlayer());
            } else {
                // Already in this chair. Do nothing.
                return;
            }
        }

        if (hasChair(event.getClickedBlock())) {
            player.printError(TranslatableComponent.of("craftbook.chairs.block-in-use"));
            return;
        }

        if (!event.getClickedBlock().getRelative(0, -1, 0).getType().isSolid()) {
            player.printError(TranslatableComponent.of("craftbook.chairs.floating"));
            return;
        } else if (event.getClickedBlock().getRelative(0, 1, 0).getType().isSolid()) {
            player.printError(TranslatableComponent.of("craftbook.chairs.obstructed"));
            return;
        }

        Location chairLoc = event.getClickedBlock().getLocation().add(0.5, 0, 0.5);

        BlockData blockData = event.getClickedBlock().getBlockData();
        if (faceWhenPossible && blockData instanceof Directional) {
            BlockFace direction = ((Directional) blockData).getFacing().getOppositeFace();

            chairLoc.setYaw(LocationUtil.getYawFromFace(direction));
            chairLoc.setPitch(0);
        } else {
            chairLoc.setPitch(event.getPlayer().getLocation().getPitch());
            chairLoc.setYaw(event.getPlayer().getLocation().getYaw());
        }

        addChair(event.getPlayer(), event.getClickedBlock(), chairLoc);
    }

    private class ChairChecker implements Runnable {
        @Override
        public void run() {
            for (Map.Entry<UUID, ChairData> pl : chairs.entrySet()) {
                Player p = Bukkit.getPlayer(pl.getKey());

                if (p == null || p.isDead() || !p.isValid()) {
                    ChairData data = chairs.remove(pl.getKey());
                    if (data != null && data.chairEntity != null) {
                        Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), () -> {
                            data.chairEntity.eject();
                            data.chairEntity.remove();
                        }, 5);
                    }
                    continue;
                }

                if (!Blocks.containsFuzzy(allowedBlocks, BukkitAdapter.adapt(pl.getValue().location.getBlockData()))
                    || !p.getWorld().equals(pl.getValue().location.getWorld())
                    || LocationUtil.getDistanceSquared(p.getLocation(), pl.getValue().location.getLocation()) > 2) {
                    removeChair(p);
                } else {
                    addChair(p, pl.getValue().location, null); // For any new players.

                    if (regenHealth) {
                        AttributeInstance maxHealthAttr = p.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                        if (maxHealthAttr != null && p.getHealth() < maxHealthAttr.getValue()) {
                            p.setHealth(Math.min(p.getHealth() + healAmount, maxHealthAttr.getValue()));
                        }
                    }
                    if (lowerExhaustion && p.getExhaustion() > -20d) {
                        p.setExhaustion((float) (p.getExhaustion() - 0.1d));
                    }
                }
            }
        }
    }

    private static final class ChairData {
        private Entity chairEntity;
        private final Block location;
        private final Location playerExitPoint;

        private ChairData(Entity entity, Block location, @Nullable Location playerExitPoint) {
            this.chairEntity = entity;
            this.location = location;
            this.playerExitPoint = playerExitPoint;
        }
    }

    private class VehicleSteerPacketHandler extends PacketAdapter {
        public VehicleSteerPacketHandler() {
            super(PacketAdapter
                .params(CraftBookPlugin.inst(), PacketType.Play.Client.STEER_VEHICLE)
                .clientSide()
                .listenerPriority(ListenerPriority.HIGHEST)
                .options(ListenerOptions.INTERCEPT_INPUT_BUFFER));
        }

        @Override
        public void onPacketReceiving(PacketEvent event) {
            if (!event.isCancelled()) {
                if (event.getPacket().getBooleans().getValues().get(1)) {
                    CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
                    if (hasChair(player)) {
                        removeChair(event.getPlayer());
                    }
                }
            }
        }
    }

    private class PositionLookPacketHandler extends PacketAdapter {
        public PositionLookPacketHandler() {
            super(PacketAdapter
                .params(CraftBookPlugin.inst(), PacketType.Play.Client.ENTITY_ACTION)
                .clientSide()
                .listenerPriority(ListenerPriority.HIGHEST)
                .options(ListenerOptions.INTERCEPT_INPUT_BUFFER));
        }

        @Override
        public void onPacketReceiving(PacketEvent event) {
            if (!event.isCancelled()) {
                CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
                if (hasChair(player)) {
                    removeChair(event.getPlayer());
                }
            }
        }
    }

    private boolean allowHeldBlock;
    private TernaryState allowSneaking;
    private boolean regenHealth;
    private boolean lowerExhaustion;
    private double healAmount;
    private List<BaseBlock> allowedBlocks;
    private boolean faceWhenPossible;
    private boolean requireSign;
    private boolean exitToLastPosition;
    private int maxSignDistance;
    private int maxClickRadius;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("allow-holding-blocks", "Allow players to sit in chairs when holding blocks.");
        allowHeldBlock = config.getBoolean("allow-holding-blocks", false);

        config.setComment("allow-sneaking", "Allow players to sit in chairs while sneaking.");
        allowSneaking = TernaryState.parseTernaryState(config.getString("allow-sneaking", TernaryState.FALSE.toString()));

        config.setComment("regen-health", "Regenerate health passively when seated.");
        regenHealth = config.getBoolean("regen-health", true);

        config.setComment("lower-exhaustion", "Lower the player's exhaustion level when seated.");
        lowerExhaustion = config.getBoolean("lower-exhaustion", true);

        config.setComment("regen-health-amount", "The amount of health regenerated passively. (Can be decimal)");
        healAmount = config.getDouble("regen-health-amount", 1);

        config.setComment("blocks", "A list of blocks that can be sat on.");
        allowedBlocks =
            BlockSyntax.getBlocks(config.getStringList("blocks", BlockCategories.STAIRS.getAll()
                .stream()
                .map(BlockType::getId)
                .sorted(String::compareToIgnoreCase)
                .collect(Collectors.toList())
            ), true);

        config.setComment("face-correct-direction", "When the player sits, automatically face them the direction of the chair. (If possible)");
        faceWhenPossible = config.getBoolean("face-correct-direction", true);

        config.setComment("require-sign", "Require a sign to be attached to the chair in order to work!");
        requireSign = config.getBoolean("require-sign", false);

        config.setComment("max-sign-distance", "The maximum distance between the click point and the sign. (When require sign is on)");
        maxSignDistance = config.getInt("max-sign-distance", 3);

        config.setComment("max-click-radius", "The maximum distance the player can be from the sign.");
        maxClickRadius = config.getInt("max-click-radius", 5);

        config.setComment("exit-to-last-position", "Teleport players to their last position when they exit the chair.");
        exitToLastPosition = config.getBoolean("exit-to-last-position", false);
    }
}
