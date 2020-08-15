/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
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

package org.enginehub.craftbook;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.function.Supplier;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.annotation.Nullable;

/**
 * Represents CraftBook info from the MANIFEST.MF file.
 */
public class CraftBookManifest {

    public static final String CRAFT_BOOK_VERSION = "CraftBook-Version";

    public static CraftBookManifest load() {
        Attributes attributes = readAttributes();
        return new CraftBookManifest(
            readAttribute(attributes, CRAFT_BOOK_VERSION, () -> "(unknown)")
        );
    }

    private static @Nullable
    Attributes readAttributes() {
        Class<CraftBookManifest> clazz = CraftBookManifest.class;
        String className = clazz.getSimpleName() + ".class";
        String classPath = clazz.getResource(className).toString();
        if (!classPath.startsWith("jar")) {
            return null;
        }

        try {
            URL url = new URL(classPath);
            JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
            Manifest manifest = jarConnection.getManifest();
            return manifest.getMainAttributes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String readAttribute(@Nullable Attributes attributes, String name,
                                        Supplier<String> defaultAction) {
        if (attributes == null) {
            return defaultAction.get();
        }
        String value = attributes.getValue(name);
        return value != null ? value : defaultAction.get();
    }

    private final String craftBookVersion;

    private CraftBookManifest(String craftBookVersion) {
        this.craftBookVersion = craftBookVersion;
    }

    public String getCraftBookVersion() {
        return craftBookVersion;
    }
}
