package com.sk89q.craftbook.mech.ai;

import org.bukkit.entity.Entity;

import com.sk89q.craftbook.bukkit.MechanismsPlugin;

public class BaseAIMechanic {

    public MechanismsPlugin plugin;
    public Entity entity;

    public BaseAIMechanic(MechanismsPlugin plugin, Entity entity) {

        this.plugin = plugin;
        this.entity = entity;
    }
}