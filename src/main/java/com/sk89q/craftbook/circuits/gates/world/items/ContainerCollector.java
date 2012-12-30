package com.sk89q.craftbook.circuits.gates.world.items;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.*;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.blocks.BlockID;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.*;

/**
 * @author Me4502
 */
public class ContainerCollector extends AbstractIC {

    public ContainerCollector(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Container Collector";
    }

    @Override
    public String getSignTitle() {

        return "CONTAINER COLLECT";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            chip.setOutput(0, collect());
        }
    }

    ItemStack doWant, doNotWant;

    @Override
    public void load() {

        doWant = ICUtil.getItem(getSign().getLine(2));
        doNotWant = ICUtil.getItem(getSign().getLine(3));
    }

    protected boolean collect() {

        Block b = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock());

        int x = b.getX();
        int y = b.getY() + 1;
        int z = b.getZ();
        Block bl = BukkitUtil.toSign(getSign()).getBlock().getWorld().getBlockAt(x, y, z);

        boolean collected = false;
        for (Entity en : BukkitUtil.toSign(getSign()).getChunk().getEntities()) {
            if (!(en instanceof Item)) {
                continue;
            }
            Item item = (Item) en;
            ItemStack stack = item.getItemStack();
            if (!ItemUtil.isStackValid(stack) || item.isDead() || !item.isValid()) {
                continue;
            }
            Location location = item.getLocation();
            int ix = location.getBlockX();
            int iy = location.getBlockY();
            int iz = location.getBlockZ();
            if (ix == getSign().getX() && iy == getSign().getY() && iz == getSign().getZ()) {

                // Check to see if it matches either test stack, if not stop
                if (doWant != null && !ItemUtil.areItemsIdentical(doWant, stack)) {
                    continue;
                }
                if (doNotWant != null && ItemUtil.areItemsIdentical(doNotWant, stack)) {
                    continue;
                }

                // Add the items to a container, and destroy them.
                if (addToContainer(bl, stack)) {
                    item.remove();
                    collected = true;
                }
            }
        }
        return collected;
    }

    private boolean addToContainer(Block bl, ItemStack stack) {

        int type = bl.getTypeId();
        if (type == BlockID.CHEST || type == BlockID.DISPENSER) {
            BlockState state = bl.getState();
            Inventory inventory = ((InventoryHolder) state).getInventory();
            if (inventory.firstEmpty() != -1) {
                inventory.addItem(stack);
                state.update();
                return true;
            }
        } else if (type == BlockID.BREWING_STAND) {

            if (!ItemUtil.isAPotionIngredient(stack)) return false;
            BrewingStand brewingStand = (BrewingStand) bl.getState();
            BrewerInventory inv = brewingStand.getInventory();
            if (fitsInSlot(stack, inv.getIngredient())) {
                if (inv.getIngredient() == null) {
                    inv.setIngredient(stack);
                } else {
                    ItemUtil.addToStack(inv.getIngredient(), stack);
                }
                brewingStand.update();
                return true;
            }
        } else if (type == BlockID.FURNACE || type == BlockID.BURNING_FURNACE) {

            Furnace furnace = (Furnace) bl.getState();
            FurnaceInventory inv = furnace.getInventory();

            if (ItemUtil.isFurnacable(stack) && fitsInSlot(stack, inv.getSmelting())) {
                if (inv.getSmelting() == null) {
                    inv.setSmelting(stack);
                } else {
                    ItemUtil.addToStack(inv.getSmelting(), stack);
                }
                furnace.update();
                return true;
            } else if (ItemUtil.isAFuel(stack) && fitsInSlot(stack, inv.getFuel())) {
                if (inv.getFuel() == null) {
                    inv.setFuel(stack);
                } else {
                    ItemUtil.addToStack(inv.getFuel(), stack);
                }
                furnace.update();
                return true;
            }
        }
        return false;
    }

    private static boolean fitsInSlot(ItemStack stack, ItemStack slot) {

        return slot == null || ItemUtil.areItemsIdentical(stack, slot);
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new ContainerCollector(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Collects items into above chest.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {"included id:data", "excluded id:data"};
            return lines;
        }
    }
}