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
package com.sk89q.craftbook.sponge.mechanics;

import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import com.sk89q.craftbook.sponge.util.type.VariableTypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.SendCommandEvent;

import java.util.HashMap;
import java.util.Map;

@Module(moduleName = "Variables", onEnable="onInitialize", onDisable="onDisable")
public class Variables extends SpongeMechanic {

    Map<String, Map<String, String>> variableStore;

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        if(config.getValue() != null) {
            try {
                variableStore = config.getValue(new VariableTypeToken());
            } catch (ObjectMappingException e) {
                e.printStackTrace();
                System.out.println("Failed to read variables! Resetting..");
                variableStore = new HashMap<>();
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        config.setValue(variableStore);
    }

    @Listener
    public void onCommandSend(SendCommandEvent event) {

    }

}
