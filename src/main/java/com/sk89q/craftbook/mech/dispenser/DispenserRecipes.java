package com.sk89q.craftbook.mech.dispenser;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.block.Dispenser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.CraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.ItemUtil;

/**
 * @author Me4502
 */
public class DispenserRecipes implements CraftBookMechanic {

    private final Set<Recipe> recipes = new HashSet<Recipe>();

    private static DispenserRecipes instance;

    @Override
    public boolean enable () {

        instance = this;
        if(CraftBookPlugin.inst().getConfiguration().customDispensingXPShooter) addRecipe(new XPShooter());
        if(CraftBookPlugin.inst().getConfiguration().customDispensingSnowShooter) addRecipe(new SnowShooter());
        if(CraftBookPlugin.inst().getConfiguration().customDispensingFireArrows) addRecipe(new FireArrows());
        if(CraftBookPlugin.inst().getConfiguration().customDispensingFan) addRecipe(new Fan());
        if(CraftBookPlugin.inst().getConfiguration().customDispensingCannon) addRecipe(new Cannon());

        return true;
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDispense(BlockDispenseEvent event) {

        if (CraftBookPlugin.inst().getConfiguration().customDispensingEnabled) {
            if (!(event.getBlock().getState() instanceof Dispenser)) return; // Heh? Isn't this just for dispensers?
            Dispenser dis = (Dispenser) event.getBlock().getState();
            if (dispenseNew(dis, event.getItem(), event.getVelocity(), event)) {
                event.setCancelled(true);
            }
        }
    }

    private boolean dispenseNew(Dispenser dis, ItemStack item, Vector velocity, BlockDispenseEvent event) {

        if (dis == null || dis.getInventory() == null || dis.getInventory().getContents() == null) return false;
        ItemStack[] stacks = dis.getInventory().getContents();
        for (Recipe r : recipes) {
            int[] recipe = r.getRecipe();
            if (checkRecipe(stacks, recipe)) {
                boolean toReturn = r.doAction(dis, item, velocity, event);
                for (int i = 0; i < stacks.length; i++) {
                    if (recipe[i] != 0) {
                        stacks[i] = ItemUtil.getUsedItem(stacks[i]);
                    }
                }
                dis.getInventory().setContents(stacks);
                return toReturn;
            }
        }
        return false;
    }

    private static boolean checkRecipe(ItemStack[] stacks, int[] recipe) {

        for (int i = 0; i < stacks.length; i++) {
            ItemStack stack = stacks[i];
            int id = stack == null ? 0 : stack.getTypeId();
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