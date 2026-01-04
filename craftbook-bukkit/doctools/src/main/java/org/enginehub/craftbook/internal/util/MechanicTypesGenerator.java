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

import org.enginehub.craftbook.mechanic.MechanicType;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

public class MechanicTypesGenerator {

    public static void generateMechanicTypes() {
        Path mechanicTypesFile = DocumentationPrinter.getGenerationFolder().resolve("MechanicTypes.java");

        try (FileWriter writer = new FileWriter(mechanicTypesFile.toFile())) {
            MechanicType.REGISTRY.keySet().stream().sorted().forEach(mech -> {
                MechanicType<?> type = MechanicType.REGISTRY.get(mech);
                try {
                    writer.write("public static final Supplier<@Nullable MechanicType<" + type.getMechanicClass().getSimpleName() + ">> " + mech.toUpperCase(Locale.ENGLISH) + " = get(\"" + mech + "\");\n");
                } catch (Throwable e) {
                    System.err.println("Failed to generate mechanic type for " + mech);
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
