package com.sk89q.craftbook.circuits.gates.world.items;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
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
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.worldedit.Vector;

public class RangedCollector extends AbstractSelfTriggeredIC {

    public RangedCollector (Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Ranged Collector";
    }

    @Override
    public String getSignTitle() {

        return "RANGED COLLECTOR";
    }

    @Override
    public void think (ChipState chip) {
        chip.setOutput(0, collect());
    }

    @Override
    public void trigger (ChipState chip) {
        if (chip.getInput(0))
            chip.setOutput(0, collect());
    }

    Vector radius;
    Location centre;

    boolean include = false;

    Block chest;

    List<ItemStack> filters = new ArrayList<ItemStack>();

    @Override
    public void load() {

        radius = ICUtil.parseRadius(getSign());
        String radiusString = radius.getBlockX() + "," + radius.getBlockY() + "," + radius.getBlockZ();
        if(radius.getBlockX() == radius.getBlockY() && radius.getBlockY() == radius.getBlockZ())
            radiusString = String.valueOf(radius.getBlockX());
        if (getLine(2).contains("=")) {
            getSign().setLine(2, radiusString + "=" + RegexUtil.EQUALS_PATTERN.split(getLine(2))[1]);
            centre = ICUtil.parseBlockLocation(getSign(), 2).getLocation();
        } else {
            getSign().setLine(2, radiusString);
            centre = getBackBlock().getLocation();
        }

        include = !getLine(3).startsWith("-");

        for(String bit : getLine(3).replace("-","").split(",")) {

            filters.add(ItemUtil.getItem(bit));
        }

        chest = getBackBlock().getRelative(0, 1, 0);
    }

    public boolean collect() {

        boolean collected = false;

        for (Entity entity : LocationUtil.getNearbyEntities(centre, radius)) {
            if (entity.isValid() && entity instanceof Item) {
                if (LocationUtil.isWithinRadius(centre, entity.getLocation(), radius)) {

                    stackCheck: {
                    ItemStack stack = ((Item) entity).getItemStack();

                    if(!ItemUtil.isStackValid(stack))
                        return false;

                    for(ItemStack filter : filters) {

                        if(!ItemUtil.isStackValid(filter))
                            continue;

                        if(include && !ItemUtil.areItemsIdentical(filter, stack))
                            break stackCheck;
                        else if(!include && ItemUtil.areItemsIdentical(filter, stack))
                            break stackCheck;
                    }

                    // Add the items to a container, and destroy them.
                    List<ItemStack> leftovers = InventoryUtil.addItemsToInventory((InventoryHolder)chest.getState(), stack);
                    if(leftovers.isEmpty()) {
                        entity.remove();
                        return true;
                    } else {
                        if(ItemUtil.areItemsIdentical(leftovers.get(0), stack) && leftovers.get(0).getAmount() != stack.getAmount()) {
                            ((Item) entity).setItemStack(leftovers.get(0));
                            return true;
                        }
                    }
                }
                }
            }
        }

        return collected;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new RangedCollector(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Collects items at a range into above chest.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {"radius=x:y:z offset", "{-}id:data{,id:data}"};
            return lines;
        }
    }
}