package com.sk89q.craftbook.mech.crafting;

import java.io.File;

import org.bukkit.configuration.file.YamlConfiguration;

import com.sk89q.craftbook.bukkit.MechanismsPlugin;

public class CustomCrafting {

    protected final RecipeManager recipes;
    protected final MechanismsPlugin plugin;

    public CustomCrafting(MechanismsPlugin plugin) {

	this.plugin = plugin;
	recipes = new RecipeManager(YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "crafting-recipes.yml")), plugin.getDataFolder());
    }
}