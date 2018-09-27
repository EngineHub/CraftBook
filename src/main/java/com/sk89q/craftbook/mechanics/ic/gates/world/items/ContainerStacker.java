package com.sk89q.craftbook.mechanics.ic.gates.world.items;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.ConfigurableIC;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.util.InventoryUtil;
import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.util.yaml.YAMLProcessor;

public class ContainerStacker extends AbstractSelfTriggeredIC {

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

        Block b = getBackBlock();

        int x = b.getX();
        int y = b.getY() + 1;
        int z = b.getZ();
        Block bl = CraftBookBukkitUtil.toSign(getSign()).getBlock().getWorld().getBlockAt(x, y, z);
        if (InventoryUtil.doesBlockHaveInventory(bl)) {
            InventoryHolder c = (InventoryHolder) bl.getState();
            for (int i = 0; i < c.getInventory().getSize(); i++) {
                ItemStack it = c.getInventory().getItem(i);
                if (ItemUtil.isStackValid(it)) {

                    if(((Factory)getFactory()).blacklist.contains(new ItemInfo(it)))
                        continue;
                    int amount = it.getAmount();
                    if (it.getAmount() < 64) {

                        int missing = 0;

                        for (int ii = 0; ii < c.getInventory().getSize(); ii++) {
                            if (ii == i)
                                continue;
                            ItemStack itt = c.getInventory().getItem(ii);
                            if (ItemUtil.isStackValid(itt) && ItemUtil.areItemsIdentical(it, itt) && it.getEnchantments().isEmpty() && !itt.getEnchantments().isEmpty() && !it.hasItemMeta() && !itt.hasItemMeta()) {

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

    public static class Factory extends AbstractICFactory implements ConfigurableIC {

        List<ItemInfo> blacklist = new ArrayList<>();

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
        public void addConfiguration(YAMLProcessor config, String path) {
            blacklist.addAll(ItemInfo.parseListFromString(config.getStringList(path + "blacklist", ItemInfo.toStringList(blacklist))));
        }
    }
}
