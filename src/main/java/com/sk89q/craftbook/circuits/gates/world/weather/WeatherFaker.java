package com.sk89q.craftbook.circuits.gates.world.weather;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.RestrictedIC;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.worldedit.Vector;

/**
 * @author Me4502
 */
public class WeatherFaker extends AbstractSelfTriggeredIC {

    public WeatherFaker(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Weather Faker";
    }

    @Override
    public String getSignTitle() {

        return "WEATHER FAKER";
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new WeatherFaker(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Fakes a players weather in radius.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {"radius", "rain (If it should rain)"};
            return lines;
        }
    }

    @Override
    public boolean isAlwaysST() {

        return true;
    }

    @Override
    public boolean isActive() {

        return true;
    }

    @Override
    public void trigger(ChipState chip) {

    }

    private ArrayList<String> players = new ArrayList<String>();

    Vector radius;
    boolean rain;

    @Override
    public void load() {

        radius = ICUtil.parseRadius(getSign());
        rain = getLine(3).equals("rain");
    }

    @Override
    public void think(ChipState chip) {

        if (chip.getInput(0)) {
            for (Player p : Bukkit.getOnlinePlayers()) {

                if (!players.contains(p.getName()) && LocationUtil.isWithinRadius(p.getLocation(),
                        BukkitUtil.toSign(getSign()).getLocation(), radius)) {
                    p.setPlayerWeather(rain ? WeatherType.DOWNFALL : WeatherType.CLEAR);
                    players.add(p.getName());
                } else if (players.contains(p.getName())) {
                    p.resetPlayerWeather();
                    players.remove(p.getName());
                }
            }
        }
    }
}