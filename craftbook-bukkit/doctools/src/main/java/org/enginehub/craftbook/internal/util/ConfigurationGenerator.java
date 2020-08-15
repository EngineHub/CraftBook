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
