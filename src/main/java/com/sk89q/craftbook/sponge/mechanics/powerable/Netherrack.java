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
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;

@Module(moduleName = "Netherrack", onEnable="onInitialize", onDisable="onDisable")
public class Netherrack extends SimplePowerable {

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
        location = location.getRelative(Direction.UP);
        if(location.getBlockType() == BlockTypes.FIRE || location.getBlockType() == BlockTypes.AIR)
            location.setBlock(powered ? BlockTypes.FIRE.getDefaultState() : BlockTypes.AIR.getDefaultState(), Cause.of(NamedCause.source(CraftBookPlugin.<CraftBookPlugin>inst().getContainer())));
    }

    @Override
    public boolean isValid(Location location) {
        return location.getBlockType() == BlockTypes.NETHERRACK;
    }
}
