package com.sk89q.craftbook.circuits.gates.world.miscellaneous;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.jinglenote.Playlist;
import com.sk89q.craftbook.util.HistoryHashMap;

public class RadioStation extends AbstractSelfTriggeredIC {

    String band;

    public static final HistoryHashMap<String, Playlist> stations = new HistoryHashMap<String, Playlist>(100);

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
        else if(playlist.isPlaying())
            playlist.stopPlaylist();
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