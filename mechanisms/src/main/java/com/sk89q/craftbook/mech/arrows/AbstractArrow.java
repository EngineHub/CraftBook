package com.sk89q.craftbook.mech.arrows;

import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.Recipe;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractArrow implements ElementalArrow {

    @SuppressWarnings("unused")
    private List<Integer> arrows = new ArrayList<Integer>();

    Recipe recipe;

    MechanismsPlugin plugin;

    String name;

    public AbstractArrow(MechanismsPlugin plugin, String name) {
        this.plugin = plugin;
        this.name = name;
    }

    @Override
    public abstract void addRecipe ();

    @Override
    public boolean onShoot (ProjectileLaunchEvent event) {
        //TODO work out if its a CraftBook arrow.
        return false;
    }

    @Override
    public boolean onHit (ProjectileHitEvent event) {
        return false;
    }

    @Override
    public boolean onCraft (PrepareItemCraftEvent event) {
        if(event.getRecipe().equals(recipe))
            return true;
        else
            return false;
    }
}
