package com.sk89q.craftbook.mechanics.arrows;

import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;

public interface ElementalArrow {

    void addRecipe();

    boolean onShoot(EntityShootBowEvent event);

    boolean onHit(ProjectileHitEvent event);

    boolean onCraft(PrepareItemCraftEvent event);
}