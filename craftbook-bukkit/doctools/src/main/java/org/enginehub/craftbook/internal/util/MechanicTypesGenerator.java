package org.enginehub.craftbook.internal.util;

import org.enginehub.craftbook.mechanic.MechanicType;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class MechanicTypesGenerator {

    public static void generateMechanicTypes() {
        Path mechanicTypesFile = DocumentationPrinter.getGenerationFolder().resolve("MechanicTypes.java");

        try (FileWriter writer = new FileWriter(mechanicTypesFile.toFile())) {
            MechanicType.REGISTRY.keySet().stream().sorted().forEach(mech -> {
                MechanicType<?> type = MechanicType.REGISTRY.get(mech);
                try {
                    writer.write("@Nullable public static final MechanicType<" + type.getMechanicClass().getSimpleName() + "> " + mech.toUpperCase() + " = get(\"" + mech + "\");\n");
                } catch (IOException | ReflectiveOperationException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
