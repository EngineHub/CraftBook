package com.sk89q.craftbook.gates.world;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.PoweredMinecart;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.util.EnumUtil;
import com.sk89q.craftbook.util.LocationUtil;

public class EntityCannon extends AbstractIC {

    private enum Type {
        PLAYER,
        MOB_HOSTILE,
        MOB_PEACEFUL,
        MOB_ANY,
        ANY,
        CART,
        CART_STORAGE,
        CART_POWERED,
        ITEM;

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
                case ITEM:
                    return entity instanceof Item;
                case ANY:
                    return true;
            }
            return false;
        }

        public static Type fromString(String name) {

            return EnumUtil.getEnumFromString(EntityCannon.Type.class, name);
        }
    }

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

    /**
     * Returns true if the entity was damaged.
     *
     * @return
     */
    protected boolean shoot() {

        Location location = BukkitUtil.toSign(getSign()).getLocation();
        Type type = Type.MOB_HOSTILE;

        if (getSign().getLine(3).length() != 0) {
            type = Type.fromString(getSign().getLine(3));
        }

        try {
            for (Entity e : LocationUtil.getNearbyEntities(location, 3)) {
                if (e.isDead() || !e.isValid()) {
                    continue;
                }
                if (!type.is(e)) {
                    continue;
                }

                double x, y, z;

                x = Double.parseDouble(getSign().getLine(2).split(":")[0]);
                y = Double.parseDouble(getSign().getLine(2).split(":")[1]);
                z = Double.parseDouble(getSign().getLine(2).split(":")[2]);

                e.setVelocity(new Vector(x,y,z).add(e.getVelocity()));

                return true;
            }
        } catch (Exception ignored) {
        }

        return false;
    }


    public static class Factory extends AbstractICFactory implements
    RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new EntityCannon(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Shoots nearby entities of type at set velocity.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "velocity x:y:z",
                    "mob type"
            };
            return lines;
        }
    }
}