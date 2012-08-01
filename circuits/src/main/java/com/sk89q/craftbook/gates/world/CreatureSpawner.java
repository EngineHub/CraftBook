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

import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.SignUtil;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.*;

public class CreatureSpawner extends AbstractIC {

    private EntityType entityType = EntityType.PIG;
    private String data;
    private boolean spawnData = false;
    private int amount = 1;
    private Block center;

    public CreatureSpawner(Server server, Sign sign) {

        super(server, sign);
        load();
    }

    private void load() {

        entityType = EntityType.fromName(getSign().getLine(2).trim());
        String line = getSign().getLine(3).trim();
        // parse the amount or rider type
        try {
            amount = Integer.parseInt(line);
        } catch (NumberFormatException e) {
            data = line;
            spawnData = !line.equals("");
        }
        center = SignUtil.getBackBlock(getSign().getBlock());
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
            if (entityType != null && entityType.isAlive()) {
                Location center = LocationUtil.getCenterOfBlock(LocationUtil.getNextFreeSpace(this.center,
                        BlockFace.UP));
                if (spawnData) {
                    // spawn the entity plus rider
                    Entity entity = center.getWorld().spawnEntity(center, entityType);
                    setEntityData(entity, data);
                } else {
                    // spawn amount of mobs
                    for (int i = 0; i < amount; i++) {
                        center.getWorld().spawnEntity(center, entityType);
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
                ((Villager) ent).setProfession(Villager.Profession.BUTCHER);
            if (data.equalsIgnoreCase("smith"))
                ((Villager) ent).setProfession(Villager.Profession.BLACKSMITH);
            if (data.equalsIgnoreCase("priest"))
                ((Villager) ent).setProfession(Villager.Profession.PRIEST);
            if (data.equalsIgnoreCase("library"))
                ((Villager) ent).setProfession(Villager.Profession.LIBRARIAN);
            if (data.equalsIgnoreCase("farmer"))
                ((Villager) ent).setProfession(Villager.Profession.FARMER);
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

            return new CreatureSpawner(getServer(), sign);
        }
    }
}
