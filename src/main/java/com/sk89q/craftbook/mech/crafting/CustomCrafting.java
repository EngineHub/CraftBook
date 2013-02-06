package com.sk89q.craftbook.mech.crafting;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;

/**
 * Custom Crafting Recipe Handler
 *
 * @author Me4502
 */
public class CustomCrafting implements Listener {

    protected final RecipeManager recipes;
    protected final CraftBookPlugin plugin = CraftBookPlugin.inst();

    private HashMap<Recipe, RecipeManager.Recipe> advancedRecipes = new HashMap<Recipe, RecipeManager.Recipe>();

    public CustomCrafting() {

        plugin.createDefaultConfiguration(new File(plugin.getDataFolder(), "crafting-recipes.yml"), "crafting-recipes.yml", false);
        recipes = new RecipeManager(new YAMLProcessor(new File(plugin.getDataFolder(), "crafting-recipes.yml"), true, YAMLFormat.EXTENDED), plugin.getLogger());
        Collection<RecipeManager.Recipe> recipeCollection = recipes.getRecipes();
        int recipes = 0;
        for (RecipeManager.Recipe r : recipeCollection) {
            try {
                if (r.getType() == RecipeManager.Recipe.RecipeType.SHAPELESS) {
                    ShapelessRecipe sh = new ShapelessRecipe(r.getResult().getItemStack());
                    for (CraftingItemStack is : r.getIngredients()) {
                        sh.addIngredient(is.getAmount(), is.getMaterial(), is.getData());
                    }
                    plugin.getServer().addRecipe(sh);
                    if(r.hasAdvancedData())
                        advancedRecipes.put(sh, r);
                } else if (r.getType() == RecipeManager.Recipe.RecipeType.SHAPED2X2 || r.getType() == RecipeManager.Recipe.RecipeType.SHAPED3X3) {
                    ShapedRecipe sh = new ShapedRecipe(r.getResult().getItemStack());
                    sh.shape(r.getShape());
                    for (Entry<CraftingItemStack, Character> is : r.getShapedIngredients().entrySet()) {
                        sh.setIngredient(is.getValue().charValue(), is.getKey().getMaterial(), is.getKey().getData());
                    }
                    plugin.getServer().addRecipe(sh);
                    if(r.hasAdvancedData())
                        advancedRecipes.put(sh, r);
                } else if (r.getType() == RecipeManager.Recipe.RecipeType.FURNACE) {
                    FurnaceRecipe sh = new FurnaceRecipe(r.getResult().getItemStack(), r.getResult().getMaterial());
                    for (CraftingItemStack is : r.getIngredients()) {
                        sh.setInput(is.getMaterial(), is.getData());
                    }
                    plugin.getServer().addRecipe(sh);
                    if(r.hasAdvancedData())
                        advancedRecipes.put(sh, r);
                } else {
                    continue;
                }
                plugin.getLogger().info("Registered a new " + r.getType().toString().toLowerCase() + " recipe!");

                recipes++;
            } catch (IllegalArgumentException e) {
                plugin.getLogger().severe("Corrupt or invalid recipe!");
                plugin.getLogger().severe("Please either delete custom-crafting.yml, " +
                        "" + "or fix the issues with your recipes file!");
                BukkitUtil.printStacktrace(e);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load recipe!");
                BukkitUtil.printStacktrace(e);
            }
        }
        plugin.getLogger().info("Registered " + recipes + " custom recipes!");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCraft(CraftItemEvent event) {

        if(advancedRecipes.containsKey(event.getRecipe())) {

            RecipeManager.Recipe recipe = advancedRecipes.get(event.getRecipe());
            if(recipe.getResult().hasAdvancedData("name"))
                event.getCurrentItem().getItemMeta().setDisplayName((String) recipe.getResult().getAdvancedData("name"));
        }
    }
}