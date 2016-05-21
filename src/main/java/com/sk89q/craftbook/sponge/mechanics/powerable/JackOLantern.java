/*
 * CraftBook Copyright (C) 2010-2016 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2016 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
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
package com.sk89q.craftbook.sponge.mechanics.powerable;

import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.CraftBookException;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;

@Module(moduleName = "JackOLantern", onEnable="onInitialize", onDisable="onDisable")
public class JackOLantern extends SimplePowerable {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    @Override
    public void onInitialize() throws CraftBookException {
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void updateState(Location<?> location, boolean powered) {
        Direction direction = location.get(Keys.DIRECTION).orElse(Direction.NORTH);
        BlockState state = BlockState.builder().blockType(powered ? BlockTypes.LIT_PUMPKIN : BlockTypes.PUMPKIN).build();
        state = state.with(Keys.DIRECTION, direction).orElse(state);
        location.setBlock(state);
    }

    @Override
    public boolean isValid(Location location) {
        return location.getBlockType() == BlockTypes.PUMPKIN || location.getBlockType() == BlockTypes.LIT_PUMPKIN;
    }
}
