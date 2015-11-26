package com.sk89q.craftbook.sponge.mechanics;

import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.property.block.PoweredProperty;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.world.Location;

@Module(moduleName = "JackOLantern", onEnable="onInitialize", onDisable="onDisable")
public class JackOLantern extends SpongeBlockMechanic {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    @Override
    public void onInitialize() throws CraftBookException {
    }

    @Override
    public void onDisable() {
    }

    @Listener
    public void onBlockUpdate(NotifyNeighborBlockEvent event) {

        BlockSnapshot source;
        if(event.getCause().first(BlockSnapshot.class).isPresent())
            source = event.getCause().first(BlockSnapshot.class).get();
        else
            return;

        if(source.getState().getType() == BlockTypes.PUMPKIN || source.getState().getType() == BlockTypes.LIT_PUMPKIN) {
            PoweredProperty poweredProperty = source.getLocation().get().getProperty(PoweredProperty.class).orElse(null);
            updateState(source.getLocation().get(), poweredProperty == null ? false : poweredProperty.getValue());
        }
    }

    public void updateState(Location<?> location, boolean powered) {
        location.setBlock(powered ? BlockTypes.LIT_PUMPKIN.getDefaultState() : BlockTypes.PUMPKIN.getDefaultState());
    }

    @Override
    public boolean isValid(Location location) {
        return location.getBlockType() == BlockTypes.PUMPKIN || location.getBlockType() == BlockTypes.LIT_PUMPKIN;
    }
}
