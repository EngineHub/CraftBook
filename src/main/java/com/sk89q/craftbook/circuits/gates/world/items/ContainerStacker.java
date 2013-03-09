package com.sk89q.craftbook.circuits.gates.world.items;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractIC;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.SelfTriggeredIC;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.util.yaml.YAMLProcessor;

public class ContainerStacker extends AbstractIC implements SelfTriggeredIC {

    public ContainerStacker (Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    @Override
    public String getTitle () {
        return "Container Stacker";
    }

    @Override
    public String getSignTitle () {
        return "CONTAINER STACKER";
    }

    @Override
    public void trigger (ChipState chip) {

        if(chip.getInput(0))
            stack();
    }

    @Override
    public boolean isActive () {
        return true;
    }

    @Override
    public void think (ChipState chip) {

        stack();
    }

    public void stack() {

        Block b = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock());

        int x = b.getX();
        int y = b.getY() + 1;
        int z = b.getZ();
        Block bl = BukkitUtil.toSign(getSign()).getBlock().getWorld().getBlockAt(x, y, z);
        if (bl.getState() instanceof InventoryHolder) {
            InventoryHolder c = (InventoryHolder) bl.getState();
            for (int i = 0; i < c.getInventory().getSize(); i++) {
                ItemStack it = c.getInventory().getItem(i);
                if (ItemUtil.isStackValid(it)) {

                    if(((Factory)getFactory()).blacklist.contains(it.getTypeId()))
                        continue;
                    int amount = it.getAmount();
                    if (it.getAmount() < 64) {

                        int missing = 0;

                        for (int ii = 0; ii < c.getInventory().getSize(); ii++) {
                            if (ii == i)
                                continue;
                            ItemStack itt = c.getInventory().getItem(ii);
                            if (ItemUtil.isStackValid(itt) && ItemUtil.areItemsIdentical(it, itt)) {

                                if (amount + itt.getAmount() <= 64) {
                                    amount += itt.getAmount();
                                } else {
                                    missing = amount + itt.getAmount() - 64;
                                    amount = 64;
                                }
                                c.getInventory().remove(itt);
                            }
                        }

                        if (amount != it.getAmount()) {

                            it.setAmount(amount);
                            c.getInventory().setItem(i, it);
                            break;
                        }
                        if(missing > 0) {

                            ItemStack miss = new ItemStack(it);
                            miss.setAmount(missing);
                            c.getInventory().addItem(miss);
                        }
                    }
                }
            }
        }
    }

    public static class Factory extends AbstractICFactory {

        List<Integer> blacklist = new ArrayList<Integer>();

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new ContainerStacker(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Stacks all items in a container to 64.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {null, null};
            return lines;
        }

        @Override
        public void addConfiguration(YAMLProcessor config, String path) {
            blacklist = config.getIntList(path + "blacklist", blacklist);
        }

        @Override
        public boolean needsConfiguration() {

            return true;
        }

    }
}
