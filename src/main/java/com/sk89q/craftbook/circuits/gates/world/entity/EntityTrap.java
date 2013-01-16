package com.sk89q.craftbook.circuits.gates.world.entity;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractIC;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.RestrictedIC;
import com.sk89q.craftbook.util.EntityType;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.worldedit.Vector;

/**
 * @author Me4502
 */
public class EntityTrap extends AbstractIC {

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

    Vector radius;
    int damage;
    EntityType type;
    Location location;

    @Override
    public void load() {

        location = BukkitUtil.toSign(getSign()).getLocation();
        radius = ICUtil.parseRadius(getSign());
        try {
            String[] splitLine = RegexUtil.EQUALS_PATTERN.split(getSign().getLine(2), 3);
            if (getSign().getLine(2).contains("=")) {
                String[] pos = RegexUtil.COLON_PATTERN.split(splitLine[1]);
                int x = Integer.parseInt(pos[0]);
                int y = Integer.parseInt(pos[1]);
                int z = Integer.parseInt(pos[2]);
                location.add(x, y, z);

                damage = Integer.parseInt(splitLine[2]);
            } else damage = 2;
        } catch (Exception ignored) {
            damage = 2;
        }

        if (!getSign().getLine(3).isEmpty()) {
            type = EntityType.fromString(getSign().getLine(3));
        } else type = EntityType.MOB_HOSTILE;
    }

    /**
     * Returns true if the entity was damaged.
     *
     * @return
     */
    protected boolean hurt() {

        boolean hasHurt = false;

        for (Entity e : LocationUtil.getNearbyEntities(location, radius)) {
            if (e == null || e.isDead() || !e.isValid()) {
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

            String[] lines = new String[] {"radius=x:y:z=damage", "mob type"};
            return lines;
        }
    }
}