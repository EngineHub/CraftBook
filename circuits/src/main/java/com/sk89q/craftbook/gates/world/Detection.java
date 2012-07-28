package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.EnumUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.SignUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.*;

import java.util.Set;

/**
 * @author Silthus
 */
public class Detection extends AbstractIC {

    private enum Type {
        PLAYER,
        MOB_HOSTILE,
        MOB_PEACEFUL,
        MOB_ANY,
        ANY,
        CART,
        CART_STORAGE,
        CART_POWERED;

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

            return EnumUtil.getEnumFromString(Detection.Type.class, name);
        }
    }

    private Type type;

    private Location center;
    private Set<Chunk> chunks;
    private int radius;

    public Detection(Server server, Sign block) {

        super(server, block);
        // lets set some defaults
        radius = 0;
        load();
    }

    private void load() {

        Sign sign = getSign();
        Block block = SignUtil.getBackBlock(sign.getBlock());
        // lets get the type to detect first
        this.type = Type.fromString(sign.getLine(3).trim());
        // set the type to any if wrong format
        if (type == null) this.type = Type.ANY;
        // update the sign with correct upper case name
        sign.setLine(3, type.name());
        sign.update();
        // now check the third line for the radius and offset
        String line = sign.getLine(2).trim();
        boolean relativeOffset = !line.contains("!");
        if (!relativeOffset) line.replace("!", "");
        // if the line contains a = the offset is given
        // the given string should look something like that:
        // radius=x:y:z or radius, e.g. 1=-2:5:11
        if (line.contains("=")) {
            try {
                String[] split = line.split("=");
                this.radius = Integer.parseInt(split[0]);
                // parse the offset
                String[] offsetSplit = split[1].split(":");
                int offsetX = Integer.parseInt(offsetSplit[0]);
                int offsetY = Integer.parseInt(offsetSplit[1]);
                int offsetZ = Integer.parseInt(offsetSplit[2]);
                if (relativeOffset) {
                    block = LocationUtil.getRelativeOffset(sign, offsetX, offsetY, offsetZ);
                } else {
                    block = LocationUtil.getOffset(block, offsetX, offsetY, offsetZ);
                }
            } catch (NumberFormatException e) {
                // do nothing and use the defaults
            } catch (IndexOutOfBoundsException e) {
                // do nothing and use the defaults
            }
        } else {
            this.radius = Integer.parseInt(line);
        }
        this.center = block.getLocation();
        this.chunks = LocationUtil.getSurroundingChunks(block, radius);
    }

    @Override
    public String getTitle() {

        return "Detection";
    }

    @Override
    public String getSignTitle() {

        return "DETECTION";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            chip.setOutput(0, isDetected());
        }
    }

    protected boolean isDetected() {

        for (Chunk chunk : this.chunks) {
            if (chunk.isLoaded()) {
                // get all entites from the chunks in the defined radius
                for (Entity entity : chunk.getEntities()) {
                    if (!entity.isDead()) {
                        if (type.is(entity)) {
                            // at last check if the entity is within the radius
                            if (LocationUtil.getGreatestDistance(entity.getLocation(), center) <= radius) {
                                return true;
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

            return new Detection(getServer(), sign);
        }
    }
}
