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

package org.enginehub.craftbook.mechanics.ic.gates.world.sensors;

import com.sk89q.worldedit.math.Vector3;
import net.kyori.adventure.text.Component;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.enginehub.craftbook.bukkit.BukkitChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.util.EntityType;
import org.enginehub.craftbook.util.ICUtil;
import org.enginehub.craftbook.util.LocationUtil;
import org.enginehub.craftbook.util.RegexUtil;

import java.util.Locale;
import java.util.Set;

/**
 * Movement Sensor. This IC is incomplete due to the bukkit API not providing ample movement
 * velocity support.
 *
 * @author Me4502
 */
public class MovementSensor extends AbstractSelfTriggeredIC {

    public MovementSensor(Server server, BukkitChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    private Set<EntityType> types;

    private Block center;
    private Vector3 radius;

    @Override
    public void load() {

        // lets get the types to detect first
        types = EntityType.getDetected(getLine(3).trim());

        // Add all if no params are specified
        if (types.isEmpty()) {
            types.add(EntityType.ANY);
        }

        getSign().setLine(3, Component.text(getLine(3).toUpperCase(Locale.ENGLISH)));

        // if the line contains a = the offset is given
        // the given string should look something like that:
        // radius=x:y:z or radius, e.g. 1=-2:5:11
        radius = ICUtil.parseRadius(getSign());
        String radiusString = radius.x() + "," + radius.y() + "," + radius.z();
        if (radius.x() == radius.y() && radius.y() == radius.z())
            radiusString = String.valueOf(radius.x());
        if (getLine(2).contains("=")) {
            getSign().setLine(2, Component.text(radiusString + "=" + RegexUtil.EQUALS_PATTERN.split(getLine(2))[1]));
            center = ICUtil.parseBlockLocation(getSign());
        } else {
            getSign().setLine(2, Component.text(radiusString));
            center = getBackBlock();
        }
        getSign().update(false);
    }

    @Override
    public String getTitle() {

        return "Movement Sensor";
    }

    @Override
    public String getSignTitle() {

        return "MOVING SENSOR";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) chip.setOutput(0, check());
    }

    @Override
    public void think(ChipState chip) {

        check();
    }

    public boolean check() {

        for (Entity entity : center.getLocation().getNearbyEntities(radius.x(), radius.y(), radius.z())) {
            if (entity.isValid()) {
                for (EntityType type : types) { // Check Type
                    if (type.is(entity)) { // Check Radius
                        if (LocationUtil.isWithinRadius(center.getLocation(), entity.getLocation(), radius)) {
                            if (entity.getVelocity().lengthSquared() >= 0.01) return true;
                        }
                        break;
                    }
                }
            }
        }
        return false;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(BukkitChangedSign sign) {

            return new MovementSensor(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Outputs high if a nearby entity is moving.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "radius=x:y:z offset", "entity type" };
        }
    }
}