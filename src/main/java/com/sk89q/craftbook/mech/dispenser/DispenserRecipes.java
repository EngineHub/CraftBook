package com.sk89q.craftbook.mech.dispenser;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Dispenser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ItemUtil;

/**
 * @author Me4502
 */
public class DispenserRecipes extends AbstractCraftBookMechanic {

    private Set<Recipe> recipes;

    private static DispenserRecipes instance;

    @Override
    public boolean enable () {

        instance = this;
        recipes = new HashSet<Recipe>();
        if(CraftBookPlugin.inst().getConfiguration().customDispensingXPShooter) addRecipe(new XPShooter());
        if(CraftBookPlugin.inst().getConfiguration().customDispensingSnowShooter) addRecipe(new SnowShooter());
        if(CraftBookPlugin.inst().getConfiguration().customDispensingFireArrows) addRecipe(new FireArrows());
        if(CraftBookPlugin.inst().getConfiguration().customDispensingFan) addRecipe(new Fan());
        if(CraftBookPlugin.inst().getConfiguration().customDispensingCannon) addRecipe(new Cannon());

        return recipes.size() > 0;
    }

    /**
     * Unloads the instanceof DispenserRecipes.
     */
    @Override
    public void disable() {

        recipes.clear();
        instance = null;
    }

    /**
     * Gets the instance of this DispenserRecipe manager.
     * 
     * @return The instance
     */
    public static DispenserRecipes inst() {

        return instance;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockDispense(BlockDispenseEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (!(event.getBlock().getState() instanceof Dispenser)) return; // Heh? Isn't this just for dispensers?
        Dispenser dis = (Dispenser) event.getBlock().getState();
        if (dispenseNew(dis, event.getItem(), event.getVelocity(), event)) {
            event.setCancelled(true);
        }
    }

    private boolean dispenseNew(Dispenser dis, ItemStack item, Vector velocity, BlockDispenseEvent event) {

        if (dis == null || dis.getInventory() == null || dis.getInventory().getContents() == null) return false;
        ItemStack[] stacks = dis.getInventory().getContents();
        for (Recipe r : recipes) {
            Material[] recipe = r.getRecipe();
            if (checkRecipe(stacks, recipe)) {
                boolean toReturn = r.doAction(dis, item, velocity, event);
                for (int i = 0; i < stacks.length; i++) {
                    if (recipe[i] != Material.AIR) {
                        stacks[i] = ItemUtil.getUsedItem(stacks[i]);
                    }
                }
                dis.getInventory().setContents(stacks);
                return toReturn;
            }
        }
        return false;
    }

    private static boolean checkRecipe(ItemStack[] stacks, Material[] recipe) {

        for (int i = 0; i < stacks.length; i++) {
            ItemStack stack = stacks[i];
            Material id = stack == null ? Material.AIR : stack.getType();
            if (recipe[i] != id) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds a dispenser recipe.
     *
     * @param recipe the recipe to add
     */
    public boolean addRecipe(Recipe recipe) {

        if (recipe == null) throw new NullPointerException("Dispenser recipe must not be null.");
        if (recipes.contains(recipe)) return false;
        recipes.add(recipe);
        return true;
    }
}