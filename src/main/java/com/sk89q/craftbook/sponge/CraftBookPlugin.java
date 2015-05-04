package com.sk89q.craftbook.sponge;

import java.io.File;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.state.ServerStartedEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.config.DefaultConfig;

import com.google.inject.Inject;
import com.sk89q.craftbook.core.CraftBookAPI;
import com.sk89q.craftbook.core.Mechanic;
import com.sk89q.craftbook.core.util.MechanicDataCache;
import com.sk89q.craftbook.sponge.blockbags.BlockBagManager;
import com.sk89q.craftbook.sponge.mechanics.Elevator;
import com.sk89q.craftbook.sponge.mechanics.Footprints;
import com.sk89q.craftbook.sponge.mechanics.HeadDrops;
import com.sk89q.craftbook.sponge.mechanics.Snow;
import com.sk89q.craftbook.sponge.mechanics.TreeLopper;
import com.sk89q.craftbook.sponge.mechanics.area.Bridge;
import com.sk89q.craftbook.sponge.mechanics.area.Door;
import com.sk89q.craftbook.sponge.mechanics.area.Gate;
import com.sk89q.craftbook.sponge.mechanics.ics.ICSocket;
import com.sk89q.craftbook.sponge.mechanics.minecart.EmptyDecay;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import com.sk89q.craftbook.sponge.st.SelfTriggerManager;
import com.sk89q.craftbook.sponge.util.SpongeDataCache;

@Plugin(id = "CraftBook", name = "CraftBook", version = "4.0"/* , dependencies = "required-after:WorldEdit@[6.0,)" */)
public class CraftBookPlugin extends CraftBookAPI {

    public static Game game;

    public Set<Mechanic> enabledMechanics = new HashSet<Mechanic>();

    MechanicDataCache cache;

    public BlockBagManager blockBagManager;

    /* Configuration Data */

    @Inject
    @DefaultConfig(sharedRoot = false)
    private File mainConfig;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    @Inject
    public PluginContainer container;

    protected SpongeConfiguration config;

    /* Logging */

    @Inject
    protected Logger logger;

    public Logger getLogger() {
        return logger;
    }

    @Subscribe
    public void onInitialization(ServerStartedEvent event) {

        game = event.getGame();
        instance = this;

        logger.info("Starting CraftBook");

        config = new SpongeConfiguration(this, mainConfig, configManager);

        discoverMechanics();

        logger.info("Loading Configuration");

        config.load();

        cache = new SpongeDataCache();
        blockBagManager = new BlockBagManager();

        for (Entry<String, Class<? extends Mechanic>> mech : getAvailableMechanics()) {

            if (config.enabledMechanics.contains(mech.getKey())) {
                try {
                    Mechanic mechanic = createMechanic(mech.getValue());
                    enabledMechanics.add(mechanic);
                    game.getEventManager().register(this, mechanic);

                    logger.info("Enabled: " + mech.getKey());
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }

        SelfTriggerManager.initialize();
    }

    @Override
    public void discoverMechanics() {

        logger.info("Enumerating Mechanics");
        registerSpongeMechanic(Elevator.class);
        registerSpongeMechanic(Snow.class);
        registerSpongeMechanic(Bridge.class);
        registerSpongeMechanic(Door.class);
        registerSpongeMechanic(Gate.class);
        registerSpongeMechanic(Footprints.class);
        registerSpongeMechanic(HeadDrops.class);
        registerSpongeMechanic(TreeLopper.class);

        registerSpongeMechanic(ICSocket.class);

        registerSpongeMechanic(EmptyDecay.class);
        logger.info("Found " + getAvailableMechanics().size() + ".");
    }

    public boolean registerSpongeMechanic(Class<? extends SpongeMechanic> clazz) {

        try {
            registerMechanic(clazz.getSimpleName(), clazz);

            return true;
        } catch (Throwable e) {
            e.printStackTrace();

            return false;
        }
    }

    @Override
    public MechanicDataCache getCache() {
        return cache;
    }
}
