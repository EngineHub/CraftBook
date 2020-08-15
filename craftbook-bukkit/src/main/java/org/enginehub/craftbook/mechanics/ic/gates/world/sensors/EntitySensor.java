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

package org.enginehub.craftbook.mechanics.ic.gates.world.sensors;

import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.bukkit.util.CraftBookBukkitUtil;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.ICVerificationException;
import org.enginehub.craftbook.util.EntityType;
import org.enginehub.craftbook.util.SearchArea;

import java.util.Set;

/**
 * @author Silthus
 */
public class EntitySensor extends AbstractSelfTriggeredIC {

    private Set<EntityType> types;

    private SearchArea area;

    private short minimum;

    private short minMode;

    public EntitySensor(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public void load() {

        // lets get the types to detect first
        types = EntityType.getDetected(getLine(3).split("<")[0].trim().split("<=")[0].trim().split(">=")[0].trim().split("==")[0].trim().split(">")[0].trim());

        if (getLine(3).contains(">="))
            minMode = 0;
        else if (getLine(3).contains("=="))
            minMode = 1;
        else if (getLine(3).contains(">"))
            minMode = 2;
        else if (getLine(3).contains("<="))
            minMode = 3;
        else if (getLine(3).contains("<"))
            minMode = 4;
        else
            minMode = 0;

        try {
            if (minMode == 0)
                minimum = Short.parseShort(getLine(3).split(">=")[1].trim());
            else if (minMode == 1)
                minimum = Short.parseShort(getLine(3).split("==")[1].trim());
            else if (minMode == 2)
                minimum = Short.parseShort(getLine(3).split(">")[1].trim());
            else if (minMode == 3)
                minimum = Short.parseShort(getLine(3).split("<=")[1].trim());
            else
                minimum = Short.parseShort(getLine(3).split("<")[1].trim());
        } catch (Exception e) {
            minimum = 1;
        }

        area = SearchArea.createArea(CraftBookBukkitUtil.toSign(getSign()).getBlock(), getLine(2));
    }

    @Override
    public String getTitle() {

        return "Entity Sensor";
    }

    @Override
    public String getSignTitle() {

        return "ENTITY SENSOR";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            chip.setOutput(0, isDetected());
        }
    }

    @Override
    public void think(ChipState state) {

        state.setOutput(0, isDetected());
    }

    protected boolean isDetected() {

        short cur = 0;

        for (Entity entity : area.getEntitiesInArea(types))
            if (entity.isValid())
                for (EntityType type : types) // Check Type
                    if (type.is(entity)) // Check Radius
                        cur++;

        if (minMode == 0 && cur >= minimum)
            return true;
        else if (minMode == 1 && cur == minimum)
            return true;
        else if (minMode == 2 && cur > minimum)
            return true;
        else if (minMode == 3 && cur <= minimum)
            return true;
        else if (minMode == 4 && cur < minimum)
            return true;

        return false;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new EntitySensor(getServer(), sign, this);
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            if (!SearchArea.isValidArea(CraftBookBukkitUtil.toSign(sign).getBlock(), sign.getLine(2)))
                throw new ICVerificationException("Invalid SearchArea on 3rd line!");
        }

        @Override
        public String getShortDescription() {

            return "Detects specific entity types in a given radius.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "SearchArea", "Entity Types{(>=|==|>)minimum}" };
        }
    }
}