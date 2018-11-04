package com.sk89q.craftbook.mechanics.ic.gates.world.sensors;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.util.EntityType;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.worldedit.math.Vector3;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import java.util.Locale;
import java.util.Set;

/**
 * Movement Sensor. This IC is incomplete due to the bukkit API not providing ample movement velocity support.
 *
 * @author Me4502
 */
public class MovementSensor extends AbstractSelfTriggeredIC {

    public MovementSensor(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    private Set<EntityType> types;

    private Block center;
    private Vector3 radius;

    @Override
    public void load() {

        // lets get the types to detect first
        types = EntityType.getDetected(getSign().getLine(3).trim());

        // Add all if no params are specified
        if (types.isEmpty()) {
            types.add(EntityType.ANY);
        }

        getSign().setLine(3, getSign().getLine(3).toUpperCase(Locale.ENGLISH));

        // if the line contains a = the offset is given
        // the given string should look something like that:
        // radius=x:y:z or radius, e.g. 1=-2:5:11
        radius = ICUtil.parseRadius(getSign());
        String radiusString = radius.getX() + "," + radius.getY() + "," + radius.getZ();
        if(radius.getX() == radius.getY() && radius.getY() == radius.getZ())
            radiusString = String.valueOf(radius.getX());
        if (getSign().getLine(2).contains("=")) {
            getSign().setLine(2, radiusString + "=" + RegexUtil.EQUALS_PATTERN.split(getSign().getLine(2))[1]);
            center = ICUtil.parseBlockLocation(getSign());
        } else {
            getSign().setLine(2, radiusString);
            center = getBackBlock();
        }
        getSign().update(false);
    }

    @Override
    public String getTitle() {

        return "Movement Sensor";
    }

    @Override
    public String getSignTitle() {

        return "MOVING SENSOR";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) chip.setOutput(0, check());
    }

    @Override
    public void think(ChipState chip) {

        check();
    }

    public boolean check() {

        for (Entity entity : LocationUtil.getNearbyEntities(center.getLocation(), radius)) {
            if (entity.isValid()) {
                for (EntityType type : types) { // Check Type
                    if (type.is(entity)) { // Check Radius
                        if (LocationUtil.isWithinRadius(center.getLocation(), entity.getLocation(), radius)) {
                            if (entity.getVelocity().lengthSquared() >= 0.01) return true;
                        }
                        break;
                    }
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
        public String getShortDescription() {

            return "Outputs high if a nearby entity is moving.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"radius=x:y:z offset", "entity type"};
        }
    }
}