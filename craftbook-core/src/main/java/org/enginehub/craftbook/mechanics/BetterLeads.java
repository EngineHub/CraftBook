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

package org.enginehub.craftbook.mechanics;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.world.entity.EntityTypes;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;

import java.util.List;
import java.util.Set;

public abstract class BetterLeads extends AbstractCraftBookMechanic {

    protected static final int MAX_LEASH_DISTANCE = 10;

    public BetterLeads(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    protected boolean stopTargetting;
    protected boolean ownerBreakOnly;
    protected boolean persistentHitch;
    protected boolean mobRepellant;
    protected Set<String> allowedMobs;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("stop-mob-target", "Stop hostile mobs targeting you if you are holding them on a leash.");
        stopTargetting = config.getBoolean("stop-mob-target", false);

        config.setComment("owner-unleash-only", "Only allow the owner of tameable entities to unleash them from a leash hitch.");
        ownerBreakOnly = config.getBoolean("owner-unleash-only", false);

        config.setComment("hitch-persists", "Stop leash hitches breaking when no entities are attached. This allows for a public horse hitch or similar.");
        persistentHitch = config.getBoolean("hitch-persists", false);

        config.setComment("mob-repel", "If you have a mob tethered to you, mobs of that type will not target you.");
        mobRepellant = config.getBoolean("mob-repel", false);

        config.setComment("allowed-mobs", "The list of mobs that can be tethered with a lead.");
        allowedMobs = Set.copyOf(config.getStringList("allowed-mobs", List.of(EntityTypes.ZOMBIE.id(), EntityTypes.SPIDER.id())));
    }
}
