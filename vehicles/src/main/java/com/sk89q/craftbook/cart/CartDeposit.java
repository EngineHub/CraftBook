package com.sk89q.craftbook.cart;

import com.sk89q.craftbook.RedstoneUtil.Power;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CartDeposit extends CartMechanism {
    public void impact(Minecart cart, CartMechanismBlocks blocks, boolean minor) {
        // validate
        if (cart == null) return;
        
        // care?
        if (minor) return;
        if (!(cart instanceof StorageMinecart)) return;
        Inventory cartinventory = ((StorageMinecart)cart).getInventory();
        
        // enabled?
        if (Power.OFF == isActive(blocks.rail, blocks.base, blocks.sign)) return;
        
        // collect/deposit set?
        if (blocks.sign == null) return;
        if (!blocks.matches("collect") && !blocks.matches("deposit")) return;
        boolean collecting = blocks.matches("collect");
        
        // search for containers
        ArrayList<Chest> containers = new ArrayList<Chest>();
        Block body = blocks.base;
        int x = body.getX();
        int y = body.getY();
        int z = body.getZ();
        if (body.getWorld().getBlockAt(x, y, z).getType() == Material.CHEST) containers.add((Chest)body.getWorld().getBlockAt(x, y, z).getState());
        if (body.getWorld().getBlockAt(x - 1, y, z).getType() == Material.CHEST) {
            containers.add((Chest)body.getWorld().getBlockAt(x - 1, y, z).getState());
            if (body.getWorld().getBlockAt(x - 2, y, z).getType() == Material.CHEST) containers.add((Chest)body.getWorld().getBlockAt(x - 2, y, z).getState());
        }
        if (body.getWorld().getBlockAt(x + 1, y, z).getType() == Material.CHEST) {
            containers.add((Chest)body.getWorld().getBlockAt(x + 1, y, z).getState());
            if (body.getWorld().getBlockAt(x + 2, y, z).getType() == Material.CHEST) containers.add((Chest)body.getWorld().getBlockAt(x + 2, y, z).getState());
        }
        if (body.getWorld().getBlockAt(x, y, z - 1).getType() == Material.CHEST) {
            containers.add((Chest)body.getWorld().getBlockAt(x, y, z - 1).getState());
            if (body.getWorld().getBlockAt(x, y, z - 2).getType() == Material.CHEST) containers.add((Chest)body.getWorld().getBlockAt(x, y, z - 2).getState());
        }
        if (body.getWorld().getBlockAt(x, y, z + 1).getType() == Material.CHEST) {
            containers.add((Chest)body.getWorld().getBlockAt(x, y, z + 1).getState());
            if (body.getWorld().getBlockAt(x, y, z + 2).getType() == Material.CHEST) containers.add((Chest)body.getWorld().getBlockAt(x, y, z + 2).getState());
        }
        
        // are there any containers?
        if (containers.isEmpty()) return;
        
        // go
        ItemStack[] trivialstackarray = {};
        ArrayList<ItemStack> leftovers = new ArrayList<ItemStack>();
        
        if (collecting) {
            // collecting
            ArrayList<ItemStack> transferitems = new ArrayList<ItemStack>(Arrays.asList(cartinventory.getContents()));
            cartinventory.clear();
            while (transferitems.remove(null)) continue;
            
            // is cart non-empty?
            if (transferitems.size() <= 0) return;
            
            //System.out.println("collecting " + transferitems.size() + " item stacks");
            //for (ItemStack stack: transferitems) System.out.println("collecting " + stack.getAmount() + " items of type " + stack.getType().toString());
            
            for (Chest container: containers) {
                if (transferitems.size() <= 0) break;
                Inventory containerinventory = container.getInventory();
                
                leftovers.addAll(containerinventory.addItem((ItemStack[]) transferitems.toArray(trivialstackarray)).values());
                transferitems.clear();
                transferitems.addAll(leftovers);
                leftovers.clear();
                
                container.update();
            }
            
            //System.out.println("collected items. " + transferitems.size() + " stacks left over.");
            
            leftovers.addAll(cartinventory.addItem((ItemStack[]) transferitems.toArray(trivialstackarray)).values());
            transferitems.clear();
            transferitems.addAll(leftovers);
            leftovers.clear();
            
            //System.out.println("collection done. " + transferitems.size() + " stacks wouldn't fit back.");
        } else {
            // depositing
            ArrayList<ItemStack> transferitems = new ArrayList<ItemStack>();
            
            for (Chest container: containers) {
                Inventory containerinventory = container.getInventory();
                transferitems.addAll(Arrays.asList(containerinventory.getContents()));
                containerinventory.clear();
                container.update();
            }
            
            while (transferitems.remove(null)) continue;
            
            // are chests empty?
            if (transferitems.size() <= 0) return;
            
            //System.out.println("depositing " + transferitems.size() + " stacks");
            //for (ItemStack stack: transferitems) System.out.println("depositing " + stack.getAmount() + " items of type " + stack.getType().toString());
            
            leftovers.addAll(cartinventory.addItem((ItemStack[]) transferitems.toArray(trivialstackarray)).values());
            transferitems.clear();
            transferitems.addAll(leftovers);
            leftovers.clear();
            
            //System.out.println("deposited, " + transferitems.size() + " items left over.");
            
            for (Chest container: containers) {
                if (leftovers.size() <= 0) break;
                Inventory containerinventory = container.getInventory();
                
                leftovers.addAll(containerinventory.addItem((ItemStack[]) transferitems.toArray(trivialstackarray)).values());
                transferitems.clear();
                transferitems.addAll(leftovers);
                leftovers.clear();
            }
            
            //System.out.println("deposit done. " + transferitems.size() + " items wouldn't fit back.");
        }
    }
}
