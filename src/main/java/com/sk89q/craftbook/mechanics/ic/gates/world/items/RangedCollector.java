package com.sk89q.craftbook.mechanics.ic.gates.world.items;

import com.google.common.collect.Lists;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ranged.RangedCollectEvent;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.InventoryUtil;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.math.Vector3;
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

    private Vector3 radius;
    private Location centre;

    private boolean include = false;

    private Block chest;

    private List<ItemStack> filters = new ArrayList<>();

    @Override
    public void load() {

        radius = ICUtil.parseRadius(getSign());
        String radiusString = radius.getX() + "," + radius.getY() + "," + radius.getZ();
        if(radius.getX() == radius.getY() && radius.getY() == radius.getZ())
            radiusString = String.valueOf(radius.getX());
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

        List<Item> itemsForChest = Lists.newArrayList();

        for (Entity entity : LocationUtil.getNearbyEntities(centre, radius)) {
            if (entity.isValid() && entity instanceof Item && ((Item) entity).getPickupDelay() < 1) {
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

                BlockFace back = SignUtil.getBack(CraftBookBukkitUtil.toSign(getSign()).getBlock());
                Block pipe = getBackBlock().getRelative(back);

                RangedCollectEvent event = new RangedCollectEvent(pipe, (Item) entity, new ArrayList<>(Collections.singletonList(stack)), getBackBlock());
                Bukkit.getPluginManager().callEvent(event);

                if (event.isCancelled()) {
                    continue;
                }

                if(event.getItems().isEmpty()) {
                    entity.remove();
                    return true;
                }

                itemsForChest.add((Item) entity);
            }
        }

        if (!itemsForChest.isEmpty()) {
            if(!InventoryUtil.doesBlockHaveInventory(chest))
                return false;

            InventoryHolder chestState = (InventoryHolder) chest.getState();

            // Add the items to a container, and destroy them.
            for (Item entity : itemsForChest) {
                ItemStack stack = entity.getItemStack();
                List<ItemStack> leftovers = InventoryUtil.addItemsToInventory(chestState, false, stack);
                if (leftovers.isEmpty()) {
                    entity.remove();
                } else {
                    if (ItemUtil.areItemsIdentical(leftovers.get(0), stack) && leftovers.get(0).getAmount() != stack.getAmount()) {
                        entity.setItemStack(leftovers.get(0));
                    }
                }
                collected = true;
            }

            //if (collected) {
            //    chestState.update();
            //}
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