package com.sk89q.craftbook.mechanics.ic.gates.world.items;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.pipe.PipeRequestEvent;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.InventoryUtil;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.Vector;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

        if(chip.getInput(0)) return;

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
            if (bit.trim().length() > 0) {
                ItemStack item = ItemSyntax.getItem(bit);
                if (ItemUtil.isStackValid(item)) {
                    filters.add(ItemSyntax.getItem(bit));
                }
            }
        }

        chest = getBackBlock().getRelative(0, 1, 0);
    }

    public boolean collect() {

        boolean collected = false;

        for (Entity entity : LocationUtil.getNearbyEntities(centre, radius)) {
            if (entity.isValid() && entity instanceof Item) {
                if (LocationUtil.isWithinRadius(centre, entity.getLocation(), radius)) {

                    ItemStack stack = ((Item) entity).getItemStack();

                    if(!ItemUtil.isStackValid(stack))
                        return false;

                    boolean passed = filters.isEmpty() || !include;

                    for(ItemStack filter : filters) {
                        if(!ItemUtil.isStackValid(filter))
                            continue;

                        if(include && ItemUtil.areItemsIdentical(filter, stack)) {
                            passed = true;
                            break;
                        } else if(!include && ItemUtil.areItemsIdentical(filter, stack)) {
                            passed = false;
                            break;
                        }
                    }

                    if (!passed) {
                        continue;
                    }

                    BlockFace back = SignUtil.getBack(BukkitUtil.toSign(getSign()).getBlock());
                    Block pipe = getBackBlock().getRelative(back);

                    PipeRequestEvent event = new PipeRequestEvent(pipe, new ArrayList<ItemStack>(Collections.singletonList(stack)), getBackBlock());
                    Bukkit.getPluginManager().callEvent(event);

                    if (event.isCancelled()) {
                        continue;
                    }

                    if(event.getItems().isEmpty()) {
                        entity.remove();
                        return true;
                    }

                    if(!InventoryUtil.doesBlockHaveInventory(chest))
                        return false;

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

            return new String[] {"radius=x:y:z offset", "{-}id:data{,id:data}"};
        }
    }
}