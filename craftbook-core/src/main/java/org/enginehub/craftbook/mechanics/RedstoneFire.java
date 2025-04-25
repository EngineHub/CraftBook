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

public abstract class RedstoneFire extends AbstractCraftBookMechanic {

    public RedstoneFire(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    protected boolean doesAffectBlock(BlockType type) {
        return (enableNetherrack && type == BlockTypes.NETHERRACK) || (enableSoulSoil && type == BlockTypes.SOUL_SOIL);
    }

    protected BlockType getFireForBlock(BlockType type) {
        if (type == BlockTypes.NETHERRACK) {
            return BlockTypes.FIRE;
        } else if (type == BlockTypes.SOUL_SOIL) {
            return BlockTypes.SOUL_FIRE;
        } else {
            throw new RuntimeException("Tried to place fire on an unsupported block. Please report this error to CraftBook");
        }
    }

    private boolean enableNetherrack;
    private boolean enableSoulSoil;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("enable-netherrack", "Whether the mechanic should affect Netherrack.");
        enableNetherrack = config.getBoolean("enable-netherrack", true);

        config.setComment("enable-soul-soil", "Whether the mechanic should affect Soul Soil.");
        enableSoulSoil = config.getBoolean("enable-soul-soil", true);
    }
}
