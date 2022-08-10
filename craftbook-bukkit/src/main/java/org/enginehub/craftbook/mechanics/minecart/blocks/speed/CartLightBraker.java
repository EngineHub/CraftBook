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

package org.enginehub.craftbook.mechanics.minecart.blocks.speed;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.entity.Minecart;
import org.bukkit.util.Vector;
import org.enginehub.craftbook.util.BlockParser;

import javax.annotation.Nullable;

public class CartLightBraker extends AbstractCartBooster {
    @Nullable
    @Override
    protected Vector getNewVelocity(Minecart minecart) {
        return minecart.getVelocity().clone().multiply(0.8);
    }

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("block", "The block the Minecart light braker uses.");
        setBlock(BlockParser.getBlock(config.getString("block", BlockTypes.GRAVEL.getId()), true));
    }
}
