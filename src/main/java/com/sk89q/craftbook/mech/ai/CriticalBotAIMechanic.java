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

        int amount = 0;
        switch(event.getEntity().getWorld().getDifficulty()) {
            case EASY:
                amount = 100;
                break;
            case HARD:
                amount = 20;
                break;
            case NORMAL:
                amount = 50;
                break;
            case PEACEFUL:
                return;
        }
        if (CraftBookPlugin.inst().getRandom().nextInt(amount) == 0) {
            CraftBookPlugin.logDebugMessage("Performing critical hit.", "ai-mechanics.shoot-bow.critical");
            event.getProjectile().setFireTicks(5000);
        }
    }
}