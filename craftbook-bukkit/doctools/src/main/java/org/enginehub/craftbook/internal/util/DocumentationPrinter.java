package org.enginehub.craftbook.internal.util;

import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlatform;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

public class DocumentationPrinter {

    private static CraftBookPlatform craftBookPlatform;

    public static Path getGenerationFolder() {
        return Paths.get("docgen_output");
    }

    private static void cleanupOldData() {
        try {
            if (Files.exists(getGenerationFolder())) {
                try (Stream<Path> walk = Files.walk(getGenerationFolder())) {
                    //noinspection ResultOfMethodCallIgnored
                    walk.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .peek(System.out::println)
                        .forEach(File::delete);
                }
            }

            Files.createDirectory(getGenerationFolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void init() {
        cleanupOldData();

        craftBookPlatform = new DocumentationPlatform();
        CraftBook.getInstance().setPlatform(craftBookPlatform);
        craftBookPlatform.load();
    }

    public static void main(String[] args) {
        init();

        ConfigurationGenerator.generateDefaultConfiguration();
        MechanicTypesGenerator.generateMechanicTypes();
//        CommandDocumentationGenerator.generateCommandDocumentation();
        MechanicConfigurationGenerator.generateMechanicConfiguration();
    }
}
