package com.sk89q.craftbook.circuits.gates.world.entity;

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

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractIC;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.RestrictedIC;
import com.sk89q.craftbook.util.EnumUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.worldedit.Vector;

public class EntityCannon extends AbstractIC {

    private enum Type {
        PLAYER, MOB_HOSTILE, MOB_PEACEFUL, MOB_ANY, ANY, CART, CART_STORAGE, CART_POWERED, ITEM;

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
     * This method launches near by entities
     *
     * @return true if a entity was thrown.
     */
    protected boolean shoot() {

        boolean resultBoolean = false;
        Location location = BukkitUtil.toSign(getSign()).getLocation();
        Type type = Type.MOB_HOSTILE;

        if (!getSign().getLine(3).isEmpty()) {
            type = Type.fromString(getSign().getLine(3));
        }

        try {
            for (Entity e : LocationUtil.getNearbyEntities(location, new Vector(3,3,3))) {
                if (e.isDead() || !e.isValid()) {
                    continue;
                }
                if (!type.is(e)) {
                    continue;
                }

                String[] split = RegexUtil.COLON_PATTERN.split(getSign().getLine(2));
                double x = Double.parseDouble(split[0]);
                double y = Double.parseDouble(split[1]);
                double z = Double.parseDouble(split[2]);

                e.setVelocity(new org.bukkit.util.Vector(x, y, z).add(e.getVelocity()));

                resultBoolean = true;
            }
        } catch (Exception ignored) {
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