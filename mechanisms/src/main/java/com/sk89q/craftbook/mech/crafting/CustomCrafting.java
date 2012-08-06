package com.sk89q.craftbook.mech.crafting;

import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class CustomCrafting {

    protected final RecipeManager recipes;
    protected final MechanismsPlugin plugin;

    public CustomCrafting(MechanismsPlugin plugin) {

        this.plugin = plugin;
        recipes = new RecipeManager(YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(),
                "crafting-recipes.yml")), plugin.getDataFolder());
    }
}