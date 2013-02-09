package com.sk89q.craftbook.mech.crafting;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.ItemUtil;
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

    public static HashMap<Recipe, RecipeManager.Recipe> advancedRecipes = new HashMap<Recipe, RecipeManager.Recipe>();

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
                } else if (r.getType() == RecipeManager.Recipe.RecipeType.SHAPED2X2 || r.getType() == RecipeManager.Recipe.RecipeType.SHAPED3X3 || r.getType() == RecipeManager.Recipe.RecipeType.SHAPED) {
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

        for(Recipe rec : advancedRecipes.keySet()) {

            if(checkRecipes(rec, event.getRecipe())) {
                event.setCurrentItem(applyAdvancedEffects(event.getCurrentItem(),rec));
                break;
            }
        }
    }

    public static ItemStack applyAdvancedEffects(ItemStack stack, Recipe rep) {
        RecipeManager.Recipe recipe = advancedRecipes.get(rep);
        ItemStack res = stack.clone();
        if(recipe.getResult().hasAdvancedData("name")) {
            ItemMeta meta = res.getItemMeta();
            meta.setDisplayName(ChatColor.RESET + (String) recipe.getResult().getAdvancedData("name"));
            res.setItemMeta(meta);
        }
        return res;
    }

    public static boolean checkRecipes(Recipe rec1, Recipe rec2) {

        if(ItemUtil.areItemsIdentical(rec1.getResult(), rec2.getResult())) {
            if(rec1 instanceof ShapedRecipe && rec2 instanceof ShapedRecipe || rec1 instanceof ShapelessRecipe && rec2 instanceof ShapelessRecipe) {
                if(rec1 instanceof ShapedRecipe && rec2 instanceof ShapedRecipe) {
                    if(((ShapedRecipe) rec1).getShape().length != ((ShapedRecipe) rec2).getShape().length)
                        return false;
                }
                else if(rec1 instanceof ShapelessRecipe && rec2 instanceof ShapelessRecipe) {
                    if(((ShapelessRecipe) rec1).getIngredientList().size() != ((ShapelessRecipe) rec2).getIngredientList().size())
                        return false;

                    List<ItemStack> test = new ArrayList<ItemStack>();
                    test.addAll(((ShapelessRecipe) rec1).getIngredientList());
                    if(!test.removeAll(((ShapelessRecipe) rec2).getIngredientList()))
                        return false;
                    if(test.size() > 0)
                        return false;
                }

                return true;
            }
        }

        return false;
    }
}