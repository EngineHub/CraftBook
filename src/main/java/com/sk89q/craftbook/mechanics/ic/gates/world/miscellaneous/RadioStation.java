package com.sk89q.craftbook.mechanics.ic.gates.world.miscellaneous;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.util.jinglenote.Playlist;

public class RadioStation extends AbstractSelfTriggeredIC {

    String band;

    public static final Map<String, Playlist> stations = new HashMap<>();

    public RadioStation (Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    public static Playlist getPlaylist(String band) {

        return stations.get(band);
    }

    @Override
    public boolean isAlwaysST() {
        return true;
    }

    @Override
    public void load() {

        band = getLine(3);
        Playlist playlist = new Playlist(getLine(2));
        stations.put(band, playlist);
    }

    @Override
    public String getTitle () {
        return "Radio Station";
    }

    @Override
    public String getSignTitle () {
        return "RADIO STATION";
    }

    @Override
    public void trigger (ChipState chip) {

        Playlist playlist = null;

        if (!stations.containsKey(band)) {
            playlist = new Playlist(getLine(2));
            stations.put(band, playlist);
        } else
            playlist = stations.get(band);

        if (chip.getInput(0) && !playlist.isPlaying())
            playlist.startPlaylist();
        else if(!chip.getInput(0) && playlist.isPlaying())
            playlist.stopPlaylist();

        chip.setOutput(0, playlist.isPlaying());
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new RadioStation(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Broadcasts a playlist.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"Playlist Name", "Radio Band"};
        }
    }
}