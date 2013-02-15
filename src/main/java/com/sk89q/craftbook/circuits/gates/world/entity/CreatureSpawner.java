// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.circuits.gates.world.entity;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Slime;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.material.MaterialData;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractIC;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.ICVerificationException;
import com.sk89q.craftbook.circuits.ic.RestrictedIC;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.blocks.BlockID;

public class CreatureSpawner extends AbstractIC {

    EntityType type;
    String data;
    int amount;

    public CreatureSpawner(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public void load() {

        type = EntityType.fromName(getSign().getLine(2).trim().toLowerCase());
        if (type == null) {
            type = EntityType.PIG;
        }
        String line = getSign().getLine(3).trim();
        // parse the amount or rider type
        try {
            String[] entityInf = RegexUtil.ASTERISK_PATTERN.split(line, 2);
            data = entityInf[0];
            amount = Integer.parseInt(entityInf[1]);
        } catch (Exception e) {
            amount = 1;
            data = line;
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

        Block center = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock());

        if (chip.getInput(0)) if (center.getRelative(0, 1, 0).getTypeId() == BlockID.MOB_SPAWNER) {

            org.bukkit.block.CreatureSpawner sp = (org.bukkit.block.CreatureSpawner) center.getRelative(0, 1,
                    0).getState();
            sp.setCreatureTypeByName(type.getName());
            sp.update();
        } else {
            Location loc = LocationUtil.getCenterOfBlock(LocationUtil.getNextFreeSpace(center, BlockFace.UP));
            // spawn amount of mobs
            for (int i = 0; i < amount; i++) {
                Entity entity = loc.getWorld().spawn(loc, type.getEntityClass());
                setEntityData(entity, data);
            }
        }
    }

    public static void setEntityData(Entity ent, String bit) {

        String[] data = RegexUtil.COLON_PATTERN.split(bit);

        if (ent instanceof Ageable && data[0].equalsIgnoreCase("baby")) {
            ((Ageable) ent).setBaby();
        }

        if (ent instanceof Tameable && data[0].equalsIgnoreCase("tame")) {
            ((Tameable) ent).setTamed(true);
        }

        if (ent instanceof LivingEntity && data[0].equalsIgnoreCase("stay")) {
            ((LivingEntity) ent).setRemoveWhenFarAway(false);
        }

        if (ent instanceof LivingEntity && data[0].equalsIgnoreCase("despawn")) {
            ((LivingEntity) ent).setRemoveWhenFarAway(true);
        }

        if (ent instanceof LivingEntity && data[0].equalsIgnoreCase("pickup")) {
            ((LivingEntity) ent).setCanPickupItems(true);
        }

        if (ent instanceof Tameable && data[0].equalsIgnoreCase("owner")) {
            ((Tameable) ent).setOwner(Bukkit.getPlayer(data[1]));
        }

        if (ent instanceof Ageable && data[0].equalsIgnoreCase("babylock")) {
            ((Ageable) ent).setBaby();
            ((Ageable) ent).setAgeLock(true);
        }

        if (ent instanceof LivingEntity && data[0].equalsIgnoreCase("health")) {
            try {
                int health = Integer.parseInt(data[1]);
                ((LivingEntity) ent).setHealth(health);
            } catch (Exception ignored) {
            }
        }

        switch (ent.getType()) {
            case CREEPER:
                if (data[0].equalsIgnoreCase("charged")) {
                    ((Creeper) ent).setPowered(true);
                }
                break;
            case PIG:
                if (data[0].equalsIgnoreCase("saddle")) {
                    ((Pig) ent).setSaddle(true);
                }
                break;
            case SLIME:
                if (data[0].equalsIgnoreCase("huge")) {
                    ((Slime) ent).setSize(16);
                } else if (data[0].equalsIgnoreCase("large")) {
                    ((Slime) ent).setSize(11);
                } else if (data[0].equalsIgnoreCase("normal")) {
                    ((Slime) ent).setSize(6);
                } else if (data[0].equalsIgnoreCase("small")) {
                    ((Slime) ent).setSize(3);
                } else if (data[0].equalsIgnoreCase("size")) {
                    try {
                        int size = Integer.parseInt(data[1]);
                        ((Slime) ent).setSize(size);
                    } catch (Exception ignored) {
                    }
                }
                break;
            case MAGMA_CUBE:
                if (data[0].equalsIgnoreCase("huge")) {
                    ((MagmaCube) ent).setSize(16);
                } else if (data[0].equalsIgnoreCase("large")) {
                    ((MagmaCube) ent).setSize(11);
                } else if (data[0].equalsIgnoreCase("normal")) {
                    ((MagmaCube) ent).setSize(6);
                } else if (data[0].equalsIgnoreCase("small")) {
                    ((MagmaCube) ent).setSize(3);
                } else if (data[0].equalsIgnoreCase("size")) {
                    try {
                        int size = Integer.parseInt(data[1]);
                        ((MagmaCube) ent).setSize(size);
                    } catch (Exception ignored) {
                    }
                }
                break;
            case WOLF:
                if (data[0].equalsIgnoreCase("angry")) {
                    ((Wolf) ent).setAngry(true);
                } else if (data[0].equalsIgnoreCase("collar")) {
                    ((Wolf) ent).setCollarColor(DyeColor.valueOf(data[1]));
                }
                break;
            case SKELETON:
                if (data[0].equalsIgnoreCase("wither")) {
                    ((Skeleton) ent).setSkeletonType(SkeletonType.WITHER);
                }
                break;
            case ENDERMAN:
                if (data[0].equalsIgnoreCase("block")) {
                    try {
                        int id = Integer.parseInt(data[1]);
                        byte d = 0;
                        if (data.length > 2) d = Byte.parseByte(data[2]);
                        ((Enderman) ent).setCarriedMaterial(new MaterialData(id, d));
                    } catch (Exception ignored) {
                    }
                }
                break;
            case PRIMED_TNT:
                if (data[0].equalsIgnoreCase("fuse")) {
                    try {
                        int length = Integer.parseInt(data[1]);
                        ((TNTPrimed) ent).setFuseTicks(length);
                    } catch (Exception ignored) {
                    }
                } else if (data[0].equalsIgnoreCase("yield")) {
                    try {
                        float yield = Float.parseFloat(data[1]);
                        ((TNTPrimed) ent).setYield(yield);
                    } catch (Exception ignored) {
                    }
                } else if (data[0].equalsIgnoreCase("fire")) {
                    ((TNTPrimed) ent).setIsIncendiary(true);
                }
                break;
            case ARROW:
                if (data[0].equalsIgnoreCase("fire")) {
                    ent.setFireTicks(5000);
                }
                if (data[0].equalsIgnoreCase("bounce")) {
                    ((Arrow) ent).setBounce(true);
                }
                break;
            case OCELOT:
                if (data[0].replace("_CAT", "").replace("_OCELOT", "").equalsIgnoreCase("WILD")) {
                    ((Ocelot) ent).setCatType(Ocelot.Type.WILD_OCELOT);
                }
                if (data[0].replace("_CAT", "").replace("_OCELOT", "").equalsIgnoreCase("BLACK")) {
                    ((Ocelot) ent).setCatType(Ocelot.Type.BLACK_CAT);
                }
                if (data[0].replace("_CAT", "").replace("_OCELOT", "").equalsIgnoreCase("RED")) {
                    ((Ocelot) ent).setCatType(Ocelot.Type.RED_CAT);
                }
                if (data[0].replace("_CAT", "").replace("_OCELOT", "").equalsIgnoreCase("SIAMESE")) {
                    ((Ocelot) ent).setCatType(Ocelot.Type.SIAMESE_CAT);
                }
                break;
            case THROWN_EXP_BOTTLE:
                if (data[0].equalsIgnoreCase("bounce")) {
                    ((ThrownExpBottle) ent).setBounce(true);
                }
                break;
            case PIG_ZOMBIE:
                if (data[0].equalsIgnoreCase("angry")) {
                    ((PigZombie) ent).setAngry(true);
                }
                break;
            case VILLAGER:
                if (data[0].equalsIgnoreCase("butcher")) {
                    ((Villager) ent).setProfession(Villager.Profession.BUTCHER);
                } else if (data[0].equalsIgnoreCase("smith")) {
                    ((Villager) ent).setProfession(Villager.Profession.BLACKSMITH);
                } else if (data[0].equalsIgnoreCase("priest")) {
                    ((Villager) ent).setProfession(Villager.Profession.PRIEST);
                } else if (data[0].equalsIgnoreCase("library")) {
                    ((Villager) ent).setProfession(Villager.Profession.LIBRARIAN);
                } else if (data[0].equalsIgnoreCase("farmer")) {
                    ((Villager) ent).setProfession(Villager.Profession.FARMER);
                }
                break;
            case SHEEP:
                if (data[0].equalsIgnoreCase("black")) {
                    ((Sheep) ent).setColor(DyeColor.BLACK);
                } else if (data[0].equalsIgnoreCase("red")) {
                    ((Sheep) ent).setColor(DyeColor.RED);
                } else if (data[0].equalsIgnoreCase("green")) {
                    ((Sheep) ent).setColor(DyeColor.GREEN);
                } else if (data[0].equalsIgnoreCase("brown")) {
                    ((Sheep) ent).setColor(DyeColor.BROWN);
                } else if (data[0].equalsIgnoreCase("blue")) {
                    ((Sheep) ent).setColor(DyeColor.BLUE);
                } else if (data[0].equalsIgnoreCase("purple")) {
                    ((Sheep) ent).setColor(DyeColor.PURPLE);
                } else if (data[0].equalsIgnoreCase("cyan")) {
                    ((Sheep) ent).setColor(DyeColor.CYAN);
                } else if (data[0].equalsIgnoreCase("silver")) {
                    ((Sheep) ent).setColor(DyeColor.SILVER);
                } else if (data[0].equalsIgnoreCase("gray")) {
                    ((Sheep) ent).setColor(DyeColor.GRAY);
                } else if (data[0].equalsIgnoreCase("pink")) {
                    ((Sheep) ent).setColor(DyeColor.PINK);
                } else if (data[0].equalsIgnoreCase("lime")) {
                    ((Sheep) ent).setColor(DyeColor.LIME);
                } else if (data[0].equalsIgnoreCase("yellow")) {
                    ((Sheep) ent).setColor(DyeColor.YELLOW);
                } else if (data[0].equalsIgnoreCase("lblue")) {
                    ((Sheep) ent).setColor(DyeColor.LIGHT_BLUE);
                } else if (data[0].equalsIgnoreCase("magenta")) {
                    ((Sheep) ent).setColor(DyeColor.MAGENTA);
                } else if (data[0].equalsIgnoreCase("orange")) {
                    ((Sheep) ent).setColor(DyeColor.ORANGE);
                } else if (data[0].equalsIgnoreCase("white")) {
                    ((Sheep) ent).setColor(DyeColor.WHITE);
                }
                break;
            default:
                break;
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new CreatureSpawner(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Spawns a mob with specified data.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {"entitytype", "data*amount"};
            return lines;
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            if (EntityType.fromName(sign.getLine(2).trim().toLowerCase()) == null) {
                throw new ICVerificationException("Invalid Entity! See bukkit EntityType list!");
            } else if (!EntityType.fromName(sign.getLine(2).trim().toLowerCase()).isSpawnable()) {
                throw new ICVerificationException("Entity is not spawnable!");
            }
        }
    }
}
