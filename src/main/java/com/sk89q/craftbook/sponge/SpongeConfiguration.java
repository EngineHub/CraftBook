package com.sk89q.craftbook.sponge;

import com.me4502.modularframework.module.ModuleWrapper;
import com.sk89q.craftbook.core.util.ConfigValue;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SpongeConfiguration {

    private CraftBookPlugin plugin;
    private File mainConfig;
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    private CommentedConfigurationNode config;

    public ConfigValue<List<String>> enabledMechanics = new ConfigValue<>("enabled-mechanics", "The list of mechanics to load.", Collections.singletonList("Elevator"), null);

    public SpongeConfiguration(CraftBookPlugin plugin, File mainConfig, ConfigurationLoader<CommentedConfigurationNode> configManager) {
        this.plugin = plugin;
        this.mainConfig = mainConfig;
        this.configManager = configManager;
    }

    public void load() {
        try {
            if (!mainConfig.exists()) {
                mainConfig.getParentFile().mkdirs();
                mainConfig.createNewFile();
            }

            config = configManager.load(CraftBookPlugin.<CraftBookPlugin>inst().configurationOptions);

            enabledMechanics.load(config);

            List<String> disabledMechanics = plugin.moduleController.getModules().stream().filter(entry -> !enabledMechanics.getValue().contains(entry.getName())).map(ModuleWrapper::getName).collect(Collectors.toList());

            config.getNode("disabled-mechanics").setValue(disabledMechanics).setComment("This contains all disabled mechanics. It is never read internally, but just acts as a convenient place to grab mechanics from.");

            configManager.save(config);
        } catch (IOException exception) {
            plugin.getLogger().error("The CraftBook Configuration could not be read!");
            exception.printStackTrace();
        }
    }

    public void save() {
        try {
            enabledMechanics.save(config);

            List<String> disabledMechanics = plugin.moduleController.getModules().stream().filter(entry -> !enabledMechanics.getValue().contains(entry.getName())).map(ModuleWrapper::getName).collect(Collectors.toList());

            config.getNode("disabled-mechanics").setValue(disabledMechanics).setComment("This contains all disabled mechanics. It is never read internally, but just acts as a convenient place to grab mechanics from.");

            configManager.save(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
