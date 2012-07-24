package com.sk89q.craftbook.gates.world;

import java.util.LinkedHashSet;
import java.util.Set;

import com.sk89q.craftbook.util.LocationUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Sign;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.PoweredMinecart;
import org.bukkit.entity.StorageMinecart;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.util.EnumUtil;
import com.sk89q.craftbook.util.SignUtil;

/**
 * 
 * @author Me4502
 *
 */
public class EntityTrap extends AbstractIC {

    private enum Type {
        PLAYER,
        MOBHOSTILE,
        MOBPEACEFUL,
        ANYMOB,
        ANY,
        CART,
        STORAGECART,
        POWEREDCART;

        public boolean is(Entity entity) {

            switch (this) {
            case PLAYER:
                return entity instanceof Player;
            case MOBHOSTILE:
                return entity instanceof Monster;
            case MOBPEACEFUL:
                return entity instanceof Animals;
            case ANYMOB:
                return entity instanceof Creature;
            case CART:
                return entity instanceof Minecart;
            case STORAGECART:
                return entity instanceof StorageMinecart;
            case POWEREDCART:
                return entity instanceof PoweredMinecart;
            case ANY:
                return true;
            }
            return false;
        }

        public static Type fromString(String name) {
            return EnumUtil.getEnumFromString(EntityTrap.Type.class, name);
        }
    }

    public EntityTrap(Server server, Sign sign) {
        super(server, sign);
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

        Sign sign = getSign();
        // lets get the type to detect first
        Type type = Type.fromString(sign.getLine(3).trim());
        // set the type to any if wrong format
        if (type == null) type = Type.ANY;
        // update the sign with correct upper case name
        sign.setLine(3, type.name());
        sign.update();
        // now check the third line for the radius and offset
        String line = sign.getLine(2).trim();
        // if the line contains a = the offset is given
        // the given string should look something like that:
        // radius=x:y:z or radius, e.g. 1=-2:5:11
        int radius = 1, offsetX = 0, offsetY = 1, offsetZ = 0, damage = 2;
        if (line.contains("=")) {
            try {
                String[] split = line.split("=");
                radius = Integer.parseInt(split[0]);
                // parse the offset
                String[] offsetSplit = split[1].split(":");
                offsetX = Integer.parseInt(offsetSplit[0]);
                offsetY = Integer.parseInt(offsetSplit[1]);
                offsetZ = Integer.parseInt(offsetSplit[2]);
                //parse the damage
                damage = Integer.parseInt(split[2]);
            } catch (NumberFormatException e) {
                // do nothing and use the defaults
            } catch (IndexOutOfBoundsException e) {
                // do nothing and use the defaults
            }
        } else if (line.length() > 0) {
            radius = Integer.parseInt(line);
        } else
            radius = 1;

        Location location = SignUtil.getBackBlock(getSign().getBlock()).getLocation();
        // add the offset to the location of the block connected to the sign
        location.add(offsetX, offsetY, offsetZ);
        for (Chunk chunk : LocationUtil.getSurroundingChunks(location.getBlock(), radius)) {
            if (chunk.isLoaded()) {
                // get all entites from the chunks in the defined radius
                for (Entity entity : chunk.getEntities()) {
                    if (!entity.isDead()) {
                        if (type.is(entity)) {
                            // at last check if the entity is within the radius
                            if (LocationUtil.getGreatestDistance(entity.getLocation(), location) <= radius) {
                                if (entity instanceof LivingEntity)
                                    ((LivingEntity) entity).damage(damage);
                                else if (entity instanceof Minecart)
                                    ((Minecart) entity).setDamage(((Minecart) entity).getDamage() + damage);
                                else
                                    entity.remove();
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static class Factory extends AbstractICFactory implements
    RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new EntityTrap(getServer(), sign);
        }
    }
}
