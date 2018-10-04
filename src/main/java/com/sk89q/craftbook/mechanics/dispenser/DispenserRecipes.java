package com.sk89q.craftbook.mechanics.dispenser;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Me4502
 */
public class DispenserRecipes extends AbstractCraftBookMechanic {

    private Set<Recipe> recipes;

    private static DispenserRecipes instance;

    @Override
    public boolean enable () {

        instance = this;
        recipes = new HashSet<>();
        if(xpShooterEnable) addRecipe(new XPShooter());
        if(snowShooterEnable) addRecipe(new SnowShooter());
        if(fireArrowsEnable) addRecipe(new FireArrows());
        if(fanEnable) addRecipe(new Fan());
        if(cannonEnable) addRecipe(new Cannon());

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

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockDispense(BlockDispenseEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (event.getBlock().getType() != Material.DISPENSER) return;
        if (dispenseNew(event.getBlock(), event.getItem(), event.getVelocity(), event)) {
            event.setCancelled(true);
        }
    }

    private boolean dispenseNew(Block block, ItemStack item, Vector velocity, BlockDispenseEvent event) {

        Dispenser dis = (Dispenser) block.getState();
        if (dis == null || dis.getInventory() == null || dis.getInventory().getContents() == null) return false;
        ItemStack[] stacks = dis.getInventory().getContents();
        for (Recipe r : recipes) {
            Material[] recipe = r.getRecipe();
            if (checkRecipe(stacks, recipe)) {
                boolean toReturn = r.doAction(block, item, velocity, event);
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

    private boolean cannonEnable;
    private boolean fanEnable;
    private boolean fireArrowsEnable;
    private boolean snowShooterEnable;
    private boolean xpShooterEnable;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "cannon-enable", "Enables Cannon Dispenser Recipe.");
        cannonEnable = config.getBoolean(path + "cannon-enable", true);

        config.setComment(path + "fan-enable", "Enables Fan Dispenser Recipe.");
        fanEnable = config.getBoolean(path + "fan-enable", true);

        config.setComment(path + "fire-arrows-enable", "Enables Fire Arrows Dispenser Recipe.");
        fireArrowsEnable = config.getBoolean(path + "fire-arrows-enable", true);

        config.setComment(path + "snow-shooter-enable", "Enables Snow Shooter Dispenser Recipe.");
        snowShooterEnable = config.getBoolean(path + "snow-shooter-enable", true);

        config.setComment(path + "xp-shooter-enable", "Enables XP Shooter Dispenser Recipe.");
        xpShooterEnable = config.getBoolean(path + "xp-shooter-enable", true);
    }
}