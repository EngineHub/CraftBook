package com.sk89q.craftbook.sponge.mechanics.powerable;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.PermissionNode;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.world.Location;

@Module(moduleName = "GlowStone", onEnable="onInitialize", onDisable="onDisable")
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
    public void onDisable() {
        offBlock.save(config);
    }

    @Override
    public void updateState(Location<?> location, boolean powered) {
        location.setBlock(powered ? BlockTypes.GLOWSTONE.getDefaultState() : offBlock.getValue());
    }

    @Override
    public boolean isValid(Location location) {
        return location.getBlockType() == BlockTypes.GLOWSTONE;
    }

    @Override
    public String getPath() {
        return "mechanics/glowstone";
    }

    @Override
    public String[] getMainDocumentation() {
        return  new String[]{
                "=========",
                "Glowstone",
                "=========",
                "The *Glowstone* mechanic allows switching a configurable block (defaulted to glass) to glowstone using redstone. Put a tonne together and you can control your home's lighting with a regular redstone switch.",
                ""
        };
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue<?>[] {
                offBlock
        };
    }

    @Override
    public PermissionNode[] getPermissionNodes() {
        return new PermissionNode[0];
    }
}
