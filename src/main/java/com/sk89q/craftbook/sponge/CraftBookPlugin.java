/*
 * CraftBook Copyright (C) 2010-2016 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2016 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package com.sk89q.craftbook.sponge;

import com.google.inject.Inject;
import com.me4502.modularframework.ModuleController;
import com.me4502.modularframework.ShadedModularFramework;
import com.me4502.modularframework.exception.ModuleNotInstantiatedException;
import com.me4502.modularframework.module.ModuleWrapper;
import com.sk89q.craftbook.core.CraftBookAPI;
import com.sk89q.craftbook.core.Mechanic;
import com.sk89q.craftbook.core.util.MechanicDataCache;
import com.sk89q.craftbook.core.util.documentation.DocumentationGenerator;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.st.SelfTriggerManager;
import com.sk89q.craftbook.sponge.st.SelfTriggeringMechanic;
import com.sk89q.craftbook.sponge.util.SpongeDataCache;
import com.sk89q.craftbook.sponge.util.data.CraftBookData;
import com.sk89q.craftbook.sponge.util.locale.TranslationsManager;
import com.sk89q.craftbook.sponge.util.type.TypeSerializers;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.File;

@Plugin(id = "craftbook", name = "CraftBook", version = "4.0")
public class CraftBookPlugin extends CraftBookAPI {

    private MechanicDataCache cache;

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

    ConfigurationOptions configurationOptions;

    /* Logging */

    @Inject
    private Logger logger;

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Listener
    public void onPreInitialization(GamePreInitializationEvent event) {
        setInstance(this);

        logger.info("Performing Pre-Initialization");
        CraftBookData.registerData();
    }

    @Listener
    public void onInitialization(GameStartedServerEvent event) throws IllegalAccessException {
        TypeSerializers.registerDefaults();

        new File("craftbook-data").mkdir();

        logger.info("Starting CraftBook");
        config = new SpongeConfiguration(this, mainConfig, configManager);

        configurationOptions = ConfigurationOptions.defaults();
        TypeSerializers.register(configurationOptions);

        logger.info("Loading Configuration");

        config.load();
        TranslationsManager.initialize();

        if(config.dataOnlyMode.getValue()) {
            logger.info("Halting CraftBook Initialization - Data Only Mode!");
            return;
        }

        discoverMechanics();

        cache = new SpongeDataCache();

        moduleController.enableModules(input -> {
            if (config.enabledMechanics.getValue().contains(input.getName())
                    || "true".equalsIgnoreCase(System.getProperty("craftbook.enable-all"))
                    || "true".equalsIgnoreCase(System.getProperty("craftbook.generate-docs"))) {
                logger.debug("Enabled: " + input.getName());
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
                CraftBookAPI.<CraftBookPlugin>inst().logger.error("Failed to initialize module: " + module.getName(), e);
            }
        }

        if("true".equalsIgnoreCase(System.getProperty("craftbook.generate-docs"))) {
            for (ModuleWrapper module : CraftBookPlugin.<CraftBookPlugin>inst().moduleController.getModules()) {
                if(!module.isEnabled()) continue;
                try {
                    Mechanic mechanic = (Mechanic) module.getModule();
                    if(mechanic instanceof DocumentationProvider)
                        DocumentationGenerator.generateDocumentation((DocumentationProvider) mechanic);
                } catch (ModuleNotInstantiatedException e) {
                    CraftBookAPI.<CraftBookPlugin>inst().logger.error("Failed to generate docs for module: " + module.getName(), e);
                }
            }

            DocumentationGenerator.generateDocumentation(config);
        }

        config.save(); //Do initial save of config.
    }

    @Listener
    public void onServerStopping(GameStoppingServerEvent event) {
        config.save();

        SelfTriggerManager.unload();
        moduleController.disableModules();
        cache.clearAll();
    }

    @Override
    public void discoverMechanics() {
        logger.info("Enumerating Mechanics");

        moduleController = ShadedModularFramework.registerModuleController(this, Sponge.getGame());
        File configDir = new File(getWorkingDirectory(), "mechanics");
        configDir.mkdir();
        moduleController.setConfigurationDirectory(configDir);
        moduleController.setConfigurationOptions(configurationOptions);

        //Standard Mechanics
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.variable.Variables");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.blockbags.BlockBagManager");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.Elevator");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.Snow");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.area.Bridge");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.area.Door");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.area.Gate");
        //TODO moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.area.complex.ComplexArea");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.Footprints");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.HeadDrops");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.LightStone");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.TreeLopper");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.pipe.Pipes");

        //Circuit Mechanics
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.Ammeter");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.ics.ICSocket");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.powerable.GlowStone");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.powerable.Netherrack");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.powerable.JackOLantern");

        //Vehicle Mechanics
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.minecart.EmptyDecay");

        logger.info("Found " + moduleController.getModules().size());
    }

    @Override
    public MechanicDataCache getCache() {
        return cache;
    }

    @Override
    public File getWorkingDirectory() {
        return mainConfig.getParentFile();
    }

}
