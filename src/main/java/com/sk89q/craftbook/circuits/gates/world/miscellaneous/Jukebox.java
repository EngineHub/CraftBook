package com.sk89q.craftbook.circuits.gates.world.miscellaneous;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.jinglenote.Playlist;
import com.sk89q.craftbook.util.SearchArea;

public class Jukebox extends AbstractSelfTriggeredIC {

    public static Map<Location, Playlist> playlists;

    Map<String, SearchArea> players;

    SearchArea area;

    public Jukebox (Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    @Override
    public void unload() {
        if(playlists.containsKey(getBackBlock().getLocation())) {
            playlists.remove(getBackBlock().getLocation()).stopPlaylist();
        }
    }

    @Override
    public void load() {

        String plist = getLine(2);
        if (!getLine(3).isEmpty()) area = SearchArea.createArea(getBackBlock(), getLine(3));

        if(!playlists.containsKey(getBackBlock().getLocation()))
            playlists.put(getBackBlock().getLocation(), new Playlist(plist));

        players = new HashMap<String, SearchArea>();
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

        Playlist playlist = playlists.get(getBackBlock().getLocation());

        if(playlist == null) return; //Heh?

        if (chip.getInput(0) && !playlist.isPlaying())
            playlist.startPlaylist();
        else if(!chip.getInput(0) && playlist.isPlaying())
            playlist.stopPlaylist();

        for(Player p : Bukkit.getServer().getOnlinePlayers()) {
            if(area != null && !area.isWithinArea(p.getLocation())) {
                if(players.containsKey(p.getName()))
                    players.remove(p.getName());
                continue;
            } else if (!players.containsKey(p.getName()))
                players.put(p.getName(), area);
        }

        playlist.getPlaylistInterpreter().setPlayers(players);

        chip.setOutput(0, playlist.isPlaying());
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
            playlists = new HashMap<Location, Playlist>();
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