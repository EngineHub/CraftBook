package com.sk89q.craftbook.mechanics.ic.gates.world.sensors;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.ICVerificationException;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.InventoryUtil;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ItemUtil;

public class ContentsSensor extends AbstractSelfTriggeredIC {

    public ContentsSensor (Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    boolean checkAmount;

    @Override
    public void load() {

        checkAmount = getLine(2).contains("*");

        item = ItemSyntax.getItem(getLine(2));
        if(getLine(3).isEmpty())
            slot = -1;
        else {
            try {
                slot = Integer.parseInt(getLine(3));
            } catch (Exception e) {
                slot = -1;
            }
        }

        if(getLine(3).contains("="))
            offset = ICUtil.parseBlockLocation(getSign(), 3);
        else
            offset = getBackBlock().getRelative(BlockFace.UP);
    }

    ItemStack item;
    int slot;
    Block offset;

    @Override
    public String getTitle () {
        return "Container Content Sensor";
    }

    @Override
    public String getSignTitle () {
        return "CONTENT SENSOR";
    }

    @Override
    public void trigger (ChipState chip) {

        if(chip.getInput(0))
            chip.setOutput(0, sense());
    }

    @Override
    public void think (ChipState chip) {

        chip.setOutput(0, sense());
    }

    public boolean sense() {

        if (InventoryUtil.doesBlockHaveInventory(offset)) {

            InventoryHolder inv = (InventoryHolder) offset.getState();
            int amount = 0;
            if(slot < 0 || slot > inv.getInventory().getContents().length) {
                for(ItemStack cont : inv.getInventory().getContents())
                    if(ItemUtil.areItemsIdentical(cont, item)) {
                        if(checkAmount) {
                            amount += cont.getAmount();
                            if(amount >= item.getAmount()) {
                                return true;
                            }
                        } else {
                            return true;
                        }
                    }
            }
            else
                return ItemUtil.areItemsIdentical(item, inv.getInventory().getItem(slot)) && (!checkAmount || inv.getInventory().getItem(slot).getAmount() >= item.getAmount());
        }

        return false;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new ContentsSensor(getServer(), sign, this);
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            ItemStack item = ItemSyntax.getItem(sign.getLine(2));
            if(item == null)
                throw new ICVerificationException("Invalid item to detect!");
        }

        @Override
        public String getShortDescription() {

            return "Detects if the above container has a specific item inside it.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"item id:data", "slot (optional)=offset"};
        }
    }
}