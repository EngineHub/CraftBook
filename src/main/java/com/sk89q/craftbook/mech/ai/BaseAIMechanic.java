package com.sk89q.craftbook.mech.ai;

import org.bukkit.entity.EntityType;

public class BaseAIMechanic {

    public EntityType[] entityType;

    public BaseAIMechanic(EntityType ... entityType) {

        this.entityType = entityType;
    }
}