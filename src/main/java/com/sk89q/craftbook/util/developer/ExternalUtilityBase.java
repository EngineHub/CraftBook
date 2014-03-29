package com.sk89q.craftbook.util.developer;

import java.io.File;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;

public abstract class ExternalUtilityBase {

    public ExternalUtilityBase(String[] args) {
        generate(args);
    }

    public File getGenerationFolder() {
        File genFile = new File(CraftBookPlugin.inst().getDataFolder(), "developer");
        genFile.mkdirs();
        return genFile;
    }

    public abstract void generate(String[] args);
}