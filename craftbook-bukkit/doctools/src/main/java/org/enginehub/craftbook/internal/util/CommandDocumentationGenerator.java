package org.enginehub.craftbook.internal.util;

import com.google.common.collect.Lists;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.enginehub.craftbook.internal.util.DocumentationPrinter.getGenerationFolder;

public class CommandDocumentationGenerator {

    private static void generateCommandsFile(String name, String data) throws IOException {
        Path commandsFolder = getGenerationFolder().resolve("commands");
        if (!Files.exists(commandsFolder)) {
            Files.createDirectory(commandsFolder);
        }
        Path file = commandsFolder.resolve(name + ".rst");

        Files.createFile(file);

        PrintWriter pw = new PrintWriter(file.toFile(), "UTF-8");
        pw.write(data);
        pw.close();
    }

    public static void generateCommandDocumentation() {
        try {
            generateCommandsFile("headdrops", CommandUtils.dumpSection("HeadDrops", Lists.newArrayList("headdrops")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            generateCommandsFile("signcopier", CommandUtils.dumpSection("SignCopier", Lists.newArrayList("signedit")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
