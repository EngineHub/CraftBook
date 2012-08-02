package com.sk89q.craftbook.mech.ai;

import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityTargetEvent;

import com.sk89q.craftbook.bukkit.MechanismsPlugin;

public class ZombieAIMechanic implements BaseAIMechanic {

    MechanismsPlugin plugin;

    public ZombieAIMechanic(MechanismsPlugin plugin) {
	this.plugin = plugin;
    }

    @Override
    public void onEntityTarget(EntityTargetEvent event) {
	if(!(event.getEntity() instanceof Zombie)) return; //Just making sure

	Zombie zombie = (Zombie)event.getEntity();
	if(!zombie.hasLineOfSight(event.getTarget())) { //Zombie can't see the target.
	    if(event.getTarget() instanceof Player) {
		if(!((Player)event.getTarget()).isSprinting())
		    event.setCancelled(true);
	    }
	    else
		event.setCancelled(true);
	}
	if(zombie.getLocation().getBlock().getLightLevel() > 6) return; //They can clearly see the target.
	if(event.getTarget() instanceof Player) {
	    if(((Player)event.getTarget()).isSneaking() && event.getTarget().getLocation().distance(zombie.getLocation()) > 2)
		event.setCancelled(true);
	}
    }
}