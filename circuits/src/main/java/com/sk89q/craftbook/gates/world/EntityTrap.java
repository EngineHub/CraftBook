package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.EnumUtil;
import com.sk89q.craftbook.util.LocationUtil;
import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.*;

import java.util.Collection;

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
        CART_POWERED;

        @SuppressWarnings("unused")
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
                case ANY:
                    return true;
            }
            return false;
        }

        public static Type fromString(String name) {

            return EnumUtil.getEnumFromString(EntityTrap.Type.class, name);
        }
    }

    private Block center;
    private int radius;
    private Type type;
    private Collection<Chunk> chunks;

    public EntityTrap(Server server, Sign sign) {

        super(server, sign);
        load();
    }

    private void load() {

        Sign sign = getSign();
        center = ICUtil.parseBlockLocation(sign);
        radius = ICUtil.parseRadius(sign);
        // lets get the type to detect first
        type = Type.fromString(sign.getLine(3).trim());
        // set the type to any if wrong format
        if (type == null) type = Type.ANY;
        // update the sign with correct upper case name
        sign.setLine(3, type.name());
        sign.update();
        this.chunks = LocationUtil.getSurroundingChunks(center, radius);
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

        int damage = 2;
        // add the offset to the location of the block connected to the sign
        for (Chunk chunk : chunks) {
            if (chunk.isLoaded()) {
                // get all entites from the chunks in the defined radius
                for (Entity entity : chunk.getEntities()) {
                    if (!entity.isDead()) {
                        if (type.is(entity)) {
                            // at last check if the entity is within the radius
                            if (LocationUtil.isWithinRadius(center.getLocation(), entity.getLocation(), radius)) {
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

        @Override
        public void verify(Sign sign) throws ICVerificationException {

            ICUtil.verifySignSyntax(sign);
        }
    }
}
