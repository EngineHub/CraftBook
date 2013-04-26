package com.sk89q.craftbook.mech.ai;

import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityShootBowEvent;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;

public class CriticalBotAIMechanic extends BaseAIMechanic implements BowShotAIMechanic {

    public CriticalBotAIMechanic(EntityType ... entity) {

        super(entity);
    }

    @Override
    public void onBowShot(EntityShootBowEvent event) {

        if (CraftBookPlugin.inst().getRandom().nextInt(30) > 25)
            event.getProjectile().setFireTicks(5000);
    }
}