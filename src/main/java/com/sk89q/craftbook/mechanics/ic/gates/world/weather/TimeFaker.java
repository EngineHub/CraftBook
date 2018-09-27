package com.sk89q.craftbook.mechanics.ic.gates.world.weather;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.RestrictedIC;
import com.sk89q.craftbook.util.SearchArea;

/**
 * @author Me4502
 */
public class TimeFaker extends AbstractSelfTriggeredIC {

    public TimeFaker(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Time Faker";
    }

    @Override
    public String getSignTitle() {

        return "TIME FAKER";
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new TimeFaker(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Radius based fake time.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"radius", "time"};
        }
    }

    private Set<String> players;

    SearchArea area;
    long time;

    @Override
    public void load() {

        players = new HashSet<>();

        area = SearchArea.createArea(getLocation().getBlock(), getLine(2));

        try {
            time = Long.parseLong(getSign().getLine(3));
        } catch (Exception e) {
            if (time == 0) time = 13000L;
        }
    }

    @Override
    public boolean isAlwaysST() {

        return true;
    }

    @Override
    public void trigger(ChipState chip) {
        if (chip.getInput(0)) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if(area.isWithinArea(p.getLocation())) {
                    p.setPlayerTime(time, false);
                    players.add(p.getName());
                } else if(players.contains(p.getName())) {
                    players.remove(p.getName());
                    p.resetPlayerTime();
                }
            }
        } else {
            for(String p : players) {
                Player pp = Bukkit.getPlayerExact(p);
                if(pp == null) continue;
                pp.resetPlayerTime();
            }
            players.clear();
        }
    }
}