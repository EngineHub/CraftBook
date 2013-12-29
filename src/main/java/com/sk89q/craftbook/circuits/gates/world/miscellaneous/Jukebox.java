package com.sk89q.craftbook.circuits.gates.world.miscellaneous;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.circuits.ic.AbstractIC;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.jinglenote.Playlist;
import com.sk89q.craftbook.util.SearchArea;
import com.sk89q.worldedit.BlockWorldVector;

public class Jukebox extends AbstractIC {

    public static Map<BlockWorldVector, Playlist> playlists = new HashMap<BlockWorldVector, Playlist>();

    SearchArea area;

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
        if (!getLine(3).isEmpty()) area = SearchArea.createArea(getBackBlock(), getLine(3));

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

        Map<Player, SearchArea> players = new WeakHashMap<Player, SearchArea>();
        for(Player p : Bukkit.getServer().getOnlinePlayers()) {
            if(area != null && !area.isWithinArea(p.getLocation())) continue;
            players.put(p, area);
        }

        playlist.setPlayers(players);
        if(chip.getInput(0))
            playlist.startPlaylist();
        else
            playlist.stopPlaylist();
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