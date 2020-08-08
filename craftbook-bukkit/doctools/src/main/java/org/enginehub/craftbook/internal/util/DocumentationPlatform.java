package org.enginehub.craftbook.internal.util;

import org.enginehub.craftbook.bukkit.BukkitCraftBookPlatform;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DocumentationPlatform extends BukkitCraftBookPlatform {

    @Override
    public Path getConfigDir() {
        return Paths.get("docgen_output");
    }

    @Override
    public boolean isPluginAvailable(String pluginName) {
        return true;
    }
}
