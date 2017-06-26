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

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

@Module(id = "glowstone", name = "GlowStone", onEnable="onInitialize", onDisable="onDisable")
public class GlowStone extends SimplePowerable implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private ConfigValue<BlockState> offBlock = new ConfigValue<>("off-block", "Sets the block that the glowstone turns into when turned off.", BlockTypes.SOUL_SAND.getDefaultState(), TypeToken.of(BlockState.class));

    @Override
    public void onInitialize() throws CraftBookException {
        offBlock.load(config);
    }

    @Override
    public void updateState(Location<?> location, boolean powered) {
        location.setBlock(powered ? BlockTypes.GLOWSTONE.getDefaultState() : offBlock.getValue(), Cause.of(NamedCause.source(CraftBookPlugin.spongeInst().getContainer())));
    }

    @Override
    public boolean getState(Location<?> location) {
        return location.getBlock().getType() == BlockTypes.GLOWSTONE;
    }

    @Override
    public boolean isValid(Location<World> location) {
        return location.getBlockType() == BlockTypes.GLOWSTONE || location.getBlock().equals(offBlock.getValue());
    }

    @Override
    public String getPath() {
        return "mechanics/glowstone";
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue<?>[] {
                offBlock
        };
    }
}
