package com.sk89q.craftbook.mechanics.ic.gates.world.miscellaneous;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.util.SearchArea;
import com.sk89q.craftbook.util.jinglenote.Playlist;

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
    public boolean isAlwaysST() {
        return true;
    }

    @Override
    public void load() {

        String plist = getLine(2);
        if (!getLine(3).isEmpty()) area = SearchArea.createArea(getLocation().getBlock(), getLine(3));

        if(!playlists.containsKey(getBackBlock().getLocation()))
            playlists.put(getBackBlock().getLocation(), new Playlist(plist));

        players = new HashMap<>();
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

        if(chip.getInput(0)) {

            boolean hasChanged = false;

            for(Player p : Bukkit.getServer().getOnlinePlayers()) {
                if(area != null && !area.isWithinArea(p.getLocation())) {
                    if(players.containsKey(p.getName())) {
                        players.remove(p.getName());
                        hasChanged = true;
                    }
                } else if (!players.containsKey(p.getName())) {
                    players.put(p.getName(), area);
                    hasChanged = true;
                }
            }

            if(hasChanged)
                playlist.getPlaylistInterpreter().setPlayers(players);
        }

        chip.setOutput(0, playlist.isPlaying());
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
            playlists = new HashMap<>();
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