package com.sk89q.craftbook.mech.arrows;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Arrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;

import com.sk89q.craftbook.util.EventUtil;

// TODO finish
public class ElementalArrowsMechanic implements Listener {

    /*
     * How I propose they work.
     * 
     * On Craft, they are given a special name. (Eg, Fire Arrows) On Shoot, somehow we check the name and if it's one
     * of those arrows, we add the
     * entityID to the list. On hit, we check the list for entityID. If it's there, we do the stuff and remove it.
     */

    List<ElementalArrow> arrows = new ArrayList<ElementalArrow>();

    public ElementalArrowsMechanic() {

        registerArrow(new FireArrow());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArrowHit(ProjectileHitEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (event.getEntity() instanceof Arrow) {
            for (ElementalArrow e : arrows) {
                if (e.onHit(event)) return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArrowShot(EntityShootBowEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (event.getProjectile() instanceof Arrow) {
            for (ElementalArrow e : arrows) {
                if (e.onShoot(event)) return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemCraft(PrepareItemCraftEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        for (ElementalArrow e : arrows) {
            if (e.onCraft(event)) return;
        }
    }

    public void registerArrow(ElementalArrow arrow) {

        arrow.addRecipe();
        arrows.add(arrow);
    }
}