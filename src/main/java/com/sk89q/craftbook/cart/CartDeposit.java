package com.sk89q.craftbook.cart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.RailUtil;
import com.sk89q.craftbook.util.RedstoneUtil.Power;
import com.sk89q.craftbook.util.RegexUtil;

public class CartDeposit extends CartMechanism {

    @Override
    public void impact(Minecart cart, CartMechanismBlocks blocks, boolean minor) {
        // validate
        if (cart == null) return;

        // care?
        if (minor) return;
        if (!(cart instanceof StorageMinecart)) return;

        // enabled?
        if (Power.OFF == isActive(blocks.rail, blocks.base, blocks.sign)) return;

        // collect/deposit set?
        if (blocks.sign == null) return;
        if (!blocks.matches("collect") && !blocks.matches("deposit")) return;
        boolean collecting = blocks.matches("collect");

        // go
        List<ItemInfo> items = new ArrayList<ItemInfo>();
        for(String data : RegexUtil.COMMA_PATTERN.split(((Sign) blocks.sign.getState()).getLine(2))) {
            int itemID = -1;
            byte itemData = -1;
            try {
                String[] splitLine = RegexUtil.COLON_PATTERN.split(data);
                itemID = Integer.parseInt(splitLine[0]);
                if(splitLine.length > 1)
                    itemData = Byte.parseByte(splitLine[1]);
            } catch (Exception ignored) {
                continue;
            }

            items.add(new ItemInfo(itemID, itemData));
        }

        Inventory cartinventory = ((StorageMinecart) cart).getInventory();
        ArrayList<ItemStack> leftovers = new ArrayList<ItemStack>();

        // search for containers
        List<Chest> containers = new ArrayList<Chest>(RailUtil.getNearbyChests(blocks.base));
        containers.addAll(RailUtil.getNearbyChests(blocks.rail));

        // are there any containers?
        if (containers.isEmpty()) return;

        if (collecting) {
            // collecting
            ArrayList<ItemStack> transferItems = new ArrayList<ItemStack>();
            if (!items.isEmpty()) {
                for (ItemStack item : cartinventory.getContents()) {
                    if (!ItemUtil.isStackValid(item))
                        continue;
                    for(ItemInfo inf : items) {
                        if (inf.getId() < 0 || inf.getId() == item.getTypeId()) {
                            if (inf.getData() < 0 || inf.getData() == item.getDurability()) {
                                transferItems.add(item.clone());
                                cartinventory.remove(item);
                            }
                        }
                    }
                }
            } else {
                transferItems.addAll(Arrays.asList(cartinventory.getContents()));
                cartinventory.clear();
            }

            transferItems.removeAll(Collections.singleton(null));

            // is cart non-empty?
            if (transferItems.isEmpty()) return;

            CraftBookPlugin.logDebugMessage("collecting " + transferItems.size() + " item stacks", "cart-deposit.collect");
            for (ItemStack stack: transferItems)
                CraftBookPlugin.logDebugMessage("collecting " + stack.getAmount() + " items of type " + stack.getType().toString(), "cart-deposit.collect");

            for (Chest container : containers) {
                if (transferItems.isEmpty()) {
                    break;
                }
                Inventory containerinventory = container.getInventory();

                leftovers.addAll(containerinventory.addItem(transferItems.toArray(new ItemStack[transferItems.size()
                                                                                                ])).values());
                transferItems.clear();
                transferItems.addAll(leftovers);
                leftovers.clear();

                container.update();
            }

            CraftBookPlugin.logDebugMessage("collected items. " + transferItems.size() + " stacks left over.", "cart-deposit.collect");

            leftovers.addAll(cartinventory.addItem(transferItems.toArray(new ItemStack[transferItems.size()])).values());
            transferItems.clear();
            transferItems.addAll(leftovers);
            leftovers.clear();

            CraftBookPlugin.logDebugMessage("collection done. " + transferItems.size() + " stacks wouldn't fit back.", "cart-deposit.collect");
        } else {
            // depositing
            ArrayList<ItemStack> transferitems = new ArrayList<ItemStack>();

            for (Chest container : containers) {
                Inventory containerinventory = container.getInventory();
                if (!items.isEmpty()) {
                    for (ItemStack item : containerinventory.getContents()) {
                        if (!ItemUtil.isStackValid(item))
                            continue;
                        for(ItemInfo inf : items) {
                            if (inf.getId() < 0 || inf.getId() == item.getTypeId())
                                if (inf.getData() < 0 || inf.getData() == item.getDurability()) {
                                    transferitems.add(item.clone());
                                    containerinventory.remove(item);
                                }
                        }
                    }
                } else {
                    transferitems.addAll(Arrays.asList(containerinventory.getContents()));
                    containerinventory.clear();
                }
                container.update();
            }

            transferitems.removeAll(Collections.singleton(null));

            // are chests empty?
            if (transferitems.isEmpty()) return;

            CraftBookPlugin.logDebugMessage("depositing " + transferitems.size() + " stacks", "cart-deposit.deposit");
            for (ItemStack stack: transferitems)
                CraftBookPlugin.logDebugMessage("depositing " + stack.getAmount() + " items oftype " + stack.getType().toString(), "cart-deposit.deposit");

            leftovers.addAll(cartinventory.addItem(transferitems.toArray(new ItemStack[transferitems.size()])).values());
            transferitems.clear();
            transferitems.addAll(leftovers);
            leftovers.clear();

            CraftBookPlugin.logDebugMessage("deposited, " + transferitems.size() + " items left over.", "cart-deposit.deposit");

            for (Chest container : containers) {
                if (transferitems.isEmpty()) {
                    break;
                }
                Inventory containerinventory = container.getInventory();

                leftovers.addAll(containerinventory.addItem(transferitems.toArray(new ItemStack[transferitems.size()
                                                                                                ])).values());
                transferitems.clear();
                transferitems.addAll(leftovers);
                leftovers.clear();
            }

            CraftBookPlugin.logDebugMessage("deposit done. " + transferitems.size() + " items wouldn't fit back.", "cart-deposit.deposit");
        }
    }

    @Override
    public String getName() {

        return "Deposit";
    }

    @Override
    public String[] getApplicableSigns() {

        return new String[] {"Collect", "Deposit"};
    }
}