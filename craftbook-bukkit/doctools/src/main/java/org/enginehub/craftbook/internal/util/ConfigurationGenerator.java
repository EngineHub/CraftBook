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

package org.enginehub.craftbook.internal.util;

import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;
import org.enginehub.craftbook.YamlConfiguration;
import org.enginehub.craftbook.bukkit.BukkitConfiguration;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigurationGenerator {

    private static Path getResourcesFolder() {
        return Paths.get("craftbook-bukkit/src/main/resources/defaults");
    }

    public static void generateDefaultConfiguration() {
        Path file = getResourcesFolder().resolve("config.yml");

        try {
            Files.deleteIfExists(file);
            Files.createFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        YamlConfiguration config = new BukkitConfiguration(new YAMLProcessor(file.toFile(), true, YAMLFormat.EXTENDED));
        config.load();

        try {
            generateConfigRst(config.config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generateConfigRst(YAMLProcessor config) throws IOException {
        Path file = DocumentationPrinter.getGenerationFolder().resolve("config.rst");

        Files.createFile(file);

        PrintWriter pw = new PrintWriter(file.toFile(), "UTF-8");
        pw.write(RstUtils.configToString(config));
        pw.close();
    }
}
