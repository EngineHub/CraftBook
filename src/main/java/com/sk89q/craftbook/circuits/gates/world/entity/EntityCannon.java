package com.sk89q.craftbook.circuits.gates.world.entity;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.RestrictedIC;
import com.sk89q.craftbook.util.EntityType;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.RegexUtil;

public class EntityCannon extends AbstractSelfTriggeredIC {

    public EntityCannon(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Entity Cannon";
    }

    @Override
    public String getSignTitle() {

        return "ENTITY CANNON";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            chip.setOutput(0, shoot());
        }
    }

    @Override
    public void think(ChipState state) {

        state.setOutput(0, shoot());
    }

    double x,y,z;
    EntityType type;
    Location location;

    @Override
    public void load() {

        location = BukkitUtil.toSign(getSign()).getLocation();
        type = EntityType.MOB_HOSTILE;

        if (!getSign().getLine(3).isEmpty()) {
            type = EntityType.fromString(getSign().getLine(3));
        }

        try {
            String[] split = RegexUtil.COLON_PATTERN.split(getSign().getLine(2));
            x = Double.parseDouble(split[0]);
            y = Double.parseDouble(split[1]);
            z = Double.parseDouble(split[2]);
        }
        catch(Exception e) {
            x = 0;
            y = 1;
            z = 0;
        }
    }

    /**
     * This method launches near by entities
     *
     * @return true if a entity was thrown.
     */
    protected boolean shoot() {

        boolean resultBoolean = false;

        for (Entity e : LocationUtil.getNearbyEntities(location, BukkitUtil.toVector(new Vector(3,3,3)))) {

            if (e.isDead() || !e.isValid())
                continue;

            if (!type.is(e))
                continue;

            e.setVelocity(new Vector(x, y, z).add(e.getVelocity()));

            resultBoolean = true;
        }

        return resultBoolean;
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new EntityCannon(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Shoots nearby entities of type at set velocity.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {"velocity x:y:z", "mob type"};
            return lines;
        }
    }
}