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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.mechanic.exception.MechanicInitializationException;
import org.enginehub.craftbook.mechanics.PaintingSwitcher;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.LocationUtil;
import org.enginehub.craftbook.util.ProtectionUtil;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class BukkitPaintingSwitcher extends PaintingSwitcher implements Listener {

    private final BiMap<UUID, Painting> paintingMap = HashBiMap.create();
    private @Nullable List<NamespacedKey> artKeys;

    public BukkitPaintingSwitcher(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @Override
    public void enable() throws MechanicInitializationException {
        super.enable();

        var paintingRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.PAINTING_VARIANT);

        this.artKeys = paintingRegistry.stream()
                .map(paintingRegistry::getKey)
                .toList();
    }

    @Override
    public void disable() {
        super.disable();

        paintingMap.clear();
        this.artKeys = null;
    }

    public boolean isBeingEdited(Painting painting) {
        UUID playerUuid = paintingMap.inverse().get(painting);
        if (playerUuid != null && paintingMap.get(playerUuid) != null) {
            Player player = Bukkit.getPlayer(playerUuid);
            return player != null
                && LocationUtil.isWithinSphericalRadius(painting.getLocation(), player.getLocation(), modifyRange);
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        if (event.getHand() == EquipmentSlot.HAND && event.getRightClicked() instanceof Painting paint) {
            CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

            if (!player.hasPermission("craftbook.paintingswitcher.use")) {
                if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                    player.printError(TranslatableComponent.of("craftbook.mechanisms.use-permission", TextComponent.of(getMechanicType().getName())));
                }
                return;
            }

            if (!ProtectionUtil.canBuild(event.getPlayer(), paint.getLocation().getBlock())) {
                if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                    player.printError(TranslatableComponent.of("craftbook.mechanisms.protection-blocked", TextComponent.of(getMechanicType().getName())));
                }
                return;
            }

            if (!isBeingEdited(paint)) {
                paintingMap.put(player.getUniqueId(), paint);
                player.printInfo(TranslatableComponent.of("craftbook.paintingswitcher.now-editing"));
            } else if (paintingMap.inverse().get(paint).equals(player.getUniqueId())) {
                paintingMap.remove(player.getUniqueId());
                player.printInfo(TranslatableComponent.of("craftbook.paintingswitcher.no-longer-editing"));
            } else if (isBeingEdited(paint)) {
                Player otherPlayer = Bukkit.getPlayer(paintingMap.inverse().get(paint));
                if (otherPlayer != null) {
                    player.printError(TranslatableComponent.of("craftbook.paintingswitcher.in-use", TextComponent.of(otherPlayer.getName())));
                }
            } else {
                return;
            }

            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onHeldItemChange(PlayerItemHeldEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        // Questionable as to which would be slower, realistically it'll be the permission lookup.
        if (!paintingMap.containsKey(player.getUniqueId())) {
            return;
        }

        if (!player.hasPermission("craftbook.paintingswitcher.use")) {
            return;
        }

        boolean isForwards;
        if (event.getNewSlot() > event.getPreviousSlot()) {
            isForwards = true;
        } else if (event.getNewSlot() < event.getPreviousSlot()) {
            isForwards = false;
        } else {
            return;
        }

        // Handle the cases where the player went from the end to the start, etc.
        if (event.getPreviousSlot() == 0 && event.getNewSlot() == 8) {
            isForwards = false;
        } else if (event.getPreviousSlot() == 8 && event.getNewSlot() == 0) {
            isForwards = true;
        }

        Painting paint = paintingMap.get(player.getUniqueId());
        if (!paint.isValid()) {
            paintingMap.remove(player.getUniqueId());
            return;
        }

        if (!LocationUtil.isWithinSphericalRadius(paint.getLocation(), event.getPlayer().getLocation(), modifyRange)) {
            player.printError(TranslatableComponent.of("craftbook.paintingswitcher.too-far-away"));
            paintingMap.remove(event.getPlayer().getUniqueId());
            return;
        }

        int newID = artKeys.indexOf(paint.getArt().getKey()) + (isForwards ? 1 : -1);
        if (newID < 0) {
            newID = artKeys.size() - 1;
        } else if (newID > artKeys.size() - 1) {
            newID = 0;
        }

        while (!paint.setArt(Registry.ART.get(artKeys.get(newID)))) {
            if (newID > 0 && !isForwards) {
                newID--;
            } else if (newID < artKeys.size() - 1 && isForwards) {
                newID++;
            } else {
                break;
            }
        }

        paintingMap.put(player.getUniqueId(), paint);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        paintingMap.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onKicked(PlayerKickEvent event) {
        paintingMap.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onHangingEntityDestroy(HangingBreakByEntityEvent event) {
        if (event.getEntity() instanceof Painting painting) {
            UUID uuid = paintingMap.inverse().remove(painting);

            if (uuid != null) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    CraftBookPlugin.inst().wrapPlayer(player).printInfo(TranslatableComponent.of("craftbook.paintingswitcher.no-longer-editing"));
                }
            }
        }
    }
}
