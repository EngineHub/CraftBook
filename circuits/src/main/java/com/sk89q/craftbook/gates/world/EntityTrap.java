package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ic.*;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.PoweredMinecart;
import org.bukkit.entity.StorageMinecart;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.util.EnumUtil;
import com.sk89q.craftbook.util.LocationUtil;

/**
 * @author Me4502
 */
public class EntityTrap extends AbstractIC {

    private enum Type {
        PLAYER,
        MOB_HOSTILE,
        MOB_PEACEFUL,
        MOB_ANY,
        ANY,
        CART,
        CART_STORAGE,
        CART_POWERED,
        ITEM;

        public boolean is(Entity entity) {

            switch (this) {
                case PLAYER:
                    return entity instanceof Player;
                case MOB_HOSTILE:
                    return entity instanceof Monster;
                case MOB_PEACEFUL:
                    return entity instanceof Animals;
                case MOB_ANY:
                    return entity instanceof Creature;
                case CART:
                    return entity instanceof Minecart;
                case CART_STORAGE:
                    return entity instanceof StorageMinecart;
                case CART_POWERED:
                    return entity instanceof PoweredMinecart;
                case ITEM:
                    return entity instanceof Item;
                case ANY:
                    return true;
            }
            return false;
        }

        public static Type fromString(String name) {

            return EnumUtil.getEnumFromString(EntityTrap.Type.class, name);
        }
    }

    public EntityTrap(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Entity Trap";
    }

    @Override
    public String getSignTitle() {

        return "ENTITY TRAP";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            chip.setOutput(0, hurt());
        }
    }

    /**
     * Returns true if the entity was damaged.
     *
     * @return
     */
    protected boolean hurt() {

        int radius = 10; //Default Radius
        int damage = 2;
        Location location = BukkitUtil.toSign(getSign()).getLocation();
        Type type = Type.MOB_HOSTILE;
        try {
            String[] splitLine = ICUtil.EQUALS_PATTERN.split(getSign().getLine(2), 3);
            radius = Integer.parseInt(splitLine[0]);
            if (getSign().getLine(2).contains("=")) {
                String[] pos = ICUtil.COLON_PATTERN.split(splitLine[1]);
                int x = Integer.parseInt(pos[0]);
                int y = Integer.parseInt(pos[1]);
                int z = Integer.parseInt(pos[2]);
                location.add(x, y, z);

                damage = Integer.parseInt(splitLine[2]);
            }
        } catch (Exception ignored) {
        }

        if (!getSign().getLine(3).isEmpty()) {
            type = Type.fromString(getSign().getLine(3));
        }

        try {
            for (Entity e : LocationUtil.getNearbyEntities(location, radius)) {
                if (e.isDead() || !e.isValid()) {
                    continue;
                }
                if (!type.is(e)) {
                    continue;
                }
                if (e instanceof LivingEntity) {
                    ((LivingEntity) e).damage(damage);
                } else if (e instanceof Minecart) {
                    ((Minecart) e).setDamage(((Minecart) e).getDamage() + damage);
                } else {
                    e.remove();
                }
                return true;
            }
        } catch (Exception ignored) {
        }

        return false;
    }


    public static class Factory extends AbstractICFactory implements
    RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new EntityTrap(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Damage nearby entities of type.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "radius=x:y:z=damage",
                    "mob type"
            };
            return lines;
        }
    }
}