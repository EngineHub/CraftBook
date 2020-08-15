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
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.util.EventUtil;


public class MoreRails extends AbstractCraftBookMechanic {

    public static MoreRails instance;

    @Override
    public void enable() {

        instance = this;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleMove(VehicleMoveEvent event) {

        if (!EventUtil.passesFilter(event)) return;

        if (!(event.getVehicle() instanceof Minecart)) return;

        if (pressurePlate)
            if (event.getTo().getBlock().getType() == Material.STONE_PRESSURE_PLATE
                || Tag.WOODEN_PRESSURE_PLATES.isTagged(event.getTo().getBlock().getType())
                || event.getTo().getBlock().getType() == Material.HEAVY_WEIGHTED_PRESSURE_PLATE
                || event.getTo().getBlock().getType() == Material.LIGHT_WEIGHTED_PRESSURE_PLATE)
                event.getVehicle().setVelocity(event.getVehicle().getVelocity().normalize().multiply(4));

        if (ladder) {
            double maxSpeed = ((Minecart) event.getVehicle()).getMaxSpeed();
            if (event.getTo().getBlock().getType() == Material.LADDER) {
                BlockFace face = ((Directional) event.getTo().getBlock().getBlockData()).getFacing().getOppositeFace();
                Vector velocity = new Vector(0, ladderVerticalVelocity, face.getModZ());
                if (velocity.length() > maxSpeed) {
                    double length = velocity.length() / maxSpeed;
                    velocity.setX(velocity.getX() / length);
                    velocity.setY(velocity.getY() / length);
                    velocity.setZ(velocity.getZ() / length);
                }
                velocity.add(new Vector(face.getModX(), 0, face.getModZ()));
                event.getVehicle().setVelocity(event.getVehicle().getVelocity().add(velocity));
            } else if (event.getTo().getBlock().getType() == Material.VINE) {
                BlockFace movementFace = BlockFace.SELF;
                MultipleFacing vine = (MultipleFacing) event.getTo().getBlock().getBlockData();
                for (BlockFace test : vine.getAllowedFaces())
                    if (vine.hasFace(test)) {
                        movementFace = test;
                        break;
                    }
                if (movementFace == BlockFace.SELF)
                    return;
                Vector velocity = new Vector(0, ladderVerticalVelocity, 0);
                if (velocity.length() > maxSpeed) {
                    double length = velocity.length() / maxSpeed;
                    velocity.setX(velocity.getX() / length);
                    velocity.setY(velocity.getY() / length);
                    velocity.setZ(velocity.getZ() / length);
                }
                velocity.add(new Vector(movementFace.getModX(), 0, movementFace.getModZ()));
                event.getVehicle().setVelocity(event.getVehicle().getVelocity().add(velocity));
            }
        }
    }

    public boolean ladder;
    private double ladderVerticalVelocity;
    public boolean pressurePlate;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

        config.setComment("pressure-plate-intersection", "Enables the pressure plate as an intersection.");
        pressurePlate = config.getBoolean("pressure-plate-intersection", false);

        config.setComment("ladder-vertical-rail", "Enables the ladder as a vertical rail.");
        ladder = config.getBoolean("ladder-vertical-rail", false);

        config.setComment("ladder-vertical-rail-velocity", "Sets the velocity applied to the minecart on vertical rails.");
        ladderVerticalVelocity = config.getDouble("ladder-vertical-rail-velocity", 0.5D);
    }
}