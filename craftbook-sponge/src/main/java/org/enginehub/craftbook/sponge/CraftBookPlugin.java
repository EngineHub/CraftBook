/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
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
package org.enginehub.craftbook.sponge;

import com.google.inject.Inject;
import com.me4502.modularframework.ShadedModularFramework;
import com.me4502.modularframework.SpongeModuleController;
import com.me4502.modularframework.exception.ModuleNotInstantiatedException;
import com.me4502.modularframework.module.ModuleWrapper;
import com.me4502.modularframework.module.SpongeModuleWrapper;
import org.enginehub.craftbook.CraftBookAPI;
import org.enginehub.craftbook.Mechanic;
import org.enginehub.craftbook.st.SelfTriggerManager;
import org.enginehub.craftbook.util.RegexUtil;
import org.enginehub.craftbook.util.documentation.DocumentationGenerator;
import org.enginehub.craftbook.util.documentation.DocumentationProvider;
import org.enginehub.craftbook.sponge.command.AboutCommand;
import org.enginehub.craftbook.sponge.command.ReportCommand;
import org.enginehub.craftbook.sponge.command.docs.GenerateDocsCommand;
import org.enginehub.craftbook.sponge.command.docs.GetDocsCommand;
import org.enginehub.craftbook.sponge.st.SelfTriggeringMechanic;
import org.enginehub.craftbook.sponge.st.SpongeSelfTriggerManager;
import org.enginehub.craftbook.sponge.util.Metrics;
import org.enginehub.craftbook.sponge.util.data.CraftBookData;
import org.enginehub.craftbook.sponge.util.locale.TranslationsManager;
import org.enginehub.craftbook.sponge.util.type.TypeSerializers;
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
        moduleController.registerModule("Variables", GameState.PRE_INITIALIZATION);
        moduleController.registerModule("BlockBagManager", GameState.PRE_INITIALIZATION);
        moduleController.registerModule("BetterPhysics");
        moduleController.registerModule("BetterPlants");
        moduleController.registerModule("BounceBlocks");
        moduleController.registerModule("Chairs");
        moduleController.registerModule("ChunkAnchor");
        moduleController.registerModule("CookingPot", GameState.PRE_INITIALIZATION);
        moduleController.registerModule("CommandSigns");
        moduleController.registerModule("Elevator");
        moduleController.registerModule("Snow");
        moduleController.registerModule("Bridge");
        moduleController.registerModule("Door");
        moduleController.registerModule("Gate");
        moduleController.registerModule("ComplexArea");
        moduleController.registerModule("Bookshelf");
        moduleController.registerModule("Footprints");
        moduleController.registerModule("HeadDrops");
        moduleController.registerModule("HiddenSwitch");
        moduleController.registerModule("LightStone");
        moduleController.registerModule("Teleporter");
        moduleController.registerModule("TreeLopper");
        moduleController.registerModule("PaintingSwitcher");
        moduleController.registerModule("Pipes");
        moduleController.registerModule("LightSwitch");
        moduleController.registerModule("Marquee");
        moduleController.registerModule("SignCopier");
        moduleController.registerModule("DispenserRecipes");
        moduleController.registerModule("XPStorer", GameState.PRE_INITIALIZATION);

        //Circuit Mechanics
        moduleController.registerModule("Ammeter");
        moduleController.registerModule("ICSocket", GameState.PRE_INITIALIZATION);
        moduleController.registerModule("GlowStone");
        moduleController.registerModule("Netherrack");
        moduleController.registerModule("JackOLantern");
        moduleController.registerModule("RedstoneJukebox");

        //Vehicle Mechanics
        //Boat
        moduleController.registerModule("EmptyDecay");
        moduleController.registerModule("ExitRemover");
        moduleController.registerModule("LandBoats");
        moduleController.registerModule("RemoveEntities");
        moduleController.registerModule("SpeedModifiers");
        moduleController.registerModule("WaterPlaceOnly");
        //Minecart
        moduleController.registerModule("EmptyDecay");
        moduleController.registerModule("ExitRemover");
        moduleController.registerModule("ItemPickup");
        moduleController.registerModule("MobBlocker");
        moduleController.registerModule("MoreRails");
        moduleController.registerModule("NoCollide");
        moduleController.registerModule("RemoveEntities");
        //Minecart - Block
        moduleController.registerModule("CartEjector");
        moduleController.registerModule("CartMessenger");
        moduleController.registerModule("CartReverser");

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
