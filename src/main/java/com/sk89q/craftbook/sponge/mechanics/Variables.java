package com.sk89q.craftbook.sponge.mechanics;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
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
                variableStore = config.getValue(new TypeToken<Map<String, Map<String, String>>>() {
                });
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
