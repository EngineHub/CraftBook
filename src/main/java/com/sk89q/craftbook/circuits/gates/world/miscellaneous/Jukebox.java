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

public class Jukebox extends AbstractIC {

    Playlist playlist;
    int radius;

    public Jukebox (Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    @Override
    public void load() {

        String plist = getLine(2);
        try {
            radius = Integer.parseInt(getLine(3));
        }
        catch(Exception e) {
            radius = -1;
        }

        playlist = new Playlist(plist);
    }

    @Override
    public String getTitle () {
        return "Jukebox";
    }

    @Override
    public String getSignTitle () {
        return "JUKEBOX";
    }

    @Override
    public void trigger (ChipState chip) {

        if(radius < 0)
            playlist.startPlaylist(Arrays.asList(Bukkit.getServer().getOnlinePlayers()));
        else {
            List<Player> players = new ArrayList<Player>();
            Location signLoc = BukkitUtil.toSign(getSign()).getLocation();
            for(Player player : BukkitUtil.toSign(getSign()).getWorld().getPlayers()) {

                if(LocationUtil.isWithinSphericalRadius(signLoc, player.getLocation(), radius))
                    players.add(player);
            }

            playlist.startPlaylist(players);
        }
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new Jukebox(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Plays a Playlist.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {"Playlist Name", "Radius"};
            return lines;
        }
    }
}
