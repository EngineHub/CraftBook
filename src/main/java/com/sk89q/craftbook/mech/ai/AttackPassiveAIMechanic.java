package com.sk89q.craftbook.mech.ai;

import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.event.entity.EntityTargetEvent;

public class AttackPassiveAIMechanic extends BaseAIMechanic implements TargetAIMechanic {

    public AttackPassiveAIMechanic(EntityType ... entity) {

        super(entity);
    }

    @Override
    public void onEntityTarget (EntityTargetEvent event) {

        if(event.getTarget() != null) return;
        for(Entity ent : event.getEntity().getNearbyEntities(15D, 15D, 15D)) {
            if(ent instanceof Animals) {
                if(event.getEntity() instanceof Monster) {
                    event.setCancelled(true);
                    ((Monster) event.getEntity()).setTarget((Animals) ent);
                } else
                    event.setTarget(ent);
                return;
            }
        }
    }
}