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

package com.sk89q.craftbook.mechanics.minecart;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.CartUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.RailUtil;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.HandSide;
import org.bukkit.Bukkit;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashSet;
import java.util.Set;

public class TemporaryCart extends AbstractCraftBookMechanic {

    private Set<RideableMinecart> minecarts = new HashSet<>();

    public Set<RideableMinecart> getMinecarts() {

        return minecarts;
    }

    @Override
    public void disable() {

        for(RideableMinecart minecart : minecarts) {
            minecart.remove();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND) return;

        if(!RailUtil.isTrack(event.getClickedBlock().getType()))
            return;

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if(player.isHoldingBlock() || player.isInsideVehicle() || player.isSneaking()) return;
        if(CartUtil.isMinecart(BukkitAdapter.adapt(player.getItemInHand(HandSide.MAIN_HAND).getType()))) {
            return;
        }

        if(!EventUtil.passesFilter(event))
            return;

        if(!player.hasPermission("craftbook.vehicles.temporary-cart.use")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.create-permission");
            return;
        }

        RideableMinecart cart = event.getClickedBlock().getWorld().spawn(BlockUtil.getBlockCentre(event.getClickedBlock()), RideableMinecart.class);
        minecarts.add(cart);
        cart.addPassenger(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDismount(final VehicleExitEvent event) {

        if(!(event.getVehicle() instanceof RideableMinecart)) return;

        if(!EventUtil.passesFilter(event))
            return;

        if(!minecarts.contains(event.getVehicle())) return;

        Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), event.getVehicle()::remove, 2L);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleDestroy(final VehicleDestroyEvent event) {
        if(!(event.getVehicle() instanceof RideableMinecart)) return;

        if(!EventUtil.passesFilter(event))
            return;

        if (minecarts.contains(event.getVehicle())) {
            event.setCancelled(true);
            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), event.getVehicle()::remove, 2L);
        }
    }

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

    }
}