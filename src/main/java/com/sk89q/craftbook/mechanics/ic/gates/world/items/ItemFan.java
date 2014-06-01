package com.sk89q.craftbook.mechanics.ic.gates.world.items;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.util.ItemUtil;

public class ItemFan extends AbstractSelfTriggeredIC {

    public ItemFan(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    double force;

    @Override
    public void load() {

        try {
            force = Double.parseDouble(getSign().getLine(2));
        } catch (Exception ignored) {
            force = 1;
        }
    }

    @Override
    public String getTitle() {

        return "Item Fan";
    }

    @Override
    public String getSignTitle() {

        return "ITEM FAN";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) chip.setOutput(0, push());
    }

    @Override
    public void think(ChipState state) {

        state.setOutput(0, push());
    }

    public boolean push() {

        boolean returnValue = false;

        Block aboveBlock = getBackBlock().getRelative(0, 1, 0);

        for (Item item : ItemUtil.getItemsAtBlock(aboveBlock)) {
            item.teleport(item.getLocation().add(0, force, 0));
            returnValue = true;
        }

        return returnValue;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new ItemFan(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Gently pushes items upwards.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"force (default 1)", null};
        }
    }
}