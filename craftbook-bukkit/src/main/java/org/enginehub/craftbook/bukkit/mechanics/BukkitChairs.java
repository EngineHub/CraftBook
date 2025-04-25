/*
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

package org.enginehub.craftbook.bukkit.mechanics;

import com.sk89q.worldedit.blocks.Blocks;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
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
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.mechanics.Chairs;
import org.enginehub.craftbook.util.BlockUtil;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.LocationUtil;
import org.enginehub.craftbook.util.ProtectionUtil;
import org.enginehub.craftbook.util.SignUtil;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class BukkitChairs extends Chairs implements Listener {
    private final Map<UUID, ChairData> chairs = new HashMap<>();
    private final NamespacedKey chairDataKey = new NamespacedKey("craftbook", "is_chair");
    private @Nullable BukkitTask tickerTask;

    public BukkitChairs(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @Override
    public void enable() {
        tickerTask = Bukkit.getScheduler().runTaskTimer(CraftBookPlugin.inst(), new ChairChecker(), 20L, 20L);
    }

    @Override
    public void disable() {
        chairs.clear();
        if (tickerTask != null) {
            tickerTask.cancel();
            tickerTask = null;
        }
    }

    private Entity createChairEntity(Block block, @Nullable Vector direction) {
        double height = block.getBoundingBox().getHeight();
        if (Tag.STAIRS.isTagged(block.getType())) {
            // Override this to provide correct heights for stairs.
            height = 0.5;
        }

        Location location = block.getLocation().toCenterLocation().subtract(0, ARMOR_STAND_MOUNT_Y - height, 0);
        if (direction != null) {
            location.setDirection(direction);
        }

        ArmorStand chairEntity = block.getWorld().spawn(location, ArmorStand.class);

        chairEntity.getPersistentDataContainer().set(chairDataKey, PersistentDataType.BYTE, (byte) 1);

        chairEntity.setTicksLived(1);
        chairEntity.setInvulnerable(true);
        chairEntity.setGravity(false);
        chairEntity.setSilent(true);
        chairEntity.setVisible(false);

        return chairEntity;
    }

    private void addChair(Player player, Block block, @Nullable Location chairLoc) {
        Entity ar = null;
        boolean isNew = false;
        if (chairs.containsKey(player.getUniqueId())) {
            ar = chairs.get(player.getUniqueId()).chairEntity;
        }

        if (ar == null || ar.isDead() || !ar.isValid()) {
            ar = createChairEntity(block, chairLoc == null ? null : chairLoc.getDirection());
            isNew = true;
        }

        if (chairLoc != null) {
            ar.setRotation(chairLoc.getYaw(), chairLoc.getPitch());
        }

        Entity far = ar;
        if (ar.isEmpty() && isNew) {
            Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), () -> {
                if (chairLoc != null) {
                    player.teleport(chairLoc);
                }
                far.addPassenger(player);
            });
        } else if (ar.isEmpty()) {
            removeChair(player);
            return;
        }

        ChairData chairData;
        if (chairs.containsKey(player.getUniqueId())) {
            ChairData oldChairData = chairs.get(player.getUniqueId());
            // Create a new ChairData with the new entity
            chairData = new ChairData(ar, oldChairData.location, oldChairData.playerExitPoint);
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
        return hasSign(block, new HashSet<>(), block);
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

            if (!LocationUtil.isWithinSphericalRadius(block, original, maxSignDistance)) {
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
    public void onEntityDismount(EntityDismountEvent event) {
        if (event.getEntity() instanceof Player bukkitPlayer) {
            CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(bukkitPlayer);
            if (hasChair(player)) {
                removeChair(bukkitPlayer);
            }
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
                player.printError(TranslatableComponent.of("craftbook.mechanisms.use-permission", TextComponent.of(getMechanicType().getName())));
            }
            return;
        }
        if (!ProtectionUtil.canUse(event.getPlayer(), event.getClickedBlock().getLocation(), event.getBlockFace(), event.getAction())) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                player.printError(TranslatableComponent.of("craftbook.mechanisms.protection-blocked", TextComponent.of(getMechanicType().getName())));
            }
            return;
        }

        // At this point they probably intend to use the chair
        event.setCancelled(true);

        if (!LocationUtil.isWithinSphericalRadius(event.getPlayer().getLocation(), event.getClickedBlock().getLocation().toCenterLocation(), maxClickRadius)) {
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
            if (Bukkit.getServer().isPaused()) {
                // Don't check chairs when the server is paused.
                return;
            }

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
                    || !LocationUtil.isWithinSphericalRadius(p.getLocation(), pl.getValue().location.getLocation(), 2)) {
                    removeChair(p);
                } else {
                    addChair(p, pl.getValue().location, null); // For any new players.

                    if (regenHealth) {
                        AttributeInstance maxHealthAttr = p.getAttribute(Attribute.MAX_HEALTH);
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

    private record ChairData(Entity chairEntity, Block location, @Nullable Location playerExitPoint) {
    }
}
