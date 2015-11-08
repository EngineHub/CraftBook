package com.sk89q.craftbook.sponge;

import com.google.inject.Inject;
import com.me4502.modularframework.ModuleController;
import com.me4502.modularframework.ShadedModularFramework;
import com.me4502.modularframework.exception.ModuleNotInstantiatedException;
import com.me4502.modularframework.module.ModuleWrapper;
import com.sk89q.craftbook.core.CraftBookAPI;
import com.sk89q.craftbook.core.util.MechanicDataCache;
import com.sk89q.craftbook.sponge.blockbags.BlockBagManager;
import com.sk89q.craftbook.sponge.st.SelfTriggerManager;
import com.sk89q.craftbook.sponge.st.SelfTriggeringMechanic;
import com.sk89q.craftbook.sponge.util.SpongeDataCache;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.config.DefaultConfig;

import java.io.File;

@Plugin(id = "CraftBook", name = "CraftBook", version = "4.0"/* , dependencies = "required-after:WorldEdit@[6.0,)" */)
public class CraftBookPlugin extends CraftBookAPI {

    public static Game game;

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

    public ModuleController moduleController;

    protected SpongeConfiguration config;

    /* Logging */

    @Inject
    protected Logger logger;

    public Logger getLogger() {
        return logger;
    }

    @Listener
    public void onInitialization(GameStartedServerEvent event) throws IllegalAccessException {
        game = event.getGame();
        setInstance(this);

        new File("craftbook-data").mkdir();

        logger.info("Starting CraftBook");

        config = new SpongeConfiguration(this, mainConfig, configManager);

        discoverMechanics();

        logger.info("Loading Configuration");

        config.load();

        cache = new SpongeDataCache();
        blockBagManager = new BlockBagManager();

        moduleController.enableModules(input -> {
            if (config.enabledMechanics.contains(input.getName()) || "true".equalsIgnoreCase(System.getProperty("craftbook.enable-all"))) {
                logger.info("Enabled: " + input.getName());
                return true;
            }

            return false;
        });

        for (ModuleWrapper module : CraftBookPlugin.<CraftBookPlugin>inst().moduleController.getModules()) {
            if (!module.isEnabled()) continue;
            try {
                if (module.getModule() instanceof SelfTriggeringMechanic && !SelfTriggerManager.isInitialized) {
                    SelfTriggerManager.initialize();
                    break;
                }
            } catch(ModuleNotInstantiatedException e) {
                e.printStackTrace();
            }
        }
    }

    @Listener
    public void onServerStopping(GameStoppingServerEvent event) {
        moduleController.disableModules();
        cache.clearAll();
    }

    @Override
    public void discoverMechanics() {
        logger.info("Enumerating Mechanics");

        moduleController = ShadedModularFramework.registerModuleController(this, game);
        File configDir = new File(mainConfig.getParent(), "mechanics");
        configDir.mkdir();
        moduleController.setConfigurationDirectory(configDir);

        //Standard Mechanics
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.Elevator");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.Snow");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.area.Bridge");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.area.Door");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.area.Gate");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.Footprints");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.HeadDrops");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.TreeLopper");

        //Circuit Mechanics
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.ics.ICSocket");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.GlowStone");

        //Vehicle Mechanics
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.minecart.EmptyDecay");

        logger.info("Found " + moduleController.getModules().size() + ".");
    }

    @Override
    public MechanicDataCache getCache() {
        return cache;
    }
}
