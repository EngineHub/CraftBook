package com.sk89q.craftbook.sponge.mechanics;

import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.world.Location;

@Module(moduleName = "GlowStone", onEnable="onInitialize", onDisable="onDisable")
public class GlowStone extends SpongeBlockMechanic {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    ConfigValue<BlockState> offBlock = new ConfigValue<>("off-block", "Sets the block that the glowstone turns into when turned off.", BlockTypes.SOUL_SAND.getDefaultState());

    @Override
    public void onInitialize() throws CraftBookException {
        offBlock.load(config);
    }

    @Override
    public void onDisable() {
        offBlock.save(config);
    }

    @Override
    public boolean isValid(Location location) {
        return location.getBlockType() == BlockTypes.GLOWSTONE;
    }
}
