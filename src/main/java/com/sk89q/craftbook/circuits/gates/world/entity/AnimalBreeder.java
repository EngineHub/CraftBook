package com.sk89q.craftbook.circuits.gates.world.entity;

import java.util.HashMap;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.InventoryUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.ItemID;

public class AnimalBreeder extends AbstractSelfTriggeredIC {

    public AnimalBreeder (Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    private Block center;
    private Vector radius;
    private Block chest;

    @Override
    public void load() {

        // if the line contains a = the offset is given
        // the given string should look something like that:
        // radius=x:y:z or radius, e.g. 1=-2:5:11
        radius = ICUtil.parseRadius(getSign());
        String radiusString = radius.getBlockX() + "," + radius.getBlockY() + "," + radius.getBlockZ();
        if(radius.getBlockX() == radius.getBlockY() && radius.getBlockY() == radius.getBlockZ())
            radiusString = String.valueOf(radius.getBlockX());
        if (getSign().getLine(2).contains("=")) {
            getSign().setLine(2, radiusString + "=" + RegexUtil.EQUALS_PATTERN.split(getSign().getLine(2))[1]);
            center = ICUtil.parseBlockLocation(getSign());
        } else {
            getSign().setLine(2, radiusString);
            center = getBackBlock();
        }

        chest = getBackBlock().getRelative(BlockFace.UP);
    }

    @Override
    public String getTitle () {
        return "Animal Breeder";
    }

    @Override
    public String getSignTitle () {
        return "ANIMAL BREEDER";
    }

    @Override
    public void trigger (ChipState chip) {

        if(chip.getInput(0))
            chip.setOutput(0, breed());
    }

    @Override
    public boolean isActive () {
        return true;
    }

    @Override
    public void think (ChipState chip) {

        if(chip.getInput(0))
            chip.setOutput(0, breed());
    }

    @Override
    public void unload() {
        if(lastEntity != null) {
            lastEntity.clear();
            lastEntity = null;
        }
    }

    HashMap<EntityType, Entity> lastEntity = new HashMap<EntityType, Entity>();

    public boolean breed() {

        if(lastEntity != null)
            lastEntity.clear();
        else
            lastEntity = new HashMap<EntityType, Entity>();
        InventoryHolder inv = null;

        if(chest.getState() instanceof InventoryHolder)
            inv = (InventoryHolder) chest.getState();

        if(inv == null)
            return false;

        for (Entity entity : LocationUtil.getNearbyEntities(center.getLocation(), radius)) {
            if (entity.isValid() && entity instanceof Ageable) {
                if(!((Ageable) entity).canBreed() || !canBreed(entity))
                    continue;
                // Check Radius
                if (LocationUtil.isWithinRadius(center.getLocation(), entity.getLocation(), radius)) {
                    if(breedAnimal(inv, entity))
                        return true;
                    else
                        lastEntity.put(entity.getType(), entity);
                }
            }
        }

        return false;
    }

    public boolean canBreed(Entity entity) {

        return entity instanceof Cow || entity instanceof Sheep || entity instanceof Pig || entity instanceof Chicken || entity instanceof Wolf;
    }

    public boolean breedAnimal(InventoryHolder inv, Entity entity) {

        if(lastEntity.get(entity.getType()) != null) {

            if (entity instanceof Cow || entity instanceof Sheep) {

                if(InventoryUtil.doesInventoryContain(inv.getInventory(), false, new ItemStack(ItemID.WHEAT, 2))) {

                    if(InventoryUtil.removeItemsFromInventory(inv, new ItemStack(ItemID.WHEAT, 2))) {
                        Ageable animal = (Ageable) entity.getWorld().spawnEntity(entity.getLocation(), entity.getType());
                        animal.setBaby();
                        ((Ageable) entity).setBreed(false);
                        return true;
                    }
                }
            } else if (entity instanceof Pig) {

                if(InventoryUtil.doesInventoryContain(inv.getInventory(), false, new ItemStack(ItemID.CARROT, 2))) {

                    if(InventoryUtil.removeItemsFromInventory(inv, new ItemStack(ItemID.CARROT, 2))) {
                        Ageable animal = (Ageable) entity.getWorld().spawnEntity(entity.getLocation(), entity.getType());
                        animal.setBaby();
                        ((Ageable) entity).setBreed(false);
                        return true;
                    }
                }
            } else if (entity instanceof Chicken) {
                if(InventoryUtil.doesInventoryContain(inv.getInventory(), false, new ItemStack(ItemID.SEEDS, 2))) {

                    if(InventoryUtil.removeItemsFromInventory(inv, new ItemStack(ItemID.SEEDS, 2))) {
                        Ageable animal = (Ageable) entity.getWorld().spawnEntity(entity.getLocation(), entity.getType());
                        animal.setBaby();
                        ((Ageable) entity).setBreed(false);
                        return true;
                    }
                }
            } else if (entity instanceof Wolf) {

                int[] validItems = new int[]{ItemID.RAW_CHICKEN, ItemID.COOKED_CHICKEN, ItemID.RAW_BEEF, ItemID.COOKED_BEEF, ItemID.RAW_PORKCHOP, ItemID.COOKED_PORKCHOP, ItemID.ROTTEN_FLESH};

                for(int item : validItems) {
                    if(InventoryUtil.doesInventoryContain(inv.getInventory(), false, new ItemStack(item, 2))) {
                        if(InventoryUtil.removeItemsFromInventory(inv, new ItemStack(item, 2))) {
                            Ageable animal = (Ageable) entity.getWorld().spawnEntity(entity.getLocation(), entity.getType());
                            animal.setBaby();
                            ((Ageable) entity).setBreed(false);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new AnimalBreeder(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Breeds nearby animals.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"+oradius=x:y:z offset", null};
        }
    }
}