package com.sk89q.craftbook.mech.crafting;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.craftbook.mech.crafting.RecipeManager.Recipe;
import com.sk89q.craftbook.mech.crafting.RecipeManager.Recipe.RecipeType;

public class CustomCrafting {

    protected final RecipeManager recipes;
    protected final MechanismsPlugin plugin;
    HashMap<Integer, Character> numberLetter = new HashMap<Integer, Character>();

    public CustomCrafting(MechanismsPlugin plugin) {

        numberLetter.put(1, 'a');
        numberLetter.put(2, 'b');
        numberLetter.put(3, 'c');
        numberLetter.put(4, 'd');
        numberLetter.put(5, 'e');
        numberLetter.put(6, 'f');
        numberLetter.put(7, 'g');
        numberLetter.put(8, 'h');
        numberLetter.put(9, 'i');

        this.plugin = plugin;
        recipes = new RecipeManager(YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(),
                "crafting-recipes.yml")), plugin.getDataFolder());
        Collection<Recipe> recipeCollection = recipes.getRecipes();
        for(Recipe r : recipeCollection) {
            if(r.getType() == RecipeType.SHAPELESS) {
                ShapelessRecipe sh = new ShapelessRecipe(r.getResult().getItemStack());
                for(CraftingItemStack is : r.getIngredients()) {
                    sh.addIngredient(is.getMaterial(), is.getData());
                }
                plugin.getServer().addRecipe(sh);
            }
            else if(r.getType() == RecipeType.SHAPED2X2) {
                ShapedRecipe sh = new ShapedRecipe(r.getResult().getItemStack());
                sh.shape("ab","cd");
                int ingredientNum = 0;
                for(CraftingItemStack is : r.getIngredients()) {
                    ingredientNum++;
                    sh.setIngredient(numberLetter.get(ingredientNum), is.getMaterial(), is.getData());
                }
                plugin.getServer().addRecipe(sh);
            }
            else if(r.getType() == RecipeType.SHAPED3X3) {
                ShapedRecipe sh = new ShapedRecipe(r.getResult().getItemStack());
                sh.shape("abc","def","ghi");
                int ingredientNum = 0;
                for(CraftingItemStack is : r.getIngredients()) {
                    ingredientNum++;
                    sh.setIngredient(numberLetter.get(ingredientNum), is.getMaterial(), is.getData());
                }
                plugin.getServer().addRecipe(sh);
            }
            else if(r.getType() == RecipeType.FURNACE) {
                FurnaceRecipe sh = new FurnaceRecipe(r.getResult().getItemStack(), r.getResult().getMaterial());
                plugin.getServer().addRecipe(sh);
            }
        }
    }
}