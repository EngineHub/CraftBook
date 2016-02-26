package com.sk89q.craftbook.sponge.mechanics.minecart;

import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.PermissionNode;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.manipulator.mutable.entity.PassengerData;
import org.spongepowered.api.entity.vehicle.minecart.Minecart;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DismountEntityEvent;

@Module(moduleName = "MinecartEmptyDecay", onEnable="onInitialize", onDisable="onDisable")
public class EmptyDecay extends SpongeMechanic implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private ConfigValue<Long> emptyTicks = new ConfigValue<>("empty-ticks", "The amount of time that the cart must be empty before it decays, in ticks.", 40L);

    @Override
    public void onInitialize() throws CraftBookException {
        emptyTicks.load(config);
    }

    @Listener
    public void onVehicleExit(DismountEntityEvent event) {

        if (event.getTargetEntity() instanceof Minecart) {
            Sponge.getGame().getScheduler().createTaskBuilder().delayTicks(emptyTicks.getValue()).execute(new MinecartDecay((Minecart) event.getTargetEntity())).submit(CraftBookPlugin.inst());
        }
    }

    private static class MinecartDecay implements Runnable {

        Minecart cart;

        MinecartDecay(Minecart cart) {

            this.cart = cart;
        }

        @Override
        public void run() {
            if (!cart.get(PassengerData.class).isPresent()) {
                cart.remove();
            }
        }
    }

    @Override
    public String getName() {
        return "Minecart" + super.getName();
    }

    @Override
    public String getPath() {
        return "mechanics/minecart/emptydecay";
    }

    @Override
    public String[] getMainDocumentation() {
        return new String[0];
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue<?>[] {
                emptyTicks
        };
    }

    @Override
    public PermissionNode[] getPermissionNodes() {
        return new PermissionNode[0];
    }
}
