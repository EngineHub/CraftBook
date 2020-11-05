package org.enginehub.craftbook.internal.util;

import com.sk89q.worldedit.cli.CLIWorldEdit;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlatform;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

public class DocumentationPrinter {

    private static CraftBookPlatform craftBookPlatform;
    private static CLIWorldEdit worldEdit;

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

        worldEdit = new CLIWorldEdit();
        worldEdit.onInitialized();
        try {
            Method method = worldEdit.getPlatform().getClass().getDeclaredMethod("setDataVersion", int.class);
            method.setAccessible(true);
            method.invoke(worldEdit.getPlatform(), 2567);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        worldEdit.onStarted();

        craftBookPlatform = new DocumentationPlatform();
        CraftBook.getInstance().setPlatform(craftBookPlatform);
        CraftBook.getInstance().setup();
    }

    public static void main(String[] args) {
        init();

        ConfigurationGenerator.generateDefaultConfiguration();
        MechanicTypesGenerator.generateMechanicTypes();
        CommandDocumentationGenerator.generateCommandDocumentation();
        MechanicConfigurationGenerator.generateMechanicConfiguration();

        worldEdit.onStopped();
    }
}
