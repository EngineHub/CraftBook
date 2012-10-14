package com.sk89q.craftbook.mech.ai;

import java.util.Random;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityShootBowEvent;

import com.sk89q.craftbook.bukkit.MechanismsPlugin;

public class SkeletonAIMechanic extends BaseAIMechanic implements BowShotAIMechanic {

    Random r = new Random();

    public SkeletonAIMechanic(MechanismsPlugin plugin, Entity entity) {

        super(plugin, entity);
    }

    @Override
    public void onBowShot(EntityShootBowEvent event) {
        if(event.getEntityType() != EntityType.SKELETON) return;
        if(r.nextInt(30) == 0) {
            event.getProjectile().setFireTicks(5000);
        }
    }
}