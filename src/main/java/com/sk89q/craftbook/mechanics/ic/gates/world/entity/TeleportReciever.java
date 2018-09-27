package com.sk89q.craftbook.mechanics.ic.gates.world.entity;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.util.Tuple2;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

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
            chip.setOutput(0, check());
        }
    }

    @Override
    public void think(ChipState chip) {

        if(!chip.getInput(0))
            chip.setOutput(0, check());
    }

    public boolean check() {

        Tuple2<Long, String> val = TeleportTransmitter.getValue(band);
        if (val == null) return false;

        Player p = Bukkit.getServer().getPlayerExact(val.b);

        if (p == null || !p.isOnline()) {
            return false;
        }

        Block block = getBackBlock();
        while(block.getType().isSolid())
            block = block.getRelative(0,1,0);

        p.teleport(block.getLocation().add(0.5, 0.5, 0.5));
        CraftBookPlugin.inst().wrapPlayer(p).print(welcome);
        TeleportTransmitter.lastKnownLocations.put(band, block.getLocation());
        return true;
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
        public String[] getLongDescription() {

            return new String[] {
                    "The '''MC1113''' will teleport a player from a corresponding [[../MC1112/]] IC on a redstone signal."
            };
        }

        @Override
        public String getShortDescription() {

            return "Reciever for the teleportation network.";
        }

        @Override
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                    "Trigger IC (When ST, disables IC when high)",//Inputs
                    "High on successful teleport",//Outputs
            };
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"Frequency", "+oWelcome Text"};
        }
    }
}