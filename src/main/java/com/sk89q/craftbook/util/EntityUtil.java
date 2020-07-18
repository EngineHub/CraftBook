package com.sk89q.craftbook.util;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Slime;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;

import java.util.ArrayList;
import java.util.List;

public final class EntityUtil {

    /**
     * Checks if an entity is standing in a specific block.
     * 
     * @param entity The entity to check.
     * @param block The block to check.
     * @return Whether the entity is in the block or not.
     */
    public static boolean isEntityInBlock(Entity entity, Block block) {

        Location entLoc = entity.getLocation().getBlock().getLocation();
        int heightOffset = 0;

        if(entity instanceof LivingEntity) {
            heightOffset = (int) Math.floor(((LivingEntity) entity).getEyeHeight());
        }
        while(heightOffset >= 0) {
            if(entLoc.getBlockX() == block.getLocation().getBlockX())
                if(entLoc.getBlockY()+heightOffset == block.getLocation().getBlockY())
                    if(entLoc.getBlockZ() == block.getLocation().getBlockZ())
                        return true;
            heightOffset --;
        }

        return false;
    }

    public static boolean isEntityOfTypeInBlock(Block block, org.bukkit.entity.EntityType type) {

        for(Entity ent : block.getChunk().getEntities()) {

            if(ent.getType() != type) continue;
            if(isEntityInBlock(ent, block))
                return true;
        }

        return false;
    }

    /**
     * Kills an entity using the proper way for it's entity type.
     * 
     * @param ent The entity to kill.
     */
    public static void killEntity(Entity ent) {

        if(ent instanceof Damageable)
            ((Damageable) ent).damage(((Damageable) ent).getHealth());
        else
            ent.remove();
    }

    /**
     * Damages an entity using the proper way for it's entity type.
     * 
     * @param ent The entity to damage.
     * @param damage The amount to damage it by.
     */
    public static void damageEntity(Entity ent, double damage) {

        if(ent instanceof Damageable)
            ((Damageable) ent).damage(damage);
        else if (ent instanceof Minecart)
            ((Minecart) ent).setDamage(((Minecart) ent).getDamage() + damage);
        else
            ent.remove();
    }

    public static org.bukkit.entity.EntityType[] parseEntityList(List<String> list) {

        List<org.bukkit.entity.EntityType> ents = new ArrayList<>();
        for(String s : list)
            ents.add(org.bukkit.entity.EntityType.fromName(s));

        return ents.toArray(new org.bukkit.entity.EntityType[ents.size()]);
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

        if (ent instanceof LivingEntity && data[0].equalsIgnoreCase("name")) {
            ent.setCustomName(data[1]);
            ent.setCustomNameVisible(true);
        }

        if (ent instanceof Ageable && data[0].equalsIgnoreCase("babylock")) {
            ((Ageable) ent).setBaby();
            ((Ageable) ent).setAgeLock(true);
        }

        if (ent instanceof LivingEntity && data[0].equalsIgnoreCase("health")) {
            try {
                double health = Double.parseDouble(data[1]);
                if(((LivingEntity) ent).getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() < health)
                    ((LivingEntity) ent).getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
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
            case ENDERMAN:
                if (data[0].equalsIgnoreCase("block")) {
                    try {
                        StringBuilder bits = new StringBuilder(data[1]);
                        for (int i = 2; i < data.length; i++) {
                            bits.append(':').append(data[i]);
                        }
                        BlockStateHolder blockState = WorldEdit.getInstance().getBlockFactory().parseFromInput(bits.toString(), new ParserContext());
                        ((Enderman) ent).setCarriedBlock(BukkitAdapter.adapt(blockState));
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
                if (StringUtils.replace(StringUtils.replace(data[0], "_CAT", ""), "_OCELOT", "").equalsIgnoreCase("WILD")) {
                    ((Ocelot) ent).setCatType(Ocelot.Type.WILD_OCELOT);
                }
                if (StringUtils.replace(StringUtils.replace(data[0], "_CAT", ""), "_OCELOT", "").equalsIgnoreCase("BLACK")) {
                    ((Ocelot) ent).setCatType(Ocelot.Type.BLACK_CAT);
                }
                if (StringUtils.replace(StringUtils.replace(data[0], "_CAT", ""), "_OCELOT", "").equalsIgnoreCase("RED")) {
                    ((Ocelot) ent).setCatType(Ocelot.Type.RED_CAT);
                }
                if (StringUtils.replace(StringUtils.replace(data[0], "_CAT", ""), "_OCELOT", "").equalsIgnoreCase("SIAMESE")) {
                    ((Ocelot) ent).setCatType(Ocelot.Type.SIAMESE_CAT);
                }
                break;
            case THROWN_EXP_BOTTLE:
                if (data[0].equalsIgnoreCase("bounce")) {
                    ((ThrownExpBottle) ent).setBounce(true);
                }
                break;
            case ZOMBIFIED_PIGLIN:
                if (data[0].equalsIgnoreCase("angry")) {
                    ((PigZombie) ent).setAngry(true);
                }
                break;
            case VILLAGER:
                if (data[0].equalsIgnoreCase("butcher")) {
                    ((Villager) ent).setProfession(Villager.Profession.BUTCHER);
                } else if (data[0].equalsIgnoreCase("toolsmith")) {
                    ((Villager) ent).setProfession(Villager.Profession.TOOLSMITH);
                } else if (data[0].equalsIgnoreCase("wepsmith") || data[0].equalsIgnoreCase("smith")) {
                    ((Villager) ent).setProfession(Villager.Profession.WEAPONSMITH);
                } else if (data[0].equalsIgnoreCase("priest")) {
                    ((Villager) ent).setProfession(Villager.Profession.CLERIC);
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
                    ((Sheep) ent).setColor(DyeColor.LIGHT_GRAY);
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
            case HORSE:
                if (ent instanceof ChestedHorse && data[0].equalsIgnoreCase("chest"))
                    ((ChestedHorse)ent).setCarryingChest(true);
                else if (data[0].equalsIgnoreCase("domestic"))
                    try {
                        ((Horse)ent).setDomestication(Integer.parseInt(data[1]));
                    } catch(Exception e){}
                else if (data[0].equalsIgnoreCase("c")) {
                    if(data[1].equalsIgnoreCase("white"))
                        ((Horse)ent).setColor(Color.WHITE);
                    else if(data[1].equalsIgnoreCase("cream"))
                        ((Horse)ent).setColor(Color.CREAMY);
                    else if(data[1].equalsIgnoreCase("chestnut"))
                        ((Horse)ent).setColor(Color.CHESTNUT);
                    else if(data[1].equalsIgnoreCase("brown"))
                        ((Horse)ent).setColor(Color.BROWN);
                    else if(data[1].equalsIgnoreCase("dbrown"))
                        ((Horse)ent).setColor(Color.DARK_BROWN);
                    else if(data[1].equalsIgnoreCase("gray"))
                        ((Horse)ent).setColor(Color.GRAY);
                    else if(data[1].equalsIgnoreCase("black"))
                        ((Horse)ent).setColor(Color.BLACK);
                } else if (data[0].equalsIgnoreCase("m")) {
                    if(data[1].equalsIgnoreCase("none"))
                        ((Horse)ent).setStyle(Style.NONE);
                    else if(data[1].equalsIgnoreCase("white"))
                        ((Horse)ent).setStyle(Style.NONE);
                    else if(data[1].equalsIgnoreCase("milky"))
                        ((Horse)ent).setStyle(Style.WHITEFIELD);
                    else if(data[1].equalsIgnoreCase("wdots"))
                        ((Horse)ent).setStyle(Style.WHITE_DOTS);
                    else if(data[1].equalsIgnoreCase("bdots"))
                        ((Horse)ent).setStyle(Style.BLACK_DOTS);
                } else if (data[0].equalsIgnoreCase("strength"))
                    try {
                        ((Horse)ent).setJumpStrength(Double.parseDouble(data[1]));
                    } catch(Exception e){}
            default:
                break;
        }
    }
}