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

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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
            generateCommandsFile("headdrops", CommandUtils.dumpSection("HeadDrops", List.of("headdrops")));
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            generateCommandsFile("signcopier", CommandUtils.dumpSection("SignCopier", List.of("signedit")));
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            generateCommandsFile("togglearea", CommandUtils.dumpSection("ToggleArea", List.of("area")));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
