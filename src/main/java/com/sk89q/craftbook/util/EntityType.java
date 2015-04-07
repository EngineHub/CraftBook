package com.sk89q.craftbook.util;

import java.util.EnumSet;
import java.util.Set;

import org.bukkit.entity.Ambient;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.Horse;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.entity.minecart.StorageMinecart;

public enum EntityType {

    PLAYER('P'), ITEM('I'), MOB_HOSTILE('H'), MOB_PEACEFUL('A'), MOB_ANY('M'), ANY('L'), CART('C'), RIDEABLE('R'),
    CART_STORAGE('S'), CART_POWERED('E'), CART_HOPPER('O'), EXPLOSIVE('T'), AMBIENT('N'), NON_LIVING('D'), LIVING('L');

    public boolean is(Entity entity) {

        switch (this) {
            case PLAYER:
                return entity instanceof Player;
            case ITEM:
                return entity instanceof Item;
            case MOB_HOSTILE:
                return entity instanceof Monster && !(entity instanceof HumanEntity);
            case MOB_PEACEFUL:
                return entity instanceof Animals && !(entity instanceof HumanEntity);
            case MOB_ANY:
                return entity instanceof Creature && !(entity instanceof HumanEntity);
            case CART:
                return entity instanceof Minecart;
            case CART_STORAGE:
                return entity instanceof StorageMinecart;
            case CART_POWERED:
                return entity instanceof PoweredMinecart;
            case CART_HOPPER:
                return entity instanceof HopperMinecart;
            case EXPLOSIVE:
                return entity instanceof Explosive;
            case RIDEABLE:
                return entity instanceof RideableMinecart || entity instanceof Boat || entity instanceof Pig || entity instanceof Horse;
            case AMBIENT:
                return entity instanceof Ambient;
            case NON_LIVING:
                return !(entity instanceof LivingEntity) && !(entity instanceof HumanEntity);
            case LIVING:
                return entity instanceof LivingEntity;
            case ANY:
                return true;
            default:
                break;
        }
        return false;
    }

    private final char shortName;

    private EntityType(char shortName) {

        this.shortName = shortName;
    }

    public char getCharName() {

        return shortName;
    }

    public static Set<EntityType> getDetected(String line) {

        Set<EntityType> types = EnumSet.noneOf(EntityType.class);

        if (line.trim().isEmpty()) {
            types.add(ANY);
            return types;
        }

        EntityType type = EnumUtil.getEnumFromString(EntityType.class, line);
        if (type != null) {
            types.add(type);
        } else {
            for (char aChar : line.toUpperCase().toCharArray()) {
                for (EntityType aType : EntityType.values()) {
                    if (aType.getCharName() == aChar) {
                        types.add(aType);
                    }
                }
            }
        }

        if (types.isEmpty()) {
            types.add(ANY);
        }

        return types;
    }

    public static EntityType fromString(String name) {

        return EnumUtil.getEnumFromString(EntityType.class, name);
    }
}