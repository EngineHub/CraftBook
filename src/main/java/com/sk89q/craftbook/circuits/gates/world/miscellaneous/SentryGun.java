package com.sk89q.craftbook.circuits.gates.world.miscellaneous;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.ICVerificationException;
import com.sk89q.craftbook.circuits.ic.RestrictedIC;
import com.sk89q.craftbook.util.EntityType;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.worldedit.Vector;

public class SentryGun extends AbstractSelfTriggeredIC {

    private EntityType type;
    private Block center;
    private Vector radius;
    float speed;

    public SentryGun(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public void load() {

        speed = 0.8f;
        type = EntityType.fromString(getSign().getLine(2).split(":")[0]);
        if(type == null)
            type = EntityType.MOB_HOSTILE;
        if(getSign().getLine(2).split(":").length > 1)
            speed = Float.parseFloat(getSign().getLine(2).split(":")[1]);
        if(getLine(3).contains("="))
            center = ICUtil.parseBlockLocation(getSign(), 3);
        else
            center = getBackBlock().getRelative(0, 1, 0);
        radius = ICUtil.parseRadius(getSign(), 3);
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

    @Override
    public void think(ChipState state) {

        shoot();
    }

    public void shoot() {

        for (Entity ent : LocationUtil.getNearbyEntities(center.getLocation(), radius)) {
            if (type.is(ent)) {
                double yOff = 0;
                if(ent instanceof LivingEntity)
                    yOff = ((LivingEntity) ent).getEyeHeight();
                Arrow ar = center.getWorld().spawnArrow(center.getLocation().add(0.5,0.5,0.5), ent.getLocation().add(0, yOff, 0).subtract(center.getLocation().add(0.5,0.5,0.5)).toVector().normalize(), speed, 0);
                ar.setTicksLived(2500);
                break;
            }
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new SentryGun(getServer(), sign, this);
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            try {
                String line = sign.getLine(3);
                if (!line.isEmpty())
                    Integer.parseInt(line);
            } catch (Exception e) {
                throw new ICVerificationException("The radius is invalid!");
            }
        }

        @Override
        public String getShortDescription() {

            return "Shoots nearby mobs with arrows.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"Mob Type{:power}", "Radius=Offset"};
        }
    }
}
