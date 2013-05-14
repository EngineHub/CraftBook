package com.sk89q.craftbook.circuits.gates.world.miscellaneous;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractIC;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.jinglenote.Playlist;
import com.sk89q.craftbook.util.LocationUtil;

public class RadioPlayer extends AbstractIC {

    String band;
    int radius;

    List<Player> listening;

    public RadioPlayer (Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    @Override
    public void load() {

        band = getLine(2);
        try {
            radius = Integer.parseInt(getLine(3));
        }
        catch(Exception e) {
            radius = -1;
        }

        listening = new ArrayList<Player>();
    }

    @Override
    public String getTitle () {
        return "Radio Player";
    }

    @Override
    public String getSignTitle () {
        return "RADIO PLAYER";
    }

    @Override
    public void trigger (ChipState chip) {

        Playlist playlist = RadioStation.getPlaylist(band);

        if(playlist == null)
            return;

        if(chip.getInput(0)) {
            if(radius < 0) {
                listening.addAll(Arrays.asList(Bukkit.getServer().getOnlinePlayers()));
                playlist.addPlayers(listening);
            } else {
                Location signLoc = BukkitUtil.toSign(getSign()).getLocation();
                for(Player player : BukkitUtil.toSign(getSign()).getWorld().getPlayers()) {

                    if(LocationUtil.isWithinSphericalRadius(signLoc, player.getLocation(), radius))
                        listening.add(player);
                }

                playlist.addPlayers(listening);
            }
        } else {

            playlist.removePlayers(listening);
            listening.clear();
        }
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new RadioPlayer(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Plays a radio station.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"Radio Band", "Radius"};
        }
    }
}