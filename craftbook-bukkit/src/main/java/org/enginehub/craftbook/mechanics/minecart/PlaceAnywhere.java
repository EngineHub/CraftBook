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

package org.enginehub.craftbook.mechanics.minecart;

import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.RailUtil;

public class PlaceAnywhere extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerClick(PlayerInteractEvent event) {

        if (!EventUtil.passesFilter(event)) return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND)
            return;
        if (event.getPlayer().getItemInHand() == null || event.getPlayer().getItemInHand().getType() != Material.MINECART)
            return;
        if (RailUtil.isTrack(event.getClickedBlock().getType())) return;

        Location loc = event.getClickedBlock().getRelative(0, 2, 0).getLocation();
        event.getClickedBlock().getWorld().spawn(loc, Minecart.class);
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            if (event.getPlayer().getItemInHand().getAmount() <= 1)
                event.getPlayer().setItemInHand(null);
            else {
                ItemStack heldItem = event.getPlayer().getItemInHand();
                heldItem.setAmount(heldItem.getAmount() - 1);
                event.getPlayer().setItemInHand(heldItem);
            }
            event.setCancelled(true);
        }
    }

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

    }
}