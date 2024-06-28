/*
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

package org.enginehub.craftbook.mechanics.crafting;

import com.google.common.base.MoreObjects;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.permissions.PermissionAttachment;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicCommandRegistrar;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.mechanics.crafting.RecipeManager.RecipeType;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.ItemUtil;
import org.enginehub.craftbook.util.ParsingUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Custom Crafting Recipe Handler
 *
 * @author Me4502
 */
public class CustomCrafting extends AbstractCraftBookMechanic {

    public static CustomCrafting INSTANCE;
    private RecipeManager manager;

    public static final Set<String> registeredNames = new HashSet<>();

    private static final Map<Recipe, RecipeManager.Recipe> advancedRecipes = new HashMap<>();

    public CustomCrafting(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @Override
    public void enable() {
        INSTANCE = this;

        MechanicCommandRegistrar registrar = CraftBookPlugin.inst().getCommandManager().getMechanicRegistrar();
        registrar.registerTopLevelWithSubCommands(
            "recp",
            List.of("recps"),
            "CraftBook Custom Crafting Commands",
            RecipeCommands::register
        );

        CraftBookPlugin.inst().createDefaultConfiguration("crafting-recipes.yml");
        manager = new RecipeManager(new YAMLProcessor(new File(CraftBookPlugin.inst().getDataFolder(), "crafting-recipes.yml"), true, YAMLFormat.EXTENDED));
        Collection<RecipeManager.Recipe> recipeCollection = manager.getRecipes();
        int recipes = 0;
        for (RecipeManager.Recipe r : recipeCollection) {
            if (addRecipe(r)) {
                recipes++;
            }
        }
        CraftBook.LOGGER.info("Registered " + recipes + " custom recipes!");
    }

    @Override
    public void disable() {
        advancedRecipes.clear();
        manager.disable();
        manager = null;

        MechanicCommandRegistrar registrar = CraftBookPlugin.inst().getCommandManager().getMechanicRegistrar();
        registrar.unregisterTopLevel("recp");
        registrar.unregisterTopLevel("recps");

        INSTANCE = null;
    }

    /**
     * Adds a recipe to the manager.
     */
    public boolean addRecipe(RecipeManager.Recipe r) {
        if (registeredNames.contains(r.getId())) {
            CraftBook.LOGGER.warn("A recipe with name " + r.getId() + " has already been registered by CraftBook. Due to a "
                + "limitation in Bukkit-derivitive servers, this can't be registered again without a restart.");
            return false;
        }

        try {
            Recipe sh;

            if (r.getType() == RecipeManager.RecipeType.SHAPELESS) {
                sh = new ShapelessRecipe(new NamespacedKey(CraftBookPlugin.inst(), r.getId()), r.getResult().getItemStack());
                for (CraftingItemStack is : r.getIngredients())
                    ((ShapelessRecipe) sh).addIngredient(is.getItemStack().getAmount(), is.getItemStack().getType());
            } else if (r.getType() == RecipeManager.RecipeType.SHAPED) {
                sh = new ShapedRecipe(new NamespacedKey(CraftBookPlugin.inst(), r.getId()), r.getResult().getItemStack());
                ((ShapedRecipe) sh).shape(r.getShape());
                for (Entry<CraftingItemStack, Character> is : r.getShapedIngredients().entrySet())
                    ((ShapedRecipe) sh).setIngredient(is.getValue(), is.getKey().getItemStack().getType());
            } else if (r.getType() == RecipeManager.RecipeType.FURNACE) {
                if (r.getIngredients().isEmpty()) {
                    CraftBookPlugin.inst().getLogger().warning("Recipe " + r.getId() + " is missing ingredients");
                    return false;
                }
                sh = new FurnaceRecipe(
                    new NamespacedKey(CraftBookPlugin.inst(), r.getId()),
                    r.getResult().getItemStack(),
                    r.getIngredients().get(0).getItemStack().getType(),
                    r.getExperience(),
                    r.getCookTime()
                );
                for (CraftingItemStack is : r.getIngredients())
                    ((FurnaceRecipe) sh).setInput(is.getItemStack().getType());
            } else
                return false;

            CraftBookPlugin.inst().getServer().addRecipe(sh);
            registeredNames.add(r.getId());
            if (r.hasAdvancedData()) {
                advancedRecipes.put(sh, r);
                CraftBookPlugin.logDebugMessage("Adding a new recipe with advanced data!", "advanced-data.init");
            }

            return true;
        } catch (IllegalArgumentException e) {
            CraftBook.LOGGER.error("Corrupt or invalid recipe!");
            CraftBook.LOGGER.error("Please either delete custom-crafting.yml, or fix the issues with your recipes file!");
            e.printStackTrace();
        } catch (Exception e) {
            CraftBook.LOGGER.error("Failed to load recipe! Is it incorrectly written?", e);
        }

        return false;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void prepareCraft(PrepareItemCraftEvent event) {
        if (!EventUtil.passesFilter(event)) return;

        ItemStack bits = null;
        Player p = null;
        CraftBookPlayer lp = null;
        try {
            p = (Player) event.getViewers().get(0);
            lp = CraftBookPlugin.inst().wrapPlayer(p);
        } catch (Exception e) {
        }
        CraftBookPlugin.logDebugMessage("Pre-Crafting has been initiated!", "advanced-data");
        try {
            boolean hasFailed = false;
            for (Entry<Recipe, RecipeManager.Recipe> recipeRecipeEntry : advancedRecipes.entrySet()) {

                if (ItemUtil.areRecipesIdentical(recipeRecipeEntry.getKey(), event.getRecipe())) {

                    thisrecipe:
                    {
                        RecipeManager.Recipe recipe = recipeRecipeEntry.getValue();

                        ItemStack[] tests = ((CraftingInventory) event.getView().getTopInventory()).getMatrix();
                        CraftingItemStack[] tests2;
                        if (recipe.getType() == RecipeType.SHAPED) {
                            List<CraftingItemStack> stacks = new ArrayList<>();

                            for (String s : recipe.getShape())
                                for (char c : s.toCharArray())
                                    for (Entry<CraftingItemStack, Character> entry : recipe.getShapedIngredients().entrySet())
                                        if (entry.getValue() == c)
                                            stacks.add(entry.getKey());
                            tests2 = stacks.toArray(new CraftingItemStack[0]);
                        } else
                            tests2 = recipe.getIngredients().toArray(new CraftingItemStack[0]);

                        ArrayList<ItemStack> leftovers = new ArrayList<>(Arrays.asList(tests));
                        leftovers.removeAll(Collections.singleton(null));

                        for (ItemStack it : tests) {

                            if (!ItemUtil.isStackValid(it)) {
                                if (it != null) {
                                    CraftBookPlugin.logDebugMessage("Invalid item in recipe: " +
                                        MoreObjects.toStringHelper(it).toString(), "advanced-data");
                                }
                                continue;
                            }
                            for (CraftingItemStack cit : tests2) {

                                if (ItemUtil.areBaseItemsIdentical(cit.getItemStack(), it)) {
                                    CraftBookPlugin.logDebugMessage("Recipe base item is correct!", "advanced-data");
                                    if (ItemUtil.areItemsIdentical(cit.getItemStack(), it)) {
                                        leftovers.remove(it);
                                        CraftBookPlugin.logDebugMessage("MetaData is correct!", "advanced-data");
                                    } else {
                                        CraftBookPlugin.logDebugMessage("MetaData is incorrect!", "advanced-data");
                                        hasFailed = true;
                                        break thisrecipe;
                                    }
                                }
                            }
                        }

                        if (!leftovers.isEmpty())
                            continue;

                        hasFailed = false;

                        if (p != null && recipe.hasAdvancedData("permission-node")) {
                            CraftBookPlugin.logDebugMessage("A recipe with permission nodes detected!", "advanced-data");
                            if (!p.hasPermission((String) recipe.getAdvancedData("permission-node"))) {
                                if (recipe.hasAdvancedData("permission-error"))
                                    lp.printError((String) recipe.getAdvancedData("permission-error"));
                                else
                                    lp.printError("mech.custom-crafting.recipe-permission");
                                ((CraftingInventory) event.getView().getTopInventory()).setResult(null);
                                return;
                            }
                        }

                        CraftBookPlugin.logDebugMessage("A recipe with custom data is being crafted!", "advanced-data");
                        bits = applyAdvancedEffects(event.getRecipe().getResult(), recipeRecipeEntry.getKey(), p);
                        break;
                    }
                }
            }
            if (hasFailed)
                throw new InvalidCraftingException("Unmet Item Meta");
        } catch (InvalidCraftingException e) {
            ((CraftingInventory) event.getView().getTopInventory()).setResult(null);
            return;
        } catch (Exception e) {
            e.printStackTrace();
            ((CraftingInventory) event.getView().getTopInventory()).setResult(null);
            return;
        }
        if (bits != null && !bits.equals(event.getRecipe().getResult())) {
            bits.setAmount(((CraftingInventory) event.getView().getTopInventory()).getResult().getAmount());
            ((CraftingInventory) event.getView().getTopInventory()).setResult(bits);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void prepareFurnace(InventoryClickEvent event) {
        if (!EventUtil.passesFilter(event)) return;

        if (!(event.getInventory() instanceof FurnaceInventory)) return;
        if (event.getAction() != InventoryAction.PLACE_ALL && event.getAction() != InventoryAction.PLACE_ONE && event.getAction() != InventoryAction.PLACE_SOME)
            return;
        if (event.getSlot() != 0) return;

        boolean shouldCancel = false;

        for (Entry<Recipe, RecipeManager.Recipe> recipeRecipeEntry : advancedRecipes.entrySet()) {
            if (!(recipeRecipeEntry.getKey() instanceof FurnaceRecipe)) continue;
            FurnaceRecipe frec = (FurnaceRecipe) recipeRecipeEntry.getKey();
            if (ItemUtil.areBaseItemsIdentical(frec.getInput(), event.getCurrentItem())) {

                RecipeManager.Recipe recipe = recipeRecipeEntry.getValue();
                if (ItemUtil.areItemsIdentical(event.getCurrentItem(), recipe.getIngredients().get(0).getItemStack())) {
                    shouldCancel = false;
                    break;
                } else {
                    shouldCancel = true;
                }
            }
        }

        if (shouldCancel)
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onFurnaceCook(FurnaceSmeltEvent event) {
        if (!EventUtil.passesFilter(event)) return;

        ItemStack bits = null;
        CraftBookPlugin.logDebugMessage("Smelting has been initiated!", "advanced-data");
        for (Entry<Recipe, RecipeManager.Recipe> recipeRecipeEntry : advancedRecipes.entrySet()) {
            if (!(recipeRecipeEntry.getKey() instanceof FurnaceRecipe)) continue;
            try {
                if (checkFurnaceRecipes((FurnaceRecipe) recipeRecipeEntry.getKey(), event.getSource(), event.getResult())) {

                    RecipeManager.Recipe recipe = recipeRecipeEntry.getValue();

                    ArrayList<ItemStack> leftovers = new ArrayList<>();
                    leftovers.add(event.getSource());
                    leftovers.removeAll(Collections.singleton(null));

                    if (!ItemUtil.isStackValid(event.getSource()))
                        continue;
                    for (CraftingItemStack cit : recipe.getIngredients()) {

                        if (ItemUtil.areBaseItemsIdentical(cit.getItemStack(), event.getSource())) {
                            CraftBookPlugin.logDebugMessage("Base item is correct!", "advanced-data");
                            if (ItemUtil.areItemsIdentical(cit.getItemStack(), event.getSource())) {
                                leftovers.remove(event.getSource());
                                CraftBookPlugin.logDebugMessage("MetaData correct!", "advanced-data");
                            } else {
                                CraftBookPlugin.logDebugMessage("MetaData incorrect!", "advanced-data");
                                throw new InvalidCraftingException("Unmet Item Meta");
                            }
                        }
                    }

                    if (!leftovers.isEmpty())
                        continue;

                    CraftBookPlugin.logDebugMessage("A recipe with custom data is being smelted!", "advanced-data");
                    bits = applyAdvancedEffects(event.getResult(), recipeRecipeEntry.getKey(), null);
                    break;
                }
            } catch (InvalidCraftingException e) {
                event.setResult(null);
                event.setCancelled(true);
                return;
            }
        }
        if (bits != null && !bits.equals(event.getResult())) {
            bits.setAmount(event.getResult().getAmount());
            event.setResult(bits);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCraft(CraftItemEvent event) {

        if (!EventUtil.passesFilter(event)) return;

        CraftBookPlugin.logDebugMessage("Crafting has been initiated!", "advanced-data");
        Player p = (Player) event.getWhoClicked();
        for (Entry<Recipe, RecipeManager.Recipe> recipeRecipeEntry : advancedRecipes.entrySet()) {

            if (ItemUtil.areRecipesIdentical(recipeRecipeEntry.getKey(), event.getRecipe())) {
                CraftBookPlugin.logDebugMessage("A recipe with custom data is being crafted!", "advanced-data");
                RecipeManager.Recipe recipe = recipeRecipeEntry.getValue();
                applyPostData(recipe, p, event);
                event.setCurrentItem(applyAdvancedEffects(event.getCurrentItem(), event.getRecipe(), (Player) event.getWhoClicked()));
                break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void applyPostData(RecipeManager.Recipe recipe, Player p, InventoryClickEvent event) {
        CraftBookPlayer localPlayer = CraftBookPlugin.inst().wrapPlayer(p);

        if (recipe.hasAdvancedData("permission-node")) {
            CraftBookPlugin.logDebugMessage("A recipe with permission nodes detected!", "advanced-data");
            if (!event.getWhoClicked().hasPermission((String) recipe.getAdvancedData("permission-node"))) {
                p.sendMessage(ChatColor.RED + "You do not have permission to craft this recipe!");
                event.setCancelled(true);
                return;
            }
        }
        if (recipe.hasAdvancedData("extra-results")) {
            CraftBookPlugin.logDebugMessage("A recipe with extra results is detected!", "advanced-data");
            ArrayList<CraftingItemStack> stacks = new ArrayList<>((Collection<CraftingItemStack>) recipe.getAdvancedData("extra-results"));
            for (CraftingItemStack stack : stacks) {
                if (stack.hasAdvancedData("chance"))
                    if (ThreadLocalRandom.current().nextDouble() < (Double) stack.getAdvancedData("chance"))
                        continue;
                HashMap<Integer, ItemStack> leftovers = event.getWhoClicked().getInventory().addItem(stack.getItemStack());
                if (!leftovers.isEmpty()) {
                    for (ItemStack istack : leftovers.values())
                        event.getWhoClicked().getWorld().dropItemNaturally(event.getWhoClicked().getLocation(), istack);
                }
            }
        }
        if (recipe.hasAdvancedData("commands-player") || recipe.hasAdvancedData("commands-console")) {
            CraftBookPlugin.logDebugMessage("A recipe with commands is detected!", "advanced-data");
            if (recipe.hasAdvancedData("commands-console")) {
                for (String s : (List<String>) recipe.getAdvancedData("commands-console")) {
                    s = ParsingUtil.parseLine(s, localPlayer);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s);
                }
            }
            if (recipe.hasAdvancedData("commands-player")) {
                for (String s : (List<String>) recipe.getAdvancedData("commands-player")) {
                    s = ParsingUtil.parseLine(s, localPlayer);
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
    }

    public static ItemStack craftItem(Recipe recipe) {
        for (Recipe rec : advancedRecipes.keySet()) {
            if (ItemUtil.areRecipesIdentical(rec, recipe))
                return applyAdvancedEffects(recipe.getResult(), rec, null);
        }

        return recipe.getResult();
    }

    private static ItemStack applyAdvancedEffects(ItemStack stack, Recipe rep, Player player) {
        RecipeManager.Recipe recipe = advancedRecipes.get(rep);

        if (recipe == null)
            return stack;

        ItemStack res = stack.clone();
        if (recipe.getResult().hasAdvancedData("item-meta"))
            res.setItemMeta(recipe.getResult().getItemStack().getItemMeta());
        return res;
    }

    private static boolean checkFurnaceRecipes(FurnaceRecipe rec1, ItemStack source, ItemStack result) {
        if (ItemUtil.areBaseItemsIdentical(rec1.getInput(), source))
            if (ItemUtil.areBaseItemsIdentical(rec1.getResult(), result))
                return true;

        return false;
    }

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

    }
}