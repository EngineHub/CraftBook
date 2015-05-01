package com.sk89q.craftbook.sponge;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import com.google.common.base.Function;
import com.sk89q.craftbook.core.Mechanic;

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
                config = configManager.load();

                // Create default configuration - TODO
                config.getNode("enabled-mechanics").setValue(Arrays.asList("Elevator"));

                configManager.save(config);
            }
            config = configManager.load();

            enabledMechanics = config.getNode("enabled-mechanics").<String> getList(new Function<Object, String>() {
                @Override
                public String apply(Object input) {
                    return String.valueOf(input);
                }
            }, Arrays.asList("Elevator", "Snow"));

            List<String> disabledMechanics = new ArrayList<String>();

            for(Entry<String, Class<? extends Mechanic>> entry : plugin.getAvailableMechanics()) {
                if(!enabledMechanics.contains(entry.getKey())) {
                    disabledMechanics.add(entry.getKey());
                }
            }

            config.getNode("disabled-mechanics").setValue(disabledMechanics);

            configManager.save(config);
        } catch (IOException exception) {
            plugin.getLogger().error("The CraftBook Configuration could not be read!");
            exception.printStackTrace();
        }
    }
}
