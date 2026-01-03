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

package org.enginehub.craftbook.mechanics.area.clipboard;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.Location;
import net.kyori.adventure.text.TextReplacementConfig;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;

import java.util.regex.Pattern;

public abstract class ToggleArea extends AbstractCraftBookMechanic {

    protected static final TextReplacementConfig DASH_REMOVER = TextReplacementConfig.builder().matchLiteral("-").replacement("").build();

    // pattern to check where the markers for on and off state are
    protected static final Pattern TOGGLED_ON_PATTERN = Pattern.compile("^-[A-Za-z0-9_]*?-$");

    public ToggleArea(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    public abstract boolean toggleCold(Actor actor, Location block);

    public boolean allowRedstone;
    public boolean removeEntitiesOnToggle;
    public int maxAreaSize;
    public int maxAreasPerUser;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("allow-redstone", "Allow ToggleAreas to be toggled via redstone.");
        allowRedstone = config.getBoolean("allow-redstone", true);

        config.setComment("remove-entities-on-toggle", "Whether the area toggling will remove entities within it.");
        removeEntitiesOnToggle = config.getBoolean("remove-entities-on-toggle", false);

        config.setComment("max-size", "Sets the max amount of blocks that a ToggleArea can hold.");
        maxAreaSize = config.getInt("max-size", 5000);

        config.setComment("max-per-user", "Sets the max amount of ToggleAreas that can be within one personal namespace.");
        maxAreasPerUser = config.getInt("max-per-user", 30);
    }

    public record ToggleAreaData(String namespace, String areaOn, String areaOff) {
    }
}
