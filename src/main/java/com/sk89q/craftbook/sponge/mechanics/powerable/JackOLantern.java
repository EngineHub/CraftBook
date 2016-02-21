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
        state = state.with(Keys.DIRECTION, direction).get();
        location.setBlock(state);
    }

    @Override
    public boolean isValid(Location location) {
        return location.getBlockType() == BlockTypes.PUMPKIN || location.getBlockType() == BlockTypes.LIT_PUMPKIN;
    }
}
