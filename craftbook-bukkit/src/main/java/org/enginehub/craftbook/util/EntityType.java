/*
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package org.enginehub.craftbook.util;

import org.bukkit.entity.Ambient;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.Horse;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.entity.minecart.StorageMinecart;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public enum EntityType {

    PLAYER('P'), ITEM('I'), MOB_HOSTILE('H'), MOB_PEACEFUL('A'), MOB_ANY('M'), ANY('L'), CART('C'), RIDEABLE('R'),
    CART_STORAGE('S'), CART_POWERED('E'), CART_HOPPER('O'), EXPLOSIVE('T'), AMBIENT('N'), NON_LIVING('D'), LIVING('L');

    public boolean is(Entity entity) {
        return switch (this) {
            case PLAYER -> entity instanceof Player;
            case ITEM -> entity instanceof Item;
            case MOB_HOSTILE -> entity instanceof Monster && !(entity instanceof HumanEntity);
            case MOB_PEACEFUL -> entity instanceof Animals && !(entity instanceof HumanEntity);
            case MOB_ANY -> entity instanceof Mob && !(entity instanceof HumanEntity);
            case CART -> entity instanceof Minecart;
            case CART_STORAGE -> entity instanceof StorageMinecart;
            case CART_POWERED -> entity instanceof PoweredMinecart;
            case CART_HOPPER -> entity instanceof HopperMinecart;
            case EXPLOSIVE -> entity instanceof Explosive;
            case RIDEABLE ->
                entity instanceof RideableMinecart || entity instanceof Boat || entity instanceof Pig || entity instanceof Horse;
            case AMBIENT -> entity instanceof Ambient;
            case NON_LIVING -> !(entity instanceof LivingEntity);
            case LIVING -> entity instanceof LivingEntity;
            case ANY -> true;
        };
    }

    private final char shortName;

    EntityType(char shortName) {

        this.shortName = shortName;
    }

    public char getCharName() {

        return shortName;
    }

    public static Set<EntityType> getDetected(String line) {

        Set<EntityType> types = new HashSet<>();

        if (line.trim().isEmpty()) {
            types.add(ANY);
            return types;
        }

        EntityType type = EnumUtil.getEnumFromString(EntityType.class, line);
        if (type != null) {
            types.add(type);
        } else {
            for (char aChar : line.toUpperCase(Locale.ENGLISH).toCharArray()) {
                for (EntityType aType : EntityType.values()) {
                    if (aType.shortName == aChar) {
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