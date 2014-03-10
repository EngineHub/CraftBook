package com.sk89q.craftbook.circuits.gates.world.entity;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.util.Tuple2;
import com.sk89q.worldedit.blocks.BlockType;

public class TeleportReciever extends AbstractSelfTriggeredIC {

    public TeleportReciever(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Teleport Reciever";
    }

    @Override
    public String getSignTitle() {

        return "TELEPORT IN";
    }

    String band;
    String welcome;

    @Override
    public void load() {

        band = getLine(2);
        welcome = getLine(3);
        if (welcome == null || welcome.isEmpty()) welcome = "The Teleporter moves you here...";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            check();
        }
    }

    @Override
    public void think(ChipState chip) {

        if(!chip.getInput(0))
            check();
    }

    public void check() {

        Tuple2<Long, String> val = TeleportTransmitter.getValue(band);
        if (val == null) return;

        Player p = Bukkit.getServer().getPlayer(val.b);

        if (p == null || !p.isOnline()) {
            return;
        }

        Block block = getBackBlock();
        while(!BlockType.canPassThrough(block.getTypeId()))
            block = block.getRelative(0,1,0);

        p.teleport(block.getLocation().add(0.5, 0.5, 0.5));
        CraftBookPlugin.inst().wrapPlayer(p).print(welcome);
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new TeleportReciever(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Reciever for the teleportation network.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"frequency name", "+owelcome text"};
        }
    }
}