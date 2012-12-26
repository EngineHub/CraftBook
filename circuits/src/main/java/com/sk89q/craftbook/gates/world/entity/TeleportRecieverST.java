package com.sk89q.craftbook.gates.world.entity;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.SelfTriggeredIC;
import org.bukkit.Server;

public class TeleportRecieverST extends TeleportReciever implements SelfTriggeredIC {

    public TeleportRecieverST(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Self-Triggered Teleport Reciever";
    }

    @Override
    public String getSignTitle() {

        return "TELEPORT IN ST";
    }

    @Override
    public boolean isActive() {

        return true;
    }

    @Override
    public void think(ChipState chip) {

        check();
    }

    public static class Factory extends TeleportReciever.Factory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new TeleportRecieverST(getServer(), sign, this);
        }
    }
}