package com.sk89q.craftbook.sponge.mechanics.boat;

import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.vehicle.Boat;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

@Module(moduleId = "boatwaterplaceonly", moduleName = "WaterPlaceOnly", onEnable="onInitialize", onDisable="onDisable")
public class WaterPlaceOnly extends SpongeMechanic implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    @Listener
    public void onEntityConstruct(SpawnEntityEvent event) {
        event.getEntities().stream().filter(entity -> entity instanceof Boat).forEach(boat -> {
            if (!isWater(boat.getLocation())) {
                event.setCancelled(true);
            }
        });
    }

    private static boolean isWater(Location<World> location) {
        return location.getBlockType() == BlockTypes.WATER || location.getBlockType() == BlockTypes.FLOWING_WATER;
    }

    @Override
    public String getName() {
        return "Boat" + super.getName();
    }

    @Override
    public String getPath() {
        return "mechanics/boat/waterplaceonly";
    }
}
