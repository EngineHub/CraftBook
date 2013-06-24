package com.sk89q.craftbook.mech.crafting;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachment;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.mech.crafting.RecipeManager.RecipeType;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.ParsingUtil;
import com.sk89q.craftbook.util.VerifyUtil;
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

    public static final HashMap<Recipe, RecipeManager.Recipe> advancedRecipes = new HashMap<Recipe, RecipeManager.Recipe>();

    public CustomCrafting() {

        plugin.createDefaultConfiguration(new File(plugin.getDataFolder(), "crafting-recipes.yml"), "crafting-recipes.yml", false);
        recipes = new RecipeManager(new YAMLProcessor(new File(plugin.getDataFolder(), "crafting-recipes.yml"), true, YAMLFormat.EXTENDED));
        Collection<RecipeManager.Recipe> recipeCollection = recipes.getRecipes();
        int recipes = 0;
        for (RecipeManager.Recipe r : recipeCollection) {
            if(addRecipe(r))
                recipes++;
        }
        plugin.getLogger().info("Registered " + recipes + " custom recipes!");
    }

    /**
     * Adds a recipe to the manager.
     */
    public boolean addRecipe(RecipeManager.Recipe r) {
        try {
            if (r.getType() == RecipeManager.RecipeType.SHAPELESS) {
                ShapelessRecipe sh = new ShapelessRecipe(r.getResult().getItemStack());
                for (CraftingItemStack is : r.getIngredients()) {
                    sh.addIngredient(is.getItemStack().getAmount(), is.getItemStack().getType(), is.getItemStack().getData().getData());
                }
                plugin.getServer().addRecipe(sh);
                if(r.hasAdvancedData()) {
                    advancedRecipes.put(sh, r);
                    CraftBookPlugin.logDebugMessage("Adding a new recipe with advanced data!", "advanced-data.init");
                }
            } else if (r.getType() == RecipeManager.RecipeType.SHAPED) {
                ShapedRecipe sh = new ShapedRecipe(r.getResult().getItemStack());
                sh.shape(r.getShape());
                for (Entry<CraftingItemStack, Character> is : r.getShapedIngredients().entrySet()) {
                    sh.setIngredient(is.getValue().charValue(), is.getKey().getItemStack().getType(), is.getKey().getItemStack().getData().getData());
                }
                plugin.getServer().addRecipe(sh);
                if(r.hasAdvancedData()) {
                    advancedRecipes.put(sh, r);
                    CraftBookPlugin.logDebugMessage("Adding a new recipe with advanced data!", "advanced-data.init");
                }
            } else if (r.getType() == RecipeManager.RecipeType.FURNACE) {
                FurnaceRecipe sh = new FurnaceRecipe(r.getResult().getItemStack(), r.getIngredients().toArray(new CraftingItemStack[r.getIngredients().size()])[0].getItemStack().getType());
                for (CraftingItemStack is : r.getIngredients()) {
                    sh.setInput(is.getItemStack().getType(), is.getItemStack().getData().getData());
                }
                plugin.getServer().addRecipe(sh);
                if(r.hasAdvancedData()) {
                    advancedRecipes.put(sh, r);
                    CraftBookPlugin.logDebugMessage("Adding a new recipe with advanced data!", "advanced-data.init");
                }
            } else {
                return false;
            }
            plugin.getLogger().info("Registered a new " + r.getType().toString().toLowerCase(Locale.ENGLISH) + " recipe!");

            return true;
        } catch (IllegalArgumentException e) {
            plugin.getLogger().severe("Corrupt or invalid recipe!");
            plugin.getLogger().severe("Please either delete custom-crafting.yml, or fix the issues with your recipes file!");
            BukkitUtil.printStacktrace(e);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load recipe! Is it incorrectly written?");
            BukkitUtil.printStacktrace(e);
        }

        return false;
    }

    @EventHandler
    public void prepareCraft(PrepareItemCraftEvent event) {

        ItemStack bits = null;
        CraftBookPlugin.logDebugMessage("Pre-Crafting has been initiated!", "advanced-data");
        try {
            boolean hasFailed = false;
            for(Recipe rec : advancedRecipes.keySet()) {

                if(checkRecipes(rec, event.getRecipe())) {

                    thisrecipe: {
                    RecipeManager.Recipe recipe = advancedRecipes.get(rec);

                    ItemStack[] tests = ((CraftingInventory)event.getView().getTopInventory()).getMatrix();
                    CraftingItemStack[] tests2;
                    if(recipe.getType() == RecipeType.SHAPED) {
                        List<CraftingItemStack> stacks = new ArrayList<CraftingItemStack>();

                        for(String s : recipe.getShape())
                            for(char c : s.toCharArray())
                                for(Entry<CraftingItemStack, Character> entry : recipe.getShapedIngredients().entrySet())
                                    if(entry.getValue().charValue() == c)
                                        stacks.add(entry.getKey());
                        tests2 = stacks.toArray(new CraftingItemStack[stacks.size()]);
                    } else
                        tests2 = recipe.getIngredients().toArray(new CraftingItemStack[recipe.getIngredients().size()]);

                    ArrayList<ItemStack> leftovers = new ArrayList<ItemStack>();
                    leftovers.addAll(Arrays.asList(tests));
                    leftovers.removeAll(Collections.singleton(null));

                    for(ItemStack it : tests) {

                        if(!ItemUtil.isStackValid(it))
                            continue;
                        for(CraftingItemStack cit : tests2) {

                            if(ItemUtil.areBaseItemsIdentical(cit.getItemStack(), it)) {
                                CraftBookPlugin.logDebugMessage("Recipe base item is correct!", "advanced-data");
                                if(ItemUtil.areItemsIdentical(cit.getItemStack(), it)) {
                                    leftovers.remove(it);
                                    CraftBookPlugin.logDebugMessage("MetaData is correct!", "advanced-data");
                                } else {
                                    CraftBookPlugin.logDebugMessage("MetaData is incorrect!", "advanced-data");
                                    hasFailed = true;
                                    break thisrecipe;
                                }
                            } else
                                continue;
                        }
                    }

                    if(!leftovers.isEmpty())
                        continue;

                    hasFailed = false;

                    CraftBookPlugin.logDebugMessage("A recipe with custom data is being crafted!", "advanced-data");
                    bits = applyAdvancedEffects(event.getRecipe().getResult(),rec, null);
                    break;
                }
                }
            }
            if(hasFailed)
                throw new InvalidCraftingException("Unmet Item Meta");
        } catch(InvalidCraftingException e){
            ((CraftingInventory)event.getView().getTopInventory()).setResult(null);
            return;
        } catch (Exception e) {
            BukkitUtil.printStacktrace(e);
            ((CraftingInventory)event.getView().getTopInventory()).setResult(null);
            return;
        }
        if(bits != null && !bits.equals(event.getRecipe().getResult())) {
            bits.setAmount(((CraftingInventory)event.getView().getTopInventory()).getResult().getAmount());
            ((CraftingInventory)event.getView().getTopInventory()).setResult(bits);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void prepareFurnace(FurnaceSmeltEvent event) {

        ItemStack bits = null;
        CraftBookPlugin.logDebugMessage("Smelting has been initiated!", "advanced-data");
        for(Recipe rec : advancedRecipes.keySet()) {

            if(!(rec instanceof FurnaceRecipe))
                continue;
            try {
                if(checkFurnaceRecipes((FurnaceRecipe) rec, event.getSource(), event.getResult())) {

                    RecipeManager.Recipe recipe = advancedRecipes.get(rec);

                    ArrayList<ItemStack> leftovers = new ArrayList<ItemStack>();
                    leftovers.add(event.getSource());
                    leftovers.removeAll(Collections.singleton(null));

                    if(!ItemUtil.isStackValid(event.getSource()))
                        continue;
                    for(CraftingItemStack cit : recipe.getIngredients()) {

                        if(ItemUtil.areBaseItemsIdentical(cit.getItemStack(), event.getSource())) {
                            CraftBookPlugin.logDebugMessage("Base item is correct!", "advanced-data");
                            if(ItemUtil.areItemsIdentical(cit.getItemStack(), event.getSource())) {
                                leftovers.remove(event.getSource());
                                CraftBookPlugin.logDebugMessage("MetaData correct!", "advanced-data");
                            } else {
                                CraftBookPlugin.logDebugMessage("MetaData incorrect!", "advanced-data");
                                throw new InvalidCraftingException("Unmet Item Meta");
                            }
                        } else
                            continue;
                    }

                    if(!leftovers.isEmpty())
                        continue;

                    CraftBookPlugin.logDebugMessage("A recipe with custom data is being smelted!", "advanced-data");
                    bits = applyAdvancedEffects(event.getResult(),rec, null);
                    break;
                }
            } catch(InvalidCraftingException e){
                event.setResult(null);
                event.setCancelled(true);
                return;
            }
        }
        if(bits != null && !bits.equals(event.getResult())) {
            bits.setAmount(event.getResult().getAmount());
            event.setResult(bits);
        }
    }

    @SuppressWarnings("unchecked")
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onCraft(CraftItemEvent event) {

        CraftBookPlugin.logDebugMessage("Crafting has been initiated!", "advanced-data");
        Player p = (Player) event.getWhoClicked();
        for(Recipe rec : advancedRecipes.keySet()) {

            try {
                if(checkRecipes(rec, event.getRecipe())) {
                    CraftBookPlugin.logDebugMessage("A recipe with custom data is being crafted!", "advanced-data");
                    RecipeManager.Recipe recipe = advancedRecipes.get(rec);
                    if(recipe.hasAdvancedData("permission-node")) {
                        CraftBookPlugin.logDebugMessage("A recipe with permission nodes detected!", "advanced-data");
                        if(!event.getWhoClicked().hasPermission((String) recipe.getAdvancedData("permission-node"))) {
                            p.sendMessage(ChatColor.RED + "You do not have permission to craft this recipe!");
                            event.setCancelled(true);
                            return;
                        }
                    }
                    if(recipe.hasAdvancedData("extra-results")) {
                        CraftBookPlugin.logDebugMessage("A recipe with extra results is detected!", "advanced-data");
                        ArrayList<CraftingItemStack> stacks = new ArrayList<CraftingItemStack>((Collection<CraftingItemStack>) recipe.getAdvancedData("extra-results"));
                        for(CraftingItemStack stack : stacks) {
                            if(stack.hasAdvancedData("chance"))
                                if(CraftBookPlugin.inst().getRandom().nextDouble() < (Double)stack.getAdvancedData("chance"))
                                    continue;
                            HashMap<Integer, ItemStack> leftovers = event.getWhoClicked().getInventory().addItem(stack.getItemStack());
                            if(!leftovers.isEmpty()) {
                                for(ItemStack istack : leftovers.values())
                                    event.getWhoClicked().getWorld().dropItemNaturally(event.getWhoClicked().getLocation(), istack);
                            }
                        }
                    }
                    if(recipe.hasAdvancedData("commands-player") || recipe.hasAdvancedData("commands-console")) {
                        CraftBookPlugin.logDebugMessage("A recipe with commands is detected!", "advanced-data");
                        if(recipe.hasAdvancedData("commands-console")) {
                            for(String s : (List<String>)recipe.getAdvancedData("commands-console")) {
                                s = ParsingUtil.parseLine(s, p);
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s);
                            }
                        }
                        if(recipe.hasAdvancedData("commands-player")) {
                            for(String s : (List<String>)recipe.getAdvancedData("commands-player")) {
                                s = ParsingUtil.parseLine(s, p);
                                PermissionAttachment att = p.addAttachment(CraftBookPlugin.inst());
                                att.setPermission("*", true);
                                boolean wasOp = p.isOp();
                                p.setOp(true);
                                Bukkit.dispatchCommand(p, s);
                                att.remove();
                                p.setOp(wasOp);
                            }
                        }
                    }

                    event.setCurrentItem(applyAdvancedEffects(event.getCurrentItem(), event.getRecipe(), (Player) event.getWhoClicked()));
                    break;
                }
            } catch(InvalidCraftingException e){
                event.setCancelled(true);
            }
        }
    }

    public static ItemStack craftItem(Recipe recipe) {

        for(Recipe rec : advancedRecipes.keySet()) {
            try {
                if(checkRecipes(rec, recipe))
                    return applyAdvancedEffects(recipe.getResult(),rec, null);
            } catch (InvalidCraftingException e){
                return null; //Invalid Recipe.
            }
        }

        return recipe.getResult();
    }

    @SuppressWarnings("unchecked")
    private static ItemStack applyAdvancedEffects(ItemStack stack, Recipe rep, Player player) {

        RecipeManager.Recipe recipe = advancedRecipes.get(rep);

        if(recipe == null)
            return stack;

        ItemStack res = stack.clone();
        if(recipe.getResult().hasAdvancedData("name")) {
            ItemMeta meta = res.getItemMeta();
            meta.setDisplayName(ChatColor.RESET + ParsingUtil.parseLine((String) recipe.getResult().getAdvancedData("name"), player));
            res.setItemMeta(meta);
        }
        if(recipe.getResult().hasAdvancedData("lore")) {
            ItemMeta meta = res.getItemMeta();
            List<String> lore = new ArrayList<String>();
            for(String s : (List<String>) recipe.getResult().getAdvancedData("lore"))
                lore.add(ParsingUtil.parseLine(s, player));
            meta.setLore(lore);
            res.setItemMeta(meta);
        }
        if(recipe.getResult().hasAdvancedData("enchants")) {
            for(Entry<Enchantment,Integer> enchants : ((Map<Enchantment,Integer>)recipe.getResult().getAdvancedData("enchants")).entrySet())
                res.addUnsafeEnchantment(enchants.getKey(), enchants.getValue());
        }
        return res;
    }

    private static boolean checkFurnaceRecipes(FurnaceRecipe rec1, ItemStack source, ItemStack result) throws InvalidCraftingException {

        if(ItemUtil.areItemsIdentical(rec1.getInput(), source))
            if(ItemUtil.areItemsIdentical(rec1.getResult(), result))
                return true;

        return false;
    }

    private static boolean checkRecipes(Recipe rec1, Recipe rec2) throws InvalidCraftingException {

        if(ItemUtil.areItemsIdentical(rec1.getResult(), rec2.getResult())) {
            CraftBookPlugin.logDebugMessage("Recipes have same results!", "advanced-data.compare-recipes");
            if(rec1 instanceof ShapedRecipe && rec2 instanceof ShapedRecipe) {
                CraftBookPlugin.logDebugMessage("Shaped recipe!", "advanced-data.compare-recipes.shaped");
                ShapedRecipe recipe1 = (ShapedRecipe) rec1;
                ShapedRecipe recipe2 = (ShapedRecipe) rec2;
                if(recipe1.getShape().length == recipe2.getShape().length) {
                    CraftBookPlugin.logDebugMessage("Same size!", "advanced-data.compare-recipes.shaped");
                    List<ItemStack> stacks1 = new ArrayList<ItemStack>();

                    for(String s : recipe1.getShape())
                        for(char c : s.toCharArray())
                            for(Entry<Character, ItemStack> entry : recipe1.getIngredientMap().entrySet())
                                if(entry.getKey().charValue() == c)
                                    stacks1.add(entry.getValue());
                    List<ItemStack> stacks2 = new ArrayList<ItemStack>();

                    for(String s : recipe2.getShape())
                        for(char c : s.toCharArray())
                            for(Entry<Character, ItemStack> entry : recipe2.getIngredientMap().entrySet())
                                if(entry.getKey().charValue() == c)
                                    stacks2.add(entry.getValue());

                    if(stacks2.size() != stacks1.size()) {
                        CraftBookPlugin.logDebugMessage("Recipes have different amounts of ingredients!", "advanced-data.compare-recipes.shaped");
                        return false;
                    }
                    List<ItemStack> test = new ArrayList<ItemStack>();
                    test.addAll(stacks1);
                    if(test.size() == 0) {
                        CraftBookPlugin.logDebugMessage("Recipes are the same!", "advanced-data.compare-recipes.shaped");
                        return true;
                    }
                    if(!test.removeAll(stacks2) && test.size() > 0) {
                        CraftBookPlugin.logDebugMessage("Recipes are NOT the same!", "advanced-data.compare-recipes.shaped");
                        return false;
                    }
                    if(test.size() > 0) {
                        CraftBookPlugin.logDebugMessage("Recipes are NOT the same!", "advanced-data.compare-recipes.shaped");
                        return false;
                    }
                }
            } else if(rec1 instanceof ShapelessRecipe && rec2 instanceof ShapelessRecipe) {

                CraftBookPlugin.logDebugMessage("Shapeless Recipe!", "advanced-data.compare-recipes.shapeless");
                ShapelessRecipe recipe1 = (ShapelessRecipe) rec1;
                ShapelessRecipe recipe2 = (ShapelessRecipe) rec2;

                if(VerifyUtil.withoutNulls(recipe1.getIngredientList()).size() != VerifyUtil.withoutNulls(recipe2.getIngredientList()).size()) {
                    CraftBookPlugin.logDebugMessage("Recipes have different amounts of ingredients!", "advanced-data.compare-recipes.shapeless");
                    return false;
                }

                CraftBookPlugin.logDebugMessage("Same Size!", "advanced-data.compare-recipes.shapeless");

                List<ItemStack> test = new ArrayList<ItemStack>();
                test.addAll(VerifyUtil.<ItemStack>withoutNulls(recipe1.getIngredientList()));
                if(test.size() == 0) {
                    CraftBookPlugin.logDebugMessage("Recipes are the same!", "advanced-data.compare-recipes.shapeless");
                    return true;
                }
                if(!test.removeAll(VerifyUtil.<ItemStack>withoutNulls(recipe2.getIngredientList())) && test.size() > 0) {
                    CraftBookPlugin.logDebugMessage("Recipes are NOT the same!", "advanced-data.compare-recipes.shapeless");
                    return false;
                }
                if(test.size() > 0) {
                    CraftBookPlugin.logDebugMessage("Recipes are NOT the same!", "advanced-data.compare-recipes.shapeless");
                    return false;
                }
            }

            CraftBookPlugin.logDebugMessage("Recipes are the same!", "advanced-data.compare-recipes");

            return true;
        }

        return false;
    }
}