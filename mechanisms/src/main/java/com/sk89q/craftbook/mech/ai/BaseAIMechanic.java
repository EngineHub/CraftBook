package com.sk89q.craftbook.mech.ai;

import org.bukkit.event.entity.EntityTargetEvent;

public interface BaseAIMechanic {

    public abstract void onEntityTarget(EntityTargetEvent event);
}