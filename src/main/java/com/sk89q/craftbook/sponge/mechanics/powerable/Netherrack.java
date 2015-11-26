package com.sk89q.craftbook.sponge.mechanics.powerable;

import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.CraftBookException;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockTypes;
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
            location.setBlock(powered ? BlockTypes.FIRE.getDefaultState() : BlockTypes.AIR.getDefaultState());
    }

    @Override
    public boolean isValid(Location location) {
        return location.getBlockType() == BlockTypes.NETHERRACK;
    }
}
