package org.enginehub.craftbook.internal.util;

import com.sk89q.craftbook.mechanic.CraftBookMechanic;
import com.sk89q.craftbook.mechanic.MechanicType;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;

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
                System.out.println("Generating " + mechanicRegistration.getId() + " docs data");
                CraftBookMechanic me = mechanicRegistration.getMechanicClass().getDeclaredConstructor().newInstance();
                Path file = getMechanicConfigFolder().resolve(mechanicRegistration.getId() + ".yml");

                YAMLProcessor mechanicConfig = new YAMLProcessor(file.toFile(), true, YAMLFormat.EXTENDED);

                try {
                    mechanicConfig.load();
                } catch (FileNotFoundException e) {
                    // Ignore this one.
                } catch (IOException e) {
                    e.printStackTrace();
                }

                me.loadFromConfiguration(mechanicConfig);

                generateConfigRst(mechanicRegistration, mechanicConfig);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private static void generateConfigRst(MechanicType<?> type, YAMLProcessor config) throws IOException {
        Path file = getMechanicConfigFolder().resolve(type.getId() + ".rst");

        Files.createFile(file);

        PrintWriter pw = new PrintWriter(file.toFile(), "UTF-8");
        pw.write(RstUtils.configToString(config));
        pw.close();
    }
}
