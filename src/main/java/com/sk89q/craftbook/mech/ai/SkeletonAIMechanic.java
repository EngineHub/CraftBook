package com.sk89q.craftbook.mech.ai;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityShootBowEvent;

public class SkeletonAIMechanic extends BaseAIMechanic implements BowShotAIMechanic {

    public SkeletonAIMechanic(Entity entity) {

        super(entity);
    }

    @Override
    public void onBowShot(EntityShootBowEvent event) {

        if (event.getEntityType() != EntityType.SKELETON) return;
        if (CraftBookPlugin.inst().getRandom().nextInt(30) == 0) {
            event.getProjectile().setFireTicks(5000);
        }
    }
}