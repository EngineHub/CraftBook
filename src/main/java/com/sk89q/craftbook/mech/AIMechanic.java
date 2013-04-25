package com.sk89q.craftbook.mech;

import java.util.HashSet;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTargetEvent;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.mech.ai.BaseAIMechanic;
import com.sk89q.craftbook.mech.ai.BowShotAIMechanic;
import com.sk89q.craftbook.mech.ai.SkeletonAIMechanic;
import com.sk89q.craftbook.mech.ai.TargetAIMechanic;
import com.sk89q.craftbook.mech.ai.ZombieAIMechanic;

public class AIMechanic implements Listener {

    HashSet<BaseAIMechanic> mechanics = new HashSet<BaseAIMechanic>();

    public AIMechanic() {

        if (!CraftBookPlugin.inst().getConfiguration().aiEnabled) return;

        if (CraftBookPlugin.inst().getConfiguration().aiZombieEnabled) {
            registerAIMechanic(new ZombieAIMechanic(EntityType.ZOMBIE));
        }
        if (CraftBookPlugin.inst().getConfiguration().aiSkeletonEnabled) {
            registerAIMechanic(new SkeletonAIMechanic(EntityType.SKELETON));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event) {

        if (event.getTarget() == null || event.getEntity() == null) return;
        for (BaseAIMechanic mechanic : mechanics) {
            if (!(mechanic instanceof TargetAIMechanic))
                continue;

            boolean passes = false;

            for(EntityType t : mechanic.entityType) {
                if(t == event.getEntity().getType()) {
                    passes = true;
                    break;
                }
            }

            if(passes)
                ((TargetAIMechanic) mechanic).onEntityTarget(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityShootBow(EntityShootBowEvent event) {

        if (event.getEntity() == null) return;
        for (BaseAIMechanic mechanic : mechanics) {
            if (!(mechanic instanceof BowShotAIMechanic))
                continue;

            boolean passes = false;

            for(EntityType t : mechanic.entityType) {
                if(t == event.getEntity().getType()) {
                    passes = true;
                    break;
                }
            }

            if(passes)
                ((BowShotAIMechanic) mechanic).onBowShot(event);
        }
    }

    public boolean registerAIMechanic(BaseAIMechanic mechanic) {

        return !mechanics.contains(mechanic) && mechanics.add(mechanic);
    }
}