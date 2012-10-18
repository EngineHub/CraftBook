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
    private int amount = 1;
    private Block center;

    public CreatureSpawner(Server server, Sign sign, ICFactory factory) {

        super(server, sign, factory);
        load();
    }

    private void load() {

        try {
            entityType = EntityType.fromName(getSign().getLine(2).trim());
            String line = getSign().getLine(3).trim();
            // parse the amount or rider type
            try {
                String[] entityInf = line.split(":");
                data = entityInf[0];
                amount = Integer.parseInt(entityInf[1]);
            } catch (Exception e) {
                data = line;
            }
            center = SignUtil.getBackBlock(getSign().getBlock());
        } catch (Exception ignored) {
        }
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

        if (chip.getInput(0)) if (entityType != null && entityType.isAlive()) {
            Location center = LocationUtil.getCenterOfBlock(LocationUtil.getNextFreeSpace(this.center,
                    BlockFace.UP));
            // spawn amount of mobs
            for (int i = 0; i < amount; i++) {
                Entity entity = center.getWorld().spawnEntity(center, entityType);
                setEntityData(entity, data);
            }
        }
    }

    public void setEntityData(Entity ent, String data) {

        switch (ent.getType()) {
            case CREEPER:
                if (data.equalsIgnoreCase("charged")) {
                    ((Creeper) ent).setPowered(true);
                }
                break;
            case SLIME:
                if (data.equalsIgnoreCase("huge")) {
                    ((Slime) ent).setSize(16);
                } else if (data.equalsIgnoreCase("large")) {
                    ((Slime) ent).setSize(11);
                } else if (data.equalsIgnoreCase("normal")) {
                    ((Slime) ent).setSize(6);
                } else if (data.equalsIgnoreCase("small")) {
                    ((Slime) ent).setSize(3);
                }
                break;
            case MAGMA_CUBE:
                if (data.equalsIgnoreCase("huge")) {
                    ((MagmaCube) ent).setSize(16);
                } else if (data.equalsIgnoreCase("large")) {
                    ((MagmaCube) ent).setSize(11);
                } else if (data.equalsIgnoreCase("normal")) {
                    ((MagmaCube) ent).setSize(6);
                } else if (data.equalsIgnoreCase("small")) {
                    ((MagmaCube) ent).setSize(3);
                }
                break;
            case WOLF:
                if (data.equalsIgnoreCase("tame")) {
                    ((Wolf) ent).setTamed(true);
                } else if (data.equalsIgnoreCase("angry")) {
                    ((Wolf) ent).setAngry(true);
                }
                break;
            case PIG_ZOMBIE:
                if (data.equalsIgnoreCase("angry")) {
                    ((PigZombie) ent).setAngry(true);
                }
                break;
            case VILLAGER:
                if (data.equalsIgnoreCase("butcher")) {
                    ((Villager) ent).setProfession(Villager.Profession.BUTCHER);
                } else if (data.equalsIgnoreCase("smith")) {
                    ((Villager) ent).setProfession(Villager.Profession.BLACKSMITH);
                } else if (data.equalsIgnoreCase("priest")) {
                    ((Villager) ent).setProfession(Villager.Profession.PRIEST);
                } else if (data.equalsIgnoreCase("library")) {
                    ((Villager) ent).setProfession(Villager.Profession.LIBRARIAN);
                } else if (data.equalsIgnoreCase("farmer")) {
                    ((Villager) ent).setProfession(Villager.Profession.FARMER);
                }
                break;
            case SHEEP:
                if (data.equalsIgnoreCase("black")) {
                    ((Sheep) ent).setColor(DyeColor.BLACK);
                } else if (data.equalsIgnoreCase("red")) {
                    ((Sheep) ent).setColor(DyeColor.RED);
                } else if (data.equalsIgnoreCase("green")) {
                    ((Sheep) ent).setColor(DyeColor.GREEN);
                } else if (data.equalsIgnoreCase("brown")) {
                    ((Sheep) ent).setColor(DyeColor.BROWN);
                } else if (data.equalsIgnoreCase("blue")) {
                    ((Sheep) ent).setColor(DyeColor.BLUE);
                } else if (data.equalsIgnoreCase("purple")) {
                    ((Sheep) ent).setColor(DyeColor.PURPLE);
                } else if (data.equalsIgnoreCase("cyan")) {
                    ((Sheep) ent).setColor(DyeColor.CYAN);
                } else if (data.equalsIgnoreCase("silver")) {
                    ((Sheep) ent).setColor(DyeColor.SILVER);
                } else if (data.equalsIgnoreCase("gray")) {
                    ((Sheep) ent).setColor(DyeColor.GRAY);
                } else if (data.equalsIgnoreCase("pink")) {
                    ((Sheep) ent).setColor(DyeColor.PINK);
                } else if (data.equalsIgnoreCase("lime")) {
                    ((Sheep) ent).setColor(DyeColor.LIME);
                } else if (data.equalsIgnoreCase("yellow")) {
                    ((Sheep) ent).setColor(DyeColor.YELLOW);
                } else if (data.equalsIgnoreCase("lblue")) {
                    ((Sheep) ent).setColor(DyeColor.LIGHT_BLUE);
                } else if (data.equalsIgnoreCase("magenta")) {
                    ((Sheep) ent).setColor(DyeColor.MAGENTA);
                } else if (data.equalsIgnoreCase("orange")) {
                    ((Sheep) ent).setColor(DyeColor.ORANGE);
                } else if (data.equalsIgnoreCase("white")) {
                    ((Sheep) ent).setColor(DyeColor.WHITE);
                }
                break;
            default:
                if (ent instanceof Animals && data.equalsIgnoreCase("baby")) {
                    ((Animals) ent).setBaby();
                }
                break;

        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new CreatureSpawner(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Spawns a mob with specified data.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "entitytype",
                    "data:amount"
            };
            return lines;
        }
    }
}
