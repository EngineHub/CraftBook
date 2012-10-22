package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.SignUtil;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

/**
 * @author Me4502
 */
public class ContainerCollector extends AbstractIC {

    public ContainerCollector(Server server, Sign sign, ICFactory factory) {

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

    protected boolean collect() {

        Block b = SignUtil.getBackBlock(getSign().getBlock());

        int x = b.getX();
        int y = b.getY() + 1;
        int z = b.getZ();
        Block bl = getSign().getBlock().getWorld().getBlockAt(x, y, z);
        for (Entity en : getSign().getChunk().getEntities()) {
            if (!(en instanceof Item)) {
                continue;
            }
            Item item = (Item) en;
            if (!ItemUtil.isStackValid(item.getItemStack()) || item.isDead() || !item.isValid()) {
                continue;
            }
            int ix = item.getLocation().getBlockX();
            int iy = item.getLocation().getBlockY();
            int iz = item.getLocation().getBlockZ();
            if (ix == getSign().getX() && iy == getSign().getY() && iz == getSign().getZ()) {

                // Create two test stacks to check against
                ItemStack[] testStacks = new ItemStack[] {null, null};

                // Create test stack #1
                try {
                    if (getSign().getLine(2).contains(":")) {
                        int id = Integer.parseInt(getSign().getLine(2).split(":")[0]);
                        int data = Integer.parseInt(getSign().getLine(2).split(":")[1]);
                        testStacks[0] = new ItemStack(id, 0, (short) data);
                    } else {
                        int id = Integer.parseInt(getSign().getLine(2));
                        testStacks[1] = new ItemStack(id, 1, (short) 0, (byte) 0);
                    }
                } catch (Exception ignored) {
                }

                // Create test stack #2
                try {
                    if (getSign().getLine(3).contains(":")) {
                        int id = Integer.parseInt(getSign().getLine(3).split(":")[0]);
                        int data = Integer.parseInt(getSign().getLine(3).split(":")[1]);
                        testStacks[1] = new ItemStack(id, 0, (short) data);
                    } else {
                        int id = Integer.parseInt(getSign().getLine(2));
                        testStacks[1] = new ItemStack(id, 1, (short) 0, (byte) 0);
                    }
                } catch (Exception ignored) {
                }

                // Check to see if it matches either test stack, if not stop
                if (testStacks[0] != null) if (ItemUtil.areItemsIdentical(testStacks[0], item.getItemStack())) {
                    continue;
                }
                if (testStacks[1] != null) if (!ItemUtil.areItemsIdentical(testStacks[1], item.getItemStack())) {
                    continue;
                }

                //Add the items to a container, and destroy them.
                if (bl.getType() == Material.CHEST) if (((Chest) bl.getState()).getInventory().firstEmpty() != -1) {

                    ((Chest) bl.getState()).getInventory().addItem(item.getItemStack());
                    item.remove();
                    return true;
                }

                if (bl.getType() == Material.DISPENSER)
                    if (((Dispenser) bl.getState()).getInventory().firstEmpty() != -1) {

                        ((Dispenser) bl.getState()).getInventory().addItem(item.getItemStack());
                        item.remove();
                        return true;
                    }

                if (bl.getType() == Material.BREWING_STAND) {

                    if (!ItemUtil.isAPotionIngredient(item.getItemStack()))
                        return false;
                    if (((BrewingStand) bl.getState()).getInventory().getIngredient() == null
                            || ItemUtil.areItemsIdentical(((BrewingStand) bl.getState()).getInventory().getIngredient
                                    (), item.getItemStack())) {

                        if (((BrewingStand) bl.getState()).getInventory().getIngredient() == null) {
                            ((BrewingStand) bl.getState()).getInventory().setIngredient(item.getItemStack());
                        } else {
                            ItemUtil.addToStack(((BrewingStand) bl.getState()).getInventory().getIngredient(),
                                    item.getItemStack());
                        }
                        item.remove();
                        return true;
                    }
                }

                if (bl.getType() == Material.FURNACE || bl.getType() == Material.BURNING_FURNACE) {

                    Furnace fur = (Furnace) bl.getState();

                    if (ItemUtil.isFurnacable(item.getItemStack()) && (fur.getInventory().getSmelting() == null
                            || ItemUtil.areItemsIdentical(item.getItemStack(), fur.getInventory().getSmelting()))) {
                        if (fur.getInventory().getSmelting() == null) {
                            fur.getInventory().setSmelting(item.getItemStack());
                        } else {
                            ItemUtil.addToStack(((Furnace) bl.getState()).getInventory().getSmelting(),
                                    item.getItemStack());
                        }
                        item.remove();
                        return true;
                    }

                    if (ItemUtil.isAFuel(item.getItemStack()) && (fur.getInventory().getFuel() == null
                            || ItemUtil.areItemsIdentical(item.getItemStack(), fur.getInventory().getFuel()))) {
                        if (fur.getInventory().getFuel() == null) {
                            fur.getInventory().setFuel(item.getItemStack());
                        } else {
                            ItemUtil.addToStack(((Furnace) bl.getState()).getInventory().getFuel(),
                                    item.getItemStack());
                        }
                        item.remove();
                        return true;
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
        public IC create(Sign sign) {

            return new ContainerCollector(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Collects items into above chest.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "included id:data",
                    "excluded id:data"
            };
            return lines;
        }
    }
}