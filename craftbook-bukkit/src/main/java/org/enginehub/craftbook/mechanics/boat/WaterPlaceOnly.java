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

package com.sk89q.craftbook.mechanics.boat;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class WaterPlaceOnly extends AbstractCraftBookMechanic {

    @EventHandler
    public void playerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if(event.getItem() != null
                    && Tag.ITEMS_BOATS.isTagged(event.getItem().getType())) {
                Block above = event.getClickedBlock().getRelative(0,1,0);
                if ((!isWater(above) || event.getClickedBlock().getY() == event.getClickedBlock().getWorld().getMaxHeight() - 1) && !isWater(event.getClickedBlock())) {
                    event.setCancelled(true);
                    event.setUseItemInHand(Result.DENY);
                    event.getPlayer().sendMessage(ChatColor.RED + "You can't place that boat on land!");
                }
            }
        }
    }

    private static boolean isWater(Block b) {
        return b.getType() == Material.WATER;
    }

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

    }
}