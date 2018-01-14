/*
 * CraftBook Copyright (C) 2010-2018 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2018 me4502 <http://www.me4502.com>
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
import com.me4502.modularframework.ShadedModularFramework;
import com.me4502.modularframework.SpongeModuleController;
import com.me4502.modularframework.exception.ModuleNotInstantiatedException;
import com.me4502.modularframework.module.ModuleWrapper;
import com.me4502.modularframework.module.SpongeModuleWrapper;
import com.sk89q.craftbook.core.CraftBookAPI;
import com.sk89q.craftbook.core.Mechanic;
import com.sk89q.craftbook.core.st.SelfTriggerManager;
import com.sk89q.craftbook.core.util.RegexUtil;
import com.sk89q.craftbook.core.util.documentation.DocumentationGenerator;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.command.AboutCommand;
import com.sk89q.craftbook.sponge.command.ReportCommand;
import com.sk89q.craftbook.sponge.command.docs.GenerateDocsCommand;
import com.sk89q.craftbook.sponge.command.docs.GetDocsCommand;
import com.sk89q.craftbook.sponge.st.SelfTriggeringMechanic;
import com.sk89q.craftbook.sponge.st.SpongeSelfTriggerManager;
import com.sk89q.craftbook.sponge.util.Metrics;
import com.sk89q.craftbook.sponge.util.data.CraftBookData;
import com.sk89q.craftbook.sponge.util.locale.TranslationsManager;
import com.sk89q.craftbook.sponge.util.type.TypeSerializers;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.GameState;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Plugin(id = "craftbook", name = "CraftBook", version = "4.0-SNAPSHOT",
        description = "CraftBook adds a number of new mechanics to Minecraft with no client mods required.")
public class CraftBookPlugin extends CraftBookAPI {

    /* Configuration Data */

    @Inject
    @DefaultConfig(sharedRoot = false)
    private File mainConfig;

    @Inject
    private Metrics metrics;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    @Inject
    public PluginContainer container;

    public SpongeModuleController<CraftBookPlugin> moduleController;

    private SelfTriggerManager selfTriggerManager;

    protected SpongeConfiguration config;

    ConfigurationOptions configurationOptions;

    public static String BUILD_NUMBER = "UNKNOWN";
    public static String GIT_HASH = "UNKNOWN";

    /* Logging */

    @Inject
    private Logger logger;

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public String getVersionString() {
        return this.container.getVersion().orElse("UNKNOWN") + '-' + BUILD_NUMBER + '-' + GIT_HASH;
    }

    public PluginContainer getContainer() {
        return this.container;
    }

    @Listener
    public void onPreInitialization(GamePreInitializationEvent event) {
        logger.info("Performing Pre-Initialization");
        setInstance(this);

        // Load build information.
        container.getAsset("build.txt").ifPresent(asset -> {
            try {
                for (String line : asset.readLines()) {
                    if (line.startsWith("hash=")) {
                        if (line.contains("-")) {
                            String[] bits = RegexUtil.MINUS_PATTERN.split(line.substring(5));
                            BUILD_NUMBER = bits[0];
                            GIT_HASH = bits[1];
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        TypeSerializers.registerDefaults();
        CraftBookData.registerData();

        discoverMechanics();

        loadConfig();

        if(config.dataOnlyMode.getValue()) {
            logger.info("Halting CraftBook Initialization - Data Only Mode! Note: Nothing will work.");
            return;
        }

        loadMechanics(event.getState());
    }

    @Listener
    public void onInitialization(GameStartedServerEvent event) {
        if(config.dataOnlyMode.getValue()) {
            logger.info("Halting CraftBook Initialization - Data Only Mode! Note: Nothing will work.");
            return;
        }

        logger.info("Starting CraftBook");

        CommandSpec generateDocsCommandSpec = CommandSpec.builder()
                .description(Text.of("Generates Documentation"))
                .permission("craftbook.docs.generate")
                .executor(new GenerateDocsCommand())
                .build();

        CommandSpec getDocsCommandSpec = CommandSpec.builder()
                .description(Text.of("Gets a Link to the Documentation"))
                .permission("craftbook.docs.get")
                .executor(new GetDocsCommand())
                .build();

        CommandSpec docsCommandSpec = CommandSpec.builder()
                .description(Text.of("Docs Base Command"))
                .permission("craftbook.docs")
                .child(generateDocsCommandSpec, "generate", "make", "build")
                .child(getDocsCommandSpec, "get", "help", "link")
                .build();

        CommandSpec aboutCommandSpec = CommandSpec.builder()
                .description(Text.of("CraftBook About Command"))
                .permission("craftbook.about")
                .executor(new AboutCommand())
                .build();

        CommandSpec reportCommandSpec = CommandSpec.builder()
                .description(Text.of("CraftBook Report Command"))
                .permission("craftbook.report")
                .arguments(
                        GenericArguments.flags().permissionFlag("craftbook.report.pastebin", "p").buildWith(GenericArguments.none())
                )
                .executor(new ReportCommand())
                .build();

        CommandSpec craftBookCommandSpec = CommandSpec.builder()
                .description(Text.of("CraftBook Base Command"))
                .permission("craftbook.craftbook")
                .child(docsCommandSpec, "docs", "manual", "man", "documentation", "doc", "help")
                .child(aboutCommandSpec, "about", "version", "ver")
                .child(reportCommandSpec, "report", "dump")
                .build();

        Sponge.getCommandManager().register(this, craftBookCommandSpec, "cb", "craftbook");

        loadMechanics(event.getState());

        saveConfig(); //Do initial save of config.
    }

    @Listener
    public void onServerReload(GameReloadEvent event) {
        disableMechanics();
        loadConfig();
        for (GameState state : GameState.values()) {
            loadMechanics(state);
        }
    }

    @Listener
    public void onServerStopping(GameStoppingServerEvent event) {
        saveConfig();

        disableMechanics();
    }

    private void loadConfig() {
        config = new SpongeConfiguration(this, mainConfig, configManager);

        configurationOptions = ConfigurationOptions.defaults();

        logger.info("Loading Configuration");

        config.load();
        TranslationsManager.initialize();
    }

    public void saveConfig() {
        config.save();
    }

    @Override
    public void discoverMechanics() {
        logger.info("Enumerating Mechanics");

        moduleController = ShadedModularFramework.registerModuleController(this, Sponge.getGame());
        File configDir = new File(getWorkingDirectory(), "mechanics");
        configDir.mkdir();
        moduleController.setConfigurationDirectory(configDir);
        moduleController.setConfigurationOptions(configurationOptions);
        moduleController.setOverrideConfigurationNode(false);

        //Standard Mechanics
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.variable.Variables", GameState.PRE_INITIALIZATION);
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.blockbags.BlockBagManager", GameState.PRE_INITIALIZATION);
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.BetterPhysics");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.BetterPlants");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.BounceBlocks");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.Chairs");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.ChunkAnchor");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.CookingPot", GameState.PRE_INITIALIZATION);
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.CommandSigns");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.Elevator");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.Snow");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.area.Bridge");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.area.Door");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.area.Gate");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.area.complex.ComplexArea");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.Bookshelf");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.Footprints");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.HeadDrops");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.HiddenSwitch");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.LightStone");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.Teleporter");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.treelopper.TreeLopper");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.PaintingSwitcher");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.pipe.Pipes");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.LightSwitch");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.Marquee");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.signcopier.SignCopier");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.dispenser.DispenserRecipes");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.XPStorer", GameState.PRE_INITIALIZATION);

        //Circuit Mechanics
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.Ammeter");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.ics.ICSocket", GameState.PRE_INITIALIZATION);
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.powerable.GlowStone");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.powerable.Netherrack");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.powerable.JackOLantern");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.powerable.RedstoneJukebox");

        //Vehicle Mechanics
        //Boat
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.boat.EmptyDecay");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.boat.ExitRemover");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.boat.LandBoats");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.boat.RemoveEntities");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.boat.SpeedModifiers");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.boat.WaterPlaceOnly");
        //Minecart
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.minecart.EmptyDecay");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.minecart.ExitRemover");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.minecart.ItemPickup");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.minecart.MobBlocker");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.minecart.MoreRails");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.minecart.NoCollide");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.minecart.RemoveEntities");
        //Minecart - Block
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.minecart.block.CartEjector");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.minecart.block.CartMessenger");
        moduleController.registerModule("com.sk89q.craftbook.sponge.mechanics.minecart.block.CartReverser");

        logger.info("Found " + moduleController.getModules().size());
    }

    private void loadMechanics(GameState loadState) {
        moduleController.enableModules(input -> {
            if (loadState == ((SpongeModuleWrapper) input).getLoadState() && (config.enabledMechanics.getValue().contains(input.getName())
                    || "true".equalsIgnoreCase(System.getProperty("craftbook.enable-all"))
                    || "true".equalsIgnoreCase(System.getProperty("craftbook.generate-docs")))) {
                logger.debug("Enabled: " + input.getName());
                return true;
            }

            return false;
        });

        for (ModuleWrapper module : moduleController.getModules()) {
            if (!module.isEnabled()) continue;
            try {
                if (((SpongeModuleWrapper) module).getModuleUnchecked() instanceof SelfTriggeringMechanic && !getSelfTriggerManager().isPresent()) {
                    this.selfTriggerManager = new SpongeSelfTriggerManager();
                    getSelfTriggerManager().ifPresent(SelfTriggerManager::initialize);
                    break;
                }
            } catch(ModuleNotInstantiatedException e) {
                logger.error("Failed to initialize module: " + module.getName(), e);
            }
        }

        if("true".equalsIgnoreCase(System.getProperty("craftbook.generate-docs"))) {
            for (ModuleWrapper module : moduleController.getModules()) {
                if(!module.isEnabled()) continue;
                try {
                    Mechanic mechanic = (Mechanic) ((SpongeModuleWrapper) module).getModuleUnchecked();
                    if(mechanic instanceof DocumentationProvider)
                        DocumentationGenerator.generateDocumentation((DocumentationProvider) mechanic);
                } catch (ModuleNotInstantiatedException e) {
                    logger.error("Failed to generate docs for module: " + module.getName(), e);
                }
            }

            DocumentationGenerator.generateDocumentation(config);
        }
    }

    private void disableMechanics() {
        getSelfTriggerManager().ifPresent(SelfTriggerManager::unload);
        this.selfTriggerManager = null;
        moduleController.disableModules();
    }

    @Override
    public Optional<SelfTriggerManager> getSelfTriggerManager() {
        return Optional.ofNullable(this.selfTriggerManager);
    }

    public SpongeConfiguration getConfig() {
        return config;
    }

    @Override
    public File getWorkingDirectory() {
        return mainConfig.getParentFile();
    }

    public static CraftBookPlugin spongeInst() {
        return inst();
    }
}
