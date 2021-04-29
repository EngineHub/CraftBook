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

package org.enginehub.craftbook.mechanics.ic.gates.world.entity;

import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.bukkit.util.CraftBookBukkitUtil;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.RestrictedIC;
import org.enginehub.craftbook.util.EntityType;
import org.enginehub.craftbook.util.EntityUtil;
import org.enginehub.craftbook.util.RegexUtil;
import org.enginehub.craftbook.util.SearchArea;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Me4502
 */
public class EntityTrap extends AbstractSelfTriggeredIC {

    public EntityTrap(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Entity Trap";
    }

    @Override
    public String getSignTitle() {

        return "ENTITY TRAP";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            chip.setOutput(0, hurt());
        }
    }

    @Override
    public void think(ChipState chip) {

        chip.setOutput(0, hurt());
    }

    SearchArea area;

    int damage;
    List<EntityType> types;
    boolean erase;

    @Override
    public void load() {

        area = SearchArea.createArea(CraftBookBukkitUtil.toSign(getSign()).getBlock(), getLine(2));

        try {
            damage = Integer.parseInt(RegexUtil.EQUALS_PATTERN.split(getSign().getLine(2))[2]);
        } catch (Exception ignored) {
            damage = 2;
        }

        if (!getLine(3).isEmpty()) {
            types = new ArrayList<>(EntityType.getDetected(getLine(3)));
        } else
            types = Collections.singletonList(EntityType.MOB_HOSTILE);

        if (types.isEmpty())
            types.add(EntityType.ANY);
        erase = getLine(3).startsWith("!");
    }

    /**
     * Returns true if the entity was damaged.
     *
     * @return
     */
    protected boolean hurt() {

        boolean hasHurt = false;

        for (Entity e : area.getEntitiesInArea(types)) {

            if (erase)
                e.remove();
            else
                EntityUtil.damageEntity(e, damage);
            hasHurt = true;
        }

        return hasHurt;
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new EntityTrap(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Damage nearby entities of type.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "radius=x:y:z=damage", "mob type" };
        }
    }
}