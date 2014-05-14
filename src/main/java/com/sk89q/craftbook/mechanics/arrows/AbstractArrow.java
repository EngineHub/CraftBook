package com.sk89q.craftbook.mechanics.arrows;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.Recipe;

public abstract class AbstractArrow implements ElementalArrow {

    @SuppressWarnings("unused")
    private List<Integer> arrows = new ArrayList<Integer>();

    Recipe recipe;

    String name;

    public AbstractArrow(String name) {

        this.name = name;
    }

    @Override
    public abstract void addRecipe();

    @Override
    public boolean onShoot(EntityShootBowEvent event) {
        // TODO work out if its a CraftBook arrow.
        return false;
    }

    @Override
    public boolean onHit(ProjectileHitEvent event) {

        return false;
    }

    @Override
    public boolean onCraft(PrepareItemCraftEvent event) {

        return event.getRecipe().equals(recipe);
    }
}
