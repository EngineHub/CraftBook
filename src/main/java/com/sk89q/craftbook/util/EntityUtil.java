package com.sk89q.craftbook.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;

public class EntityUtil {

    /**
     * Checks if an entity is standing in a specific block.
     * 
     * @param entity The entity to check.
     * @param block The block to check.
     * @return Whether the entity is in the block or not.
     */
    public static boolean isEntityInBlock(Entity entity, Block block) {

        Location entLoc = entity.getLocation().getBlock().getLocation();
        int heightOffset = 0;

        if(entity instanceof LivingEntity) {
            heightOffset = (int) Math.floor(((LivingEntity) entity).getEyeHeight());
        }
        while(heightOffset >= 0) {
            if(entLoc.getBlockX() == block.getLocation().getBlockX())
                if(entLoc.getBlockY()+heightOffset == block.getLocation().getBlockY())
                    if(entLoc.getBlockZ() == block.getLocation().getBlockZ())
                        return true;
            heightOffset --;
        }

        return false;
    }

    /**
     * Kills an entity using the proper way for it's entity type.
     * 
     * @param ent The entity to kill.
     */
    public static void killEntity(Entity ent) {

        if(ent instanceof Damageable)
            ((Damageable) ent).damage(((Damageable) ent).getHealth());
        else
            ent.remove();
    }

    /**
     * Damages an entity using the proper way for it's entity type.
     * 
     * @param ent The entity to damage.
     * @param damage The amount to damage it by.
     */
    public static void damageEntity(Entity ent, int damage) {

        if(ent instanceof Damageable)
            ((Damageable) ent).damage(damage);
        else if (ent instanceof Minecart)
            ((Minecart) ent).setDamage(((Minecart) ent).getDamage() + damage);
        else
            ent.remove();
    }

    public static org.bukkit.entity.EntityType[] parseEntityList(List<String> list) {

        List<org.bukkit.entity.EntityType> ents = new ArrayList<org.bukkit.entity.EntityType>();
        for(String s : list)
            ents.add(org.bukkit.entity.EntityType.fromName(s));

        return ents.toArray(new org.bukkit.entity.EntityType[ents.size()]);
    }
}