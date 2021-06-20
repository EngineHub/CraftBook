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

package org.enginehub.craftbook.mechanics.minecart;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.HandSide;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.exception.MechanicInitializationException;
import org.enginehub.craftbook.util.CartUtil;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.RailUtil;

public class TemporaryCart extends AbstractCraftBookMechanic {
    private NamespacedKey temporaryCartKey;

    @Override
    public void enable() throws MechanicInitializationException {
        this.temporaryCartKey = new NamespacedKey("craftbook", "temporary_cart");
    }

    public NamespacedKey getTemporaryCartKey() {
        return this.temporaryCartKey;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        if (!EventUtil.passesFilter(event)) {
            return;
        }

        if (!RailUtil.isTrack(event.getClickedBlock().getType())) {
            return;
        }

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (player.isHoldingBlock() || player.isInsideVehicle() || player.isSneaking()) {
            return;
        }

        if (CartUtil.isMinecart(BukkitAdapter.adapt(player.getItemInHand(HandSide.MAIN_HAND).getType()))) {
            return;
        }

        if (!player.hasPermission("craftbook.temporarycart.use")) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                player.printError("mech.use-permission");
            }
            return;
        }

        RideableMinecart cart = event.getClickedBlock().getWorld().spawn(
            event.getClickedBlock().getLocation().toCenterLocation(),
            RideableMinecart.class
        );
        cart.getPersistentDataContainer().set(temporaryCartKey, PersistentDataType.BYTE, (byte) 1);
        cart.addPassenger(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDismount(final VehicleExitEvent event) {
        if (!(event.getVehicle() instanceof RideableMinecart)) {
            return;
        }

        if (!EventUtil.passesFilter(event)) {
            return;
        }

        Vehicle vehicle = event.getVehicle();

        if (vehicle.getPersistentDataContainer().has(temporaryCartKey, PersistentDataType.BYTE)) {
            Bukkit.getScheduler().runTaskLater(
                CraftBookPlugin.inst(),
                vehicle::remove,
                2L
            );
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleDestroy(final VehicleDestroyEvent event) {
        if (!(event.getVehicle() instanceof RideableMinecart)) {
            return;
        }

        if (!EventUtil.passesFilter(event)) {
            return;
        }

        Vehicle vehicle = event.getVehicle();

        if (vehicle.getPersistentDataContainer().has(temporaryCartKey, PersistentDataType.BYTE)) {
            event.setCancelled(true);
            Bukkit.getScheduler().runTaskLater(
                CraftBookPlugin.inst(),
                vehicle::remove,
                2L
            );
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        for (Entity entity : event.getChunk().getEntities()) {
            if (entity.getPersistentDataContainer().has(temporaryCartKey, PersistentDataType.BYTE)) {
                entity.remove();
            }
        }
    }
}
