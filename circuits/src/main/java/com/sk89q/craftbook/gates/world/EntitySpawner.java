// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.gates.world;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Sign;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Wolf;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.worldedit.blocks.BlockType;

public class EntitySpawner extends AbstractIC {

    public EntitySpawner(Server server, Sign sign) {

        super(server, sign);
    }

    @Override
    public String getTitle() {

        return "Creature Spawner";
    }

    @Override
    public String getSignTitle() {

        return "CREATURE SPAWNER";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            String type = getSign().getLine(2).trim().split(":")[0];
            String rider = getSign().getLine(3).trim().split(":")[0];
            String typeEffect = "", riderEffect = "";

            if (getSign().getLine(2).trim().split(":").length > 1)
                typeEffect = getSign().getLine(2).trim().split(":")[1];
            if (getSign().getLine(3).trim().split(":").length > 1)
                riderEffect = getSign().getLine(3).trim().split(":")[1];

            if (EntityType.fromName(type) != null) {
                Location loc = getSign().getBlock().getLocation();
                int maxY = Math.min(getSign().getWorld().getMaxHeight(), loc.getBlockY() + 10);
                int x = loc.getBlockX();
                int z = loc.getBlockZ();

                for (int y = loc.getBlockY() + 1; y <= maxY; y++) {
                    if (BlockType.canPassThrough(getSign().getWorld().getBlockTypeIdAt(x, y, z))) {
                        if (rider.length() != 0 && EntityType.fromName(rider) != null) {
                            Entity ent = getSign().getWorld().spawn(new Location(getSign().getWorld(), x, y, z),
                                    EntityType.fromName(type).getEntityClass());
                            Entity ent2 = getSign().getWorld().spawn(new Location(getSign().getWorld(), x, y, z),
                                    EntityType.fromName(rider).getEntityClass());
                            setEntityData(ent, typeEffect);
                            setEntityData(ent2, riderEffect);
                            ent.setPassenger(ent2);
                        } else {
                            Entity ent = getSign().getWorld().spawn(new Location(getSign().getWorld(), x, y, z),
                                    EntityType.fromName(type).getEntityClass());
                            setEntityData(ent, typeEffect);
                        }
                        return;
                    }
                }
            }
        }
    }

    public void setEntityData(Entity ent, String data) {

        if (data.equalsIgnoreCase("")) return;
        if (ent instanceof Animals) {
            if (data.equalsIgnoreCase("baby"))
                ((Animals) ent).setBaby();
        }
        if (ent instanceof Creeper) {
            if (data.equalsIgnoreCase("charged"))
                ((Creeper) ent).setPowered(true);
        }
        if (ent instanceof Slime) {
            if (data.equalsIgnoreCase("huge"))
                ((Slime) ent).setSize(16);
            if (data.equalsIgnoreCase("large"))
                ((Slime) ent).setSize(11);
            if (data.equalsIgnoreCase("normal"))
                ((Slime) ent).setSize(6);
            if (data.equalsIgnoreCase("small"))
                ((Slime) ent).setSize(3);
        }
        if (ent instanceof MagmaCube) {
            if (data.equalsIgnoreCase("huge"))
                ((MagmaCube) ent).setSize(16);
            if (data.equalsIgnoreCase("large"))
                ((MagmaCube) ent).setSize(11);
            if (data.equalsIgnoreCase("normal"))
                ((MagmaCube) ent).setSize(6);
            if (data.equalsIgnoreCase("small"))
                ((MagmaCube) ent).setSize(3);
        }
        if (ent instanceof Wolf) {
            if (data.equalsIgnoreCase("tame"))
                ((Wolf) ent).setTamed(true);
            if (data.equalsIgnoreCase("angry"))
                ((Wolf) ent).setAngry(true);
        }
        if (ent instanceof PigZombie) {
            if (data.equalsIgnoreCase("angry"))
                ((PigZombie) ent).setAngry(true);
        }
        if (ent instanceof Villager) {
            if (data.equalsIgnoreCase("butcher"))
                ((Villager) ent).setProfession(Profession.BUTCHER);
            if (data.equalsIgnoreCase("smith"))
                ((Villager) ent).setProfession(Profession.BLACKSMITH);
            if (data.equalsIgnoreCase("priest"))
                ((Villager) ent).setProfession(Profession.PRIEST);
            if (data.equalsIgnoreCase("library"))
                ((Villager) ent).setProfession(Profession.LIBRARIAN);
            if (data.equalsIgnoreCase("farmer"))
                ((Villager) ent).setProfession(Profession.FARMER);
        }
        if (ent instanceof Sheep) {
            if (data.equalsIgnoreCase("black"))
                ((Sheep) ent).setColor(DyeColor.BLACK);
            if (data.equalsIgnoreCase("red"))
                ((Sheep) ent).setColor(DyeColor.RED);
            if (data.equalsIgnoreCase("green"))
                ((Sheep) ent).setColor(DyeColor.GREEN);
            if (data.equalsIgnoreCase("brown"))
                ((Sheep) ent).setColor(DyeColor.BROWN);
            if (data.equalsIgnoreCase("blue"))
                ((Sheep) ent).setColor(DyeColor.BLUE);
            if (data.equalsIgnoreCase("purple"))
                ((Sheep) ent).setColor(DyeColor.PURPLE);
            if (data.equalsIgnoreCase("cyan"))
                ((Sheep) ent).setColor(DyeColor.CYAN);
            if (data.equalsIgnoreCase("silver"))
                ((Sheep) ent).setColor(DyeColor.SILVER);
            if (data.equalsIgnoreCase("gray"))
                ((Sheep) ent).setColor(DyeColor.GRAY);
            if (data.equalsIgnoreCase("pink"))
                ((Sheep) ent).setColor(DyeColor.PINK);
            if (data.equalsIgnoreCase("lime"))
                ((Sheep) ent).setColor(DyeColor.LIME);
            if (data.equalsIgnoreCase("yellow"))
                ((Sheep) ent).setColor(DyeColor.YELLOW);
            if (data.equalsIgnoreCase("lblue"))
                ((Sheep) ent).setColor(DyeColor.LIGHT_BLUE);
            if (data.equalsIgnoreCase("magenta"))
                ((Sheep) ent).setColor(DyeColor.MAGENTA);
            if (data.equalsIgnoreCase("orange"))
                ((Sheep) ent).setColor(DyeColor.ORANGE);
            if (data.equalsIgnoreCase("white"))
                ((Sheep) ent).setColor(DyeColor.WHITE);
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new EntitySpawner(getServer(), sign);
        }
    }
}