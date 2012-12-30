package com.sk89q.craftbook.circuits.gates.world.items;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.SelfTriggeredIC;
import org.bukkit.Server;

public class ItemFanST extends ItemFan implements SelfTriggeredIC {

    public ItemFanST(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public boolean isActive() {

        return true;
    }

    @Override
    public String getTitle() {

        return "Self-Triggered Item Fan";
    }

    @Override
    public String getSignTitle() {

        return "ITEM FAN ST";
    }

    @Override
    public void think(ChipState state) {

        state.setOutput(0, push());
    }

    public static class Factory extends ItemFan.Factory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new ItemFanST(getServer(), sign, this);
        }
    }
}