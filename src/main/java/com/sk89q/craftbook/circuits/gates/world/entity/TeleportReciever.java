package com.sk89q.craftbook.circuits.gates.world.entity;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.*;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.Tuple2;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class TeleportReciever extends AbstractIC {

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

    public void check() {

        Tuple2<Long, String> val = TeleportTransmitter.getValue(band);
        if (val == null) return;

        Player p = Bukkit.getServer().getPlayer(val.b);

        if (p == null || !p.isOnline()) {
            return;
        }

        p.teleport(SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock()).getLocation().add(0.5, 1.5, 0.5));
        p.sendMessage(ChatColor.YELLOW + welcome);
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
        public String getDescription() {

            return "Reciever for the teleportation network.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {"frequency name", "welcome text"};
            return lines;
        }
    }
}