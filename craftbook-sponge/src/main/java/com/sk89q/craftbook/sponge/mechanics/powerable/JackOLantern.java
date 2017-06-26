/*
 * CraftBook Copyright (C) 2010-2017 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2017 me4502 <http://www.me4502.com>
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
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

@Module(id = "jackolantern", name = "JackOLantern", onEnable="onInitialize", onDisable="onDisable")
public class JackOLantern extends SimplePowerable implements DocumentationProvider {

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
        location.setBlock(state, Cause.of(NamedCause.source(CraftBookPlugin.spongeInst().getContainer())));
    }

    @Override
    public boolean getState(Location<?> location) {
        return location.getBlock().getType() == BlockTypes.LIT_PUMPKIN;
    }

    @Override
    public boolean isValid(Location<World> location) {
        return location.getBlockType() == BlockTypes.PUMPKIN || location.getBlockType() == BlockTypes.LIT_PUMPKIN;
    }

    @Override
    public String getPath() {
        return "mechanics/jack_o_lantern";
    }
}
