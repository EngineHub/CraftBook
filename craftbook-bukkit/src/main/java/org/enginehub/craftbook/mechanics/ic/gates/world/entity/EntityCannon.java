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

package org.enginehub.craftbook.mechanics.ic.gates.world.entity;

import org.enginehub.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.worldedit.math.Vector3;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.RestrictedIC;
import org.enginehub.craftbook.util.EntityType;
import org.enginehub.craftbook.util.LocationUtil;
import org.enginehub.craftbook.util.RegexUtil;

public class EntityCannon extends AbstractSelfTriggeredIC {

    public EntityCannon(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Entity Cannon";
    }

    @Override
    public String getSignTitle() {

        return "ENTITY CANNON";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            chip.setOutput(0, shoot());
        }
    }

    @Override
    public void think(ChipState state) {

        state.setOutput(0, shoot());
    }

    double x,y,z;
    EntityType type;
    Location location;

    @Override
    public void load() {

        location = CraftBookBukkitUtil.toSign(getSign()).getLocation();

        if (!getSign().getLine(3).isEmpty())
            type = EntityType.fromString(getSign().getLine(3));

        if(type == null)
            type = EntityType.MOB_HOSTILE;

        try {
            String[] split = RegexUtil.COLON_PATTERN.split(getSign().getLine(2));
            x = Double.parseDouble(split[0]);
            y = Double.parseDouble(split[1]);
            z = Double.parseDouble(split[2]);
        }
        catch(Exception e) {
            x = 0;
            y = 1;
            z = 0;
        }
    }

    /**
     * This method launches near by entities
     *
     * @return true if a entity was thrown.
     */
    protected boolean shoot() {

        boolean resultBoolean = false;

        for (Entity e : LocationUtil.getNearbyEntities(location, Vector3.at(3,3,3))) {

            if (e.isDead() || !e.isValid())
                continue;

            if (!type.is(e))
                continue;

            e.setVelocity(new Vector(x, y, z).add(e.getVelocity()));

            resultBoolean = true;
        }

        return resultBoolean;
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new EntityCannon(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Shoots nearby entities of type at set velocity.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"velocity x:y:z", "mob type"};
        }
    }
}