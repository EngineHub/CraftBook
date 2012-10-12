package com.sk89q.craftbook.mech.crafting;

import java.io.File;
import java.util.Collection;
import java.util.Map.Entry;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.craftbook.mech.crafting.RecipeManager.Recipe;
import com.sk89q.craftbook.mech.crafting.RecipeManager.Recipe.RecipeType;
import com.sk89q.craftbook.util.GeneralUtil;

/**
 * 
 * Custom Crafting Recipe Handler
 * 
 * @author Me4502
 *
 */
public class CustomCrafting {

    protected final RecipeManager recipes;
    protected final MechanismsPlugin plugin;

    public CustomCrafting(MechanismsPlugin plugin) {

        this.plugin = plugin;
        recipes = new RecipeManager(YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(),
                "crafting-recipes.yml")), plugin.getDataFolder());
        Collection<Recipe> recipeCollection = recipes.getRecipes();
        for (Recipe r : recipeCollection)
            try {
                if (r.getType() == RecipeType.SHAPELESS) {
                    ShapelessRecipe sh = new ShapelessRecipe(r.getResult().getItemStack());
                    for (CraftingItemStack is : r.getIngredients())
                        sh.addIngredient(is.getAmount(), is.getMaterial(), is.getData());
                    plugin.getServer().addRecipe(sh);
                } else if (r.getType() == RecipeType.SHAPED2X2) {
                    ShapedRecipe sh = new ShapedRecipe(r.getResult().getItemStack());
                    sh.shape(r.getShape());
                    for (Entry<CraftingItemStack, Character> is : r.getShapedIngredients().entrySet())
                        sh.setIngredient(is.getValue(), is.getKey().getMaterial(), is.getKey().getData());
                    plugin.getServer().addRecipe(sh);
                } else if (r.getType() == RecipeType.SHAPED3X3) {
                    ShapedRecipe sh = new ShapedRecipe(r.getResult().getItemStack());
                    sh.shape(r.getShape());
                    for (Entry<CraftingItemStack, Character> is : r.getShapedIngredients().entrySet())
                        sh.setIngredient(is.getValue(), is.getKey().getMaterial(), is.getKey().getData());
                    plugin.getServer().addRecipe(sh);
                } else if (r.getType() == RecipeType.FURNACE) {
                    FurnaceRecipe sh = new FurnaceRecipe(r.getResult().getItemStack(), r.getResult().getMaterial());
                    for (CraftingItemStack is : r.getIngredients())
                        sh.setInput(is.getMaterial(), is.getData());
                    plugin.getServer().addRecipe(sh);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().severe("Corrupt or invalid recipe!");
                plugin.getLogger().severe("Please either delete custom-crafting.yml, or fix the issues with your recipes file!");
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load recipe!");
                plugin.getLogger().severe(GeneralUtil.getStackTrace(e));
            }
    }
}