package com.sk89q.craftbook.sponge.mechanics.boat;

import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.entity.vehicle.Boat;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.SpawnEntityEvent;

@Module(moduleId = "landboats", moduleName = "LandBoats", onEnable="onInitialize", onDisable="onDisable")
public class LandBoats extends SpongeMechanic implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    @Listener
    public void onEntityConstruct(SpawnEntityEvent event) {
        event.getEntities().stream().filter(entity -> entity instanceof Boat).forEach(boat -> ((Boat) boat).setMoveOnLand(true));
    }

    @Override
    public String getPath() {
        return "mechanics/boat/landboats";
    }
}
