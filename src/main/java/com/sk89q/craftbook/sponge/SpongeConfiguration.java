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

    public ConfigValue<List<String>> enabledMechanics = new ConfigValue<>("enabled-mechanics", "The list of mechanics to load.", Collections.singletonList("Variables"), null);

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
