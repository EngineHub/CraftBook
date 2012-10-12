package com.sk89q.craftbook.cart;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.RailUtil;
import com.sk89q.craftbook.RedstoneUtil.Power;

public class CartDeposit extends CartMechanism {

    @Override
    public void impact(Minecart cart, CartMechanismBlocks blocks, boolean minor) {
        // validate
        if (cart == null) return;

        // care?
        if (minor) return;
        if (!(cart instanceof StorageMinecart)) return;
        Inventory cartinventory = ((StorageMinecart) cart).getInventory();

        // enabled?
        if (Power.OFF == isActive(blocks.rail, blocks.base, blocks.sign)) return;

        // collect/deposit set?
        if (blocks.sign == null) return;
        if (!blocks.matches("collect") && !blocks.matches("deposit")) return;
        boolean collecting = blocks.matches("collect");

        // search for containers
        ArrayList<Chest> containers = RailUtil.getNearbyChests(blocks.base);

        // are there any containers?
        if (containers.isEmpty()) return;

        // go
        ItemStack[] trivialstackarray = {};
        ArrayList<ItemStack> leftovers = new ArrayList<ItemStack>();

        int itemID = -1;
        byte itemData = -1;
        try {
            itemID = Integer.parseInt(((Sign) blocks.sign.getState()).getLine(2).split(":")[0]);
            itemData = Byte.parseByte(((Sign) blocks.sign.getState()).getLine(2).split(":")[1]);
        }
        catch(Exception e){}

        if(itemID < 0)
            return;

        if (collecting) {
            // collecting
            ArrayList<ItemStack> transferitems = new ArrayList<ItemStack>();
            if (((Sign) blocks.sign.getState()).getLine(2).length() > 0)
                for (ItemStack item : cartinventory.getContents()) {
                    if (item == null) continue;
                    if (itemID == item.getTypeId()) if(itemData < 0 || itemData == item.getDurability()) {
                        transferitems.add(new ItemStack(item.getTypeId(), item.getAmount(), item.getDurability()));
                        cartinventory.remove(item);
                    }
                }
            else {
                transferitems.addAll(Arrays.asList(cartinventory.getContents()));
                cartinventory.clear();
            }

            while (transferitems.remove(null)) continue;

            // is cart non-empty?
            if (transferitems.size() <= 0) return;

            //System.out.println("collecting " + transferitems.size() + " item stacks");
            //for (ItemStack stack: transferitems) System.out.println("collecting " + stack.getAmount() + " items of
            // type " + stack.getType().toString());

            for (Chest container : containers) {
                if (transferitems.size() <= 0) break;
                Inventory containerinventory = container.getInventory();

                leftovers.addAll(containerinventory.addItem(transferitems.toArray(trivialstackarray))
                        .values());
                transferitems.clear();
                transferitems.addAll(leftovers);
                leftovers.clear();

                container.update();
            }

            //System.out.println("collected items. " + transferitems.size() + " stacks left over.");

            leftovers.addAll(cartinventory.addItem(transferitems.toArray(trivialstackarray)).values());
            transferitems.clear();
            transferitems.addAll(leftovers);
            leftovers.clear();

            //System.out.println("collection done. " + transferitems.size() + " stacks wouldn't fit back.");
        } else {
            // depositing
            ArrayList<ItemStack> transferitems = new ArrayList<ItemStack>();

            for (Chest container : containers) {
                Inventory containerinventory = container.getInventory();
                if (((Sign) blocks.sign.getState()).getLine(2).length() > 0)
                    for (ItemStack item : containerinventory.getContents()) {
                        if (item == null) continue;
                        if (itemID == item.getTypeId()) if(itemData < 0 || itemData == item.getDurability()) {
                            transferitems.add(new ItemStack(item.getTypeId(), item.getAmount(), item.getDurability()));
                            containerinventory.remove(item);
                        }
                    }
                else {
                    transferitems.addAll(Arrays.asList(containerinventory.getContents()));
                    containerinventory.clear();
                }
                container.update();
            }

            while (transferitems.remove(null)) {
            }

            // are chests empty?
            if (transferitems.size() <= 0) return;

            //System.out.println("depositing " + transferitems.size() + " stacks");
            //for (ItemStack stack: transferitems) System.out.println("depositing " + stack.getAmount() + " items of
            // type " + stack.getType().toString());

            leftovers.addAll(cartinventory.addItem(transferitems.toArray(trivialstackarray)).values());
            transferitems.clear();
            transferitems.addAll(leftovers);
            leftovers.clear();

            //System.out.println("deposited, " + transferitems.size() + " items left over.");

            for (Chest container : containers) {
                if (transferitems.size() <= 0) break;
                Inventory containerinventory = container.getInventory();

                leftovers.addAll(containerinventory.addItem(transferitems.toArray(trivialstackarray))
                        .values());
                transferitems.clear();
                transferitems.addAll(leftovers);
                leftovers.clear();
            }

            //System.out.println("deposit done. " + transferitems.size() + " items wouldn't fit back.");
        }
    }

    @Override
    public void enter(Minecart cart, Entity entity, CartMechanismBlocks blocks,
            boolean minor) {

    }
}