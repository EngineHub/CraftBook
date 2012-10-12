package com.sk89q.craftbook.gates.world;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.ICVerificationException;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.util.EnumUtil;
import com.sk89q.craftbook.util.SignUtil;

public class SentryGun extends AbstractIC {

    /**
     * @author Me4502
     */
    private enum Type {
        PLAYER,
        MOB_HOSTILE,
        MOB_PEACEFUL,
        MOB_ANY;

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
                default:
                    return entity instanceof Monster;
            }
        }

        public static Type fromString(String name) {

            return EnumUtil.getEnumFromString(SentryGun.Type.class, name);
        }
    }

    private Type type;
    private Block center;
    private int radius = 10;

    public SentryGun(Server server, Sign block, ICFactory factory) {

        super(server, block, factory);
        load();
    }

    private void load() {

        type = Type.fromString(getSign().getLine(2));
        center = SignUtil.getBackBlock(getSign().getBlock());
        radius = Integer.parseInt(getSign().getLine(3));
    }

    @Override
    public String getTitle() {

        return "Sentry Gun";
    }

    @Override
    public String getSignTitle() {

        return "SENTRY GUN";
    }

    @Override
    public void trigger(ChipState chip) {

        shoot();
    }

    public void shoot() {

        // add the offset to the location of the block connected to the sign
        /*
        for (Chunk chunk : LocationUtil.getSurroundingChunks(center, radius)) {
            if (chunk.isLoaded()) {
                // get all entites from the chunks in the defined radius
                for (Entity entity : chunk.getEntities()) {
                    if (!entity.isDead()) {
                        if (type.is(entity)) {
                            // at last check if the entity is within the radius
                            if (entity.getLocation().distanceSquared(center.getLocation()) <= radius * radius) {
                                Block signBlock = getSign().getBlock();
                                BlockFace face = SignUtil.getBack(signBlock);
                                Block targetDir = signBlock.getRelative(face).getRelative(face);
                                chunk.getWorld().spawnArrow(targetDir.getLocation(),
                                        entity.getLocation().subtract(targetDir.getLocation()).add(0.5, 0.5,
                                                0.5).toVector(), 2.0f, 0.0f);
                                break;
                            }
                        }
                    }
                }
            }
        }
         */

        for (Entity aEntity : center.getWorld().getEntities())
            if (!aEntity.isDead() && aEntity.isValid() && type.is(aEntity)
                    && aEntity.getLocation().distanceSquared(center.getLocation()) <= radius * radius) {
                Block signBlock = getSign().getBlock();
                BlockFace face = SignUtil.getBack(signBlock);
                Block targetDir = signBlock.getRelative(face).getRelative(face);
                center.getWorld().spawnArrow(targetDir.getLocation(),
                        aEntity.getLocation().subtract(targetDir.getLocation()).add(0.5, 0.5,
                                0.5).toVector(), 2.0f, 0.0f);
                break;
            }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new SentryGun(getServer(), sign, this);
        }

        @Override
        public void verify(Sign sign) throws ICVerificationException {

            try {
                String line = sign.getLine(3);
                if (line != null && !line.contains("")) Integer.parseInt(line);
            } catch (Exception e) {
                throw new ICVerificationException("You need to give a radius in line four.");
            }
        }
    }
}
