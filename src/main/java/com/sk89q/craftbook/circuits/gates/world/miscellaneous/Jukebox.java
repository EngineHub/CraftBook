package com.sk89q.craftbook.circuits.gates.world.miscellaneous;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractIC;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.jinglenote.Playlist;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.Tuple2;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.WorldVector;

public class Jukebox extends AbstractIC {

    public static Map<BlockWorldVector, Playlist> playlists = new HashMap<BlockWorldVector, Playlist>();

    int radius;

    public Jukebox (Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    @Override
    public void onICBreak(BlockBreakEvent event) {
        super.onICBreak(event);
        if(playlists.containsKey(getSign().getBlockVector())) {
            playlists.remove(getSign().getBlockVector()).stopPlaylist();
        }
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

        if(!playlists.containsKey(getSign().getBlockVector()))
            playlists.put(getSign().getBlockVector(), new Playlist(plist));
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

        Playlist playlist = playlists.get(getSign().getBlockVector());

        if(playlist == null) return; //Heh?

        if(radius < 0) {
            Map<Player, Tuple2<WorldVector, Integer>> players = new WeakHashMap<Player, Tuple2<WorldVector, Integer>>();
            for(Player p : Bukkit.getServer().getOnlinePlayers()) {

                players.put(p, new Tuple2<WorldVector, Integer>(getSign().getBlockVector(), radius));
            }

            playlist.setPlayers(players);
            if(chip.getInput(0))
                playlist.startPlaylist();
            else
                playlist.stopPlaylist();
        } else {
            Map<Player, Tuple2<WorldVector, Integer>> players = new WeakHashMap<Player, Tuple2<WorldVector, Integer>>();
            Location signLoc = BukkitUtil.toSign(getSign()).getLocation();
            for(Player player : BukkitUtil.toSign(getSign()).getWorld().getPlayers()) {

                if(LocationUtil.isWithinSphericalRadius(signLoc, player.getLocation(), radius))
                    players.put(player, new Tuple2<WorldVector, Integer>(getSign().getBlockVector(), radius));
            }

            playlist.setPlayers(players);
            if(chip.getInput(0))
                playlist.startPlaylist();
            else
                playlist.stopPlaylist();
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

            return new String[] {"Playlist Name", "Radius"};
        }
    }
}