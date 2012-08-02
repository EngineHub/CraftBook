package com.sk89q.craftbook.mech;

import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.craftbook.mech.ai.BaseAIMechanic;
import com.sk89q.craftbook.mech.ai.ZombieAIMechanic;

public class AIMechanic implements Listener {

    public MechanismsPlugin plugin;

    public AIMechanic(MechanismsPlugin plugin) {
	this.plugin = plugin;
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
	if(!plugin.getLocalConfiguration().aiSettings.enabled) return;
	BaseAIMechanic ai = null;
	if(event.getEntity() instanceof Zombie)
	    ai = new ZombieAIMechanic(plugin);
	if(ai == null) return;
	ai.onEntityTarget(event);
    }
}
