package com.sk89q.craftbook.circuits.gates.world.entity;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Entity;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.RestrictedIC;
import com.sk89q.craftbook.util.EntityType;
import com.sk89q.craftbook.util.EntityUtil;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.worldedit.Vector;

/**
 * @author Me4502
 */
public class EntityTrap extends AbstractSelfTriggeredIC {

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

    @Override
    public void think(ChipState chip) {

        chip.setOutput(0, hurt());
    }

    Vector radius;
    int damage;
    EntityType type;
    Location location;

    @Override
    public void load() {

        location = ICUtil.parseBlockLocation(getSign()).getLocation();
        radius = ICUtil.parseRadius(getSign());
        try {
            damage = Integer.parseInt(RegexUtil.EQUALS_PATTERN.split(getSign().getLine(2))[2]);
        } catch (Exception ignored) {
            damage = 2;
        }

        if (!getLine(3).isEmpty()) {
            try {
                type = EntityType.fromString(getSign().getLine(3));
            } catch(Exception e){
                type = EntityType.ANY;
            }
        } else
            type = EntityType.MOB_HOSTILE;

        if(type == null)
            type = EntityType.ANY;
    }

    /**
     * Returns true if the entity was damaged.
     *
     * @return
     */
    protected boolean hurt() {

        boolean hasHurt = false;

        for (Entity e : LocationUtil.getNearbyEntities(location, radius)) {

            if (!type.is(e))
                continue;

            EntityUtil.damageEntity(e, damage);
            hasHurt = true;
        }

        return hasHurt;
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new EntityTrap(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Damage nearby entities of type.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"radius=x:y:z=damage", "mob type"};
        }
    }
}