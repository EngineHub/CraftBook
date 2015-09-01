package com.sk89q.craftbook.sponge;

import com.me4502.modularframework.module.ModuleWrapper;
import ninja.leaping.configurate.ConfigurationNode;
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

    public List<String> enabledMechanics;

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
            config = configManager.load();

            enabledMechanics = getValue(config.getNode("enabled-mechanics"), Collections.singletonList("Elevator"), "The list of mechanics to load.");

            List<String> disabledMechanics = plugin.moduleController.getModules().stream().filter(entry -> !enabledMechanics.contains(entry.getName())).map(ModuleWrapper::getName).collect(Collectors.toList());

            config.getNode("disabled-mechanics").setValue(disabledMechanics).setComment("This contains all disabled mechanics. It is never read internally, but just acts as a convenient place to grab mechanics from.");

            configManager.save(config);
        } catch (IOException exception) {
            plugin.getLogger().error("The CraftBook Configuration could not be read!");
            exception.printStackTrace();
        }
    }

    public static <T> T getValue(ConfigurationNode node, T defaultValue, String comment) {
        if(node.isVirtual()) {
            node.setValue(defaultValue);
        }

        if(comment != null && node instanceof CommentedConfigurationNode) {
            ((CommentedConfigurationNode)node).setComment(comment);
        }

        return node.getValue(input -> {

            //Add converters into here where necessary.

            return (T)input;
        }, defaultValue);
    }
}
