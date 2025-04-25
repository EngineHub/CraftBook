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
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;

public class BetterSponge extends AbstractCraftBookMechanic {

    public BetterSponge(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    protected boolean isValidSponge(BlockType type) {
        return type == BlockTypes.SPONGE || includeWet && type == BlockTypes.WET_SPONGE;
    }

    protected boolean isRemovableWater(BlockType material) {
        return material == BlockTypes.WATER || destructive && (material == BlockTypes.SEAGRASS
            || material == BlockTypes.TALL_SEAGRASS || material == BlockTypes.KELP_PLANT
            || material == BlockTypes.KELP);
    }

    protected int radius;
    protected boolean sphereRange;
    protected boolean redstone;
    protected boolean includeWet;
    protected boolean destructive;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("radius", "The maximum radius of the sponge.");
        radius = config.getInt("radius", 5);

        config.setComment("sphere-range", "Whether the active range should be spherical or cuboid.");
        sphereRange = config.getBoolean("sphere-range", true);

        config.setComment("include-wet-sponges", "Whether wet sponges also activate the mechanic.");
        includeWet = config.getBoolean("include-wet-sponges", false);

        config.setComment("require-redstone", "Whether to require redstone to suck up water or not.");
        redstone = config.getBoolean("require-redstone", false);

        config.setComment("destructive", "Whether to remove blocks that spread water such as kelp. These will not be returned when the sponge is de-activated.");
        destructive = config.getBoolean("destructive", true);
    }
}
