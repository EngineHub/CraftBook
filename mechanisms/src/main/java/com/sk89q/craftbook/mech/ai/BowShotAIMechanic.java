package com.sk89q.craftbook.mech.ai;

import org.bukkit.event.entity.EntityShootBowEvent;

public interface BowShotAIMechanic {

    public abstract void onBowShot(EntityShootBowEvent event);

}