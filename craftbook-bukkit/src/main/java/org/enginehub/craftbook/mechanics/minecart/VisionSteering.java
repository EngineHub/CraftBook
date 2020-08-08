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

import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.RailUtil;
import com.sk89q.util.yaml.YAMLProcessor;

public class VisionSteering extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(!event.getPlayer().isInsideVehicle())
            return;

        if(!(event.getPlayer().getVehicle() instanceof Minecart))
            return;

        if(Math.abs((double)event.getFrom().getYaw() - (double)event.getTo().getYaw()) < minimumSensitivity)
            return;

        if(RailUtil.isTrack(event.getPlayer().getVehicle().getLocation().getBlock().getType()))
            return;

        Vector direction = event.getPlayer().getLocation().getDirection();
        direction = direction.normalize();
        direction.setY(0);
        direction = direction.multiply(event.getPlayer().getVehicle().getVelocity().length());
        direction.setY(event.getPlayer().getVehicle().getVelocity().getY());
        event.getPlayer().getVehicle().setVelocity(direction);
    }

    private int minimumSensitivity;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

        config.setComment("minimum-sensitivity", "Sets the sensitivity of Vision Steering.");
        minimumSensitivity = config.getInt("minimum-sensitivity", 3);
    }
}