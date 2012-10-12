package com.sk89q.craftbook.mech.dispenser;

import java.util.ArrayList;

import org.bukkit.block.Dispenser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.craftbook.util.ItemUtil;

/**
 * @author Me4502
 */
public class DispenserRecipes implements Listener {

    final MechanismsPlugin plugin;

    final ArrayList<Recipe> recipes = new ArrayList<Recipe>();

    public DispenserRecipes(MechanismsPlugin plugin) {

        this.plugin = plugin;
        addRecipe(new XPShooter());
        addRecipe(new SnowShooter());
        addRecipe(new FireArrows());
        addRecipe(new Fan());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockDispense(BlockDispenseEvent event) {

        if (plugin.getLocalConfiguration().dispenserSettings.enable) {
            if(!(event.getBlock().getState() instanceof Dispenser))
                return; //Heh? Isn't this just for dispensers?
            Dispenser dis = (Dispenser) event.getBlock().getState();
            if (dispenseNew(dis, event.getItem(), event.getVelocity(), event)) event.setCancelled(true);
        }
    }

    private boolean dispenseNew(Dispenser dis, ItemStack item, Vector velocity, BlockDispenseEvent event) {

        if (dis == null || dis.getInventory() == null || dis.getInventory().getContents() == null) return false;
        ItemStack[] stacks = dis.getInventory().getContents();
        boolean toReturn = false;
        try {
            for (Recipe r : recipes) {
                if (r == null) {
                    recipes.remove(r); //Invalid recipe (save CPU cycles a little) (Should never occur)
                    continue;
                }
                current:
                {
                    if (r.recipe[0] == 0 && stacks[0] == null || r.recipe[0] == stacks[0].getTypeId()) {
                        for (int i = 1; i < stacks.length; i++)
                            if (!(!(r.recipe[i] != 0 && stacks[i] == null) && (r.recipe[i] == 0 && stacks[i] ==
                                    null || r.recipe[i] == stacks[i].getTypeId())))
                                break current; //This recipe is wrong. Do normal dispenser stuff...
                        toReturn = r.doAction(dis, item, velocity, event);
                        for (int i = 1; i < stacks.length; i++)
                            if (r.recipe[i] == 0 && stacks[i] == null || r.recipe[i] == stacks[i].getTypeId()) {
                                if (stacks[i] == null || stacks[i].getTypeId() == 0 || r.recipe[i] == 0)
                                    continue;
                                stacks[i] = ItemUtil.getUsedItem(stacks[i]);
                            } else
                                return true; //Cancel the event, as obviously something went wrong.
                        dis.getInventory().setContents(stacks);
                    }
                    break current;
                }
            }
            return toReturn; //Leave it be.
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Adds a dispenser recipe.
     *
     * @param recipe
     */
    public boolean addRecipe(Recipe recipe) {

        if (recipes.contains(recipe)) return false;
        recipes.add(recipe);
        return true;
    }
}