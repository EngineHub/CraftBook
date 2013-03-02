package com.sk89q.craftbook.mech.arrows;

import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;

public interface ElementalArrow {

    public void addRecipe();

    public boolean onShoot(ProjectileLaunchEvent event);

    public boolean onHit(ProjectileHitEvent event);

    public boolean onCraft(PrepareItemCraftEvent event);
}