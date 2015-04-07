package com.sk89q.craftbook.mechanics.arrows;

import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;

public interface ElementalArrow {

    public void addRecipe();

    public boolean onShoot(EntityShootBowEvent event);

    public boolean onHit(ProjectileHitEvent event);

    public boolean onCraft(PrepareItemCraftEvent event);
}