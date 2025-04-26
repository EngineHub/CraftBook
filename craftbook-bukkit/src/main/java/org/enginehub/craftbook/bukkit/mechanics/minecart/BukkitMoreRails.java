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

package org.enginehub.craftbook.bukkit.mechanics.minecart;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.mechanics.minecart.MoreRails;
import org.enginehub.craftbook.util.EventUtil;


public class BukkitMoreRails extends MoreRails implements Listener {

    public BukkitMoreRails(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleMove(VehicleMoveEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        if (!(event.getVehicle() instanceof Minecart minecart)) {
            return;
        }

        Block toBlock = event.getTo().getBlock();
        Material toType = toBlock.getType();

        if (pressurePlate && Tag.PRESSURE_PLATES.isTagged(toType)) {
            minecart.setVelocity(minecart.getVelocity().normalize());
        } else if (ladder) {
            double maxSpeed = minecart.getMaxSpeed();

            Vector velocity = new Vector(0, ladderVerticalVelocity, 0);

            if (velocity.lengthSquared() > maxSpeed * maxSpeed) {
                // Normalize to maxSpeed
                double length = velocity.length() / maxSpeed;
                velocity.setX(velocity.getX() / length);
                velocity.setY(velocity.getY() / length);
                velocity.setZ(velocity.getZ() / length);
            }

            BlockFace face = null;

            if (toType == Material.LADDER) {
                face = ((Directional) toBlock.getBlockData()).getFacing().getOppositeFace();
            } else if (toType == Material.VINE) {
                MultipleFacing vine = (MultipleFacing) toBlock.getBlockData();
                for (BlockFace test : vine.getAllowedFaces()) {
                    if (vine.hasFace(test)) {
                        face = test;
                        break;
                    }
                }
            }

            if (face != null) {
                velocity.add(new Vector(face.getModX(), 0, face.getModZ()));
                minecart.setVelocity(minecart.getVelocity().add(velocity));
            }
        }
    }
}
