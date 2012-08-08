package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.EnumUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.SignUtil;
import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.*;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Silthus
 */
public class EntitySensor extends AbstractIC {

    private enum Type {
        PLAYER('P'),
        ITEM('I'),
        MOB_HOSTILE('H'),
        MOB_PEACEFUL('A'),
        MOB_ANY('M'),
        ANY('L'),
        CART('C'),
        CART_STORAGE('S'),
        CART_POWERED('E');

        public boolean is(Entity entity) {

            switch (this) {
                case PLAYER:
                    return entity instanceof Player;
                case ITEM:
                    return entity instanceof Item;
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

        private final char shortName;

        private Type(char shortName) {

            this.shortName = shortName;
        }

        public char getCharName() {

            return shortName;
        }

        public static HashSet<Type> getDetected(String line) {

            HashSet<Type> types = new HashSet<Type>();

            Type type = EnumUtil.getEnumFromString(Type.class, line);
            if (type != null) {
                types.add(type);
            } else {
                for (char aChar : line.toCharArray()) {
                    for (Type aType : Type.values()) {
                        if (aType.getCharName() == aChar) types.add(aType);
                    }
                }
            }

            if (types.size() == 0) types.add(ANY);

            return types;
        }
    }

    private HashSet<Type> types;

    private Block center;
    private Set<Chunk> chunks;
    private int radius;

    public EntitySensor(Server server, Sign block) {

        super(server, block);
        load();
    }

    private void load() {

        Sign sign = getSign();
        // lets get the types to detect first
        types = Type.getDetected(sign.getLine(3).trim());

        // Add all if no params are specified
        if (types.size() == 0) types.add(Type.ANY);

        sign.setLine(3, sign.getLine(3).toUpperCase());
        sign.update();

        // if the line contains a = the offset is given
        // the given string should look something like that:
        // radius=x:y:z or radius, e.g. 1=-2:5:11
        radius = ICUtil.parseRadius(getSign());
        if (getSign().getLine(2).contains("=")) {
            center = ICUtil.parseBlockLocation(getSign());
        } else {
            center = SignUtil.getBackBlock(getSign().getBlock());
        }
        chunks = LocationUtil.getSurroundingChunks(SignUtil.getBackBlock(sign.getBlock()), radius);
    }

    @Override
    public String getTitle() {

        return "Entity Sensor";
    }

    @Override
    public String getSignTitle() {

        return "ENTITY SENSOR";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            chip.setOutput(0, isDetected());
        }
    }

    protected boolean isDetected() {

        for (Chunk chunk : chunks) {
            if (chunk.isLoaded()) {
                // Get all entites from the chunks in the defined radius
                for (Entity entity : chunk.getEntities()) {
                    if (!entity.isDead() && entity.isValid()) {
                        for (Type type : types) {
                            // Check Type
                            if (type.is(entity)) {
                                // Check Radius
                                return LocationUtil.isWithinRadius(center.getLocation(), entity.getLocation(), radius);
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new EntitySensor(getServer(), sign);
        }

        @Override
        public void verify(Sign sign) throws ICVerificationException {

            ICUtil.verifySignSyntax(sign);
        }
    }
}
