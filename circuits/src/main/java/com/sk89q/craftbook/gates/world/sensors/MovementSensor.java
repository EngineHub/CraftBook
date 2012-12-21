package com.sk89q.craftbook.gates.world.sensors;

import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.gates.world.sensors.EntitySensor.Type;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.ICUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.SignUtil;

/**
 * Movement Sensor.
 * 
 * This IC is incomplete due to the bukkit API not providing ample movement velocity support.
 * 
 * @author Me4502
 *
 */
public class MovementSensor extends AbstractIC {

    public MovementSensor (Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    private Set<Type> types;

    private Block center;
    private Set<Chunk> chunks;
    private int radius;

    @Override
    public void load() {

        // lets get the types to detect first
        types = Type.getDetected(getSign().getLine(3).trim());

        // Add all if no params are specified
        if (types.isEmpty()) {
            types.add(Type.ANY);
        }

        getSign().setLine(3, getSign().getLine(3).toUpperCase());

        // if the line contains a = the offset is given
        // the given string should look something like that:
        // radius=x:y:z or radius, e.g. 1=-2:5:11
        radius = ICUtil.parseRadius(getSign());
        if (getSign().getLine(2).contains("=")) {
            getSign().setLine(2, radius + "=" + ICUtil.EQUALS_PATTERN.split(getSign().getLine(2))[1]);
            center = ICUtil.parseBlockLocation(getSign());
        } else {
            getSign().setLine(2, String.valueOf(radius));
            center = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock());
        }
        chunks = LocationUtil.getSurroundingChunks(SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock()),
                radius); //Update chunks
        getSign().update(false);
    }

    @Override
    public String getTitle () {
        return "Movement Sensor";
    }

    @Override
    public String getSignTitle () {
        return "MOVING SENSOR";
    }

    @Override
    public void trigger (ChipState chip) {

        if(chip.getInput(0))
            chip.setOutput(0, check());
    }

    public boolean check() {

        for (Chunk chunk : chunks)
            if (chunk.isLoaded()) {
                for (Entity entity : chunk.getEntities())
                    if (entity.isValid()) {
                        for (Type type : types)
                            // Check Type
                            if (type.is(entity)) {
                                // Check Radius
                                if (LocationUtil.isWithinRadius(center.getLocation(), entity.getLocation(), radius)) {
                                    if(entity.getVelocity().lengthSquared() >= 0.01)
                                        return true;
                                }
                                break;
                            }
                    }
            }
        return false;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new MovementSensor(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Outputs high if a nearby entity is moving.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "radius=x:y:z offset",
                    "entity type"
            };
            return lines;
        }
    }
}