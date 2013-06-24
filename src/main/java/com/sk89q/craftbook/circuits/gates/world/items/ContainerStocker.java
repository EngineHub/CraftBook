package com.sk89q.craftbook.circuits.gates.world.items;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Chest;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.RestrictedIC;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.ItemSyntax;

public class ContainerStocker extends AbstractSelfTriggeredIC {

    public ContainerStocker(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    ItemStack item;
    Location offset;

    @Override
    public void load() {

        if(getLine(3).isEmpty())
            offset = getBackBlock().getRelative(0, 1, 0).getLocation();
        else
            offset = ICUtil.parseBlockLocation(getSign(), 3).getLocation();
        item = ItemSyntax.getItem(getLine(2));
    }

    @Override
    public String getTitle() {

        return "Container Stocker";
    }

    @Override
    public String getSignTitle() {

        return "STOCKER";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) chip.setOutput(0, stock());
    }

    @Override
    public void think(ChipState chip) {

        chip.setOutput(0, stock());
    }

    public boolean stock() {

        if (offset.getBlock().getState() instanceof InventoryHolder) {

            Chest c = (Chest) offset.getBlock().getState();
            if (c.getInventory().addItem(item.clone()).isEmpty()) {
                c.update();
                return true;
            }
        }
        return false;
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new ContainerStocker(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Adds item into container at specified offset.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"item id:data", "x:y:z offset"};
        }
    }
}