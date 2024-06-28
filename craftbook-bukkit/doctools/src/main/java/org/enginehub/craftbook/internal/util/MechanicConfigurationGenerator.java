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
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class MechanicConfigurationGenerator {

    private static Path getMechanicConfigFolder() {
        Path path = DocumentationPrinter.getGenerationFolder().resolve("mechanics");
        try {
            if (!Files.exists(path)) {
                Files.createDirectory(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    public static void generateMechanicConfiguration() {
        MechanicType.REGISTRY.values().forEach(mechanicRegistration -> {
            try {
                System.out.println("Generating " + mechanicRegistration.id() + " docs data");
                CraftBookMechanic me = mechanicRegistration.getMechanicClass().getDeclaredConstructor(MechanicType.class).newInstance(mechanicRegistration);
                Path file = getMechanicConfigFolder().resolve(mechanicRegistration.id() + ".yml");

                YAMLProcessor mechanicConfig = new YAMLProcessor(file.toFile(), true, YAMLFormat.EXTENDED);

                try {
                    mechanicConfig.load();
                } catch (FileNotFoundException e) {
                    // Ignore this one.
                } catch (Throwable e) {
                    System.out.println("Failed to load config for " + mechanicRegistration.id() + " - " + e.getMessage());
                    e.printStackTrace();
                }

                me.loadFromConfiguration(mechanicConfig);

                generateConfigRst(mechanicRegistration, mechanicConfig);
            } catch (Throwable t) {
                System.out.println("Failed to load config for " + mechanicRegistration.id() + " - " + t.getMessage());
                t.printStackTrace();
            }
        });
    }

    private static void generateConfigRst(MechanicType<?> type, YAMLProcessor config) throws IOException {
        Path file = getMechanicConfigFolder().resolve(type.id() + ".rst");

        Files.createFile(file);

        PrintWriter pw = new PrintWriter(file.toFile(), "UTF-8");
        pw.write(RstUtils.configToString(config));
        pw.close();
    }
}
