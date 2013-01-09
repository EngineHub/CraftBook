package com.sk89q.craftbook.mech.dispenser;

import java.util.ArrayList;

import org.bukkit.block.Dispenser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.ItemUtil;

/**
 * @author Me4502
 */
public class DispenserRecipes implements Listener {

    private final CraftBookPlugin plugin = CraftBookPlugin.inst();

    private final ArrayList<Recipe> recipes = new ArrayList<Recipe>();

    private static DispenserRecipes instance;

    public DispenserRecipes() {

        instance = this;
        addRecipe(new XPShooter());
        addRecipe(new SnowShooter());
        addRecipe(new FireArrows());
        addRecipe(new Fan());
        addRecipe(new Cannon());
    }

    public static DispenserRecipes inst() {

        return instance;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDispense(BlockDispenseEvent event) {

        if (plugin.getConfiguration().customDispensingEnabled) {
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