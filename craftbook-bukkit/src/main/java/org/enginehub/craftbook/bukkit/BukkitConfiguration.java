/*
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

package org.enginehub.craftbook.bukkit;

import com.sk89q.util.yaml.YAMLProcessor;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.YamlConfiguration;

import java.io.IOException;
import java.nio.file.Files;

public class BukkitConfiguration extends YamlConfiguration {

    public BukkitConfiguration(YAMLProcessor config) {
        super(config);
    }

    @Override
    public void load() {
        try {
            config.load();
        } catch (IOException e) {
            CraftBook.LOGGER.error("Error loading CraftBook configuration", e);
        }

        if (config.getList("enabled-mechanics") != null) {
            try {
                Files.move(
                        CraftBook.getInstance().getPlatform().getWorkingDirectory().resolve("config.yml"),
                        CraftBook.getInstance().getPlatform().getWorkingDirectory().resolve("config.yml.old")
                );

                CraftBookPlugin.inst().createDefaultConfiguration("config.yml");

                config.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        super.load();
    }
}
