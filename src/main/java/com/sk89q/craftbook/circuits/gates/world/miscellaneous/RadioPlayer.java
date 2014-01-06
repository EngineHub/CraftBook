package com.sk89q.craftbook.circuits.gates.world.miscellaneous;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

public class RadioPlayer extends AbstractSelfTriggeredIC {

    String band;
    SearchArea area;

    Map<String, SearchArea> listening;

    public RadioPlayer (Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    @Override
    public void load() {

        band = getLine(2);
        if (!getLine(3).isEmpty())
            area = SearchArea.createArea(getBackBlock(), getLine(3));
        else
            area = SearchArea.createEmptyArea();

        listening = new HashMap<String, SearchArea>();
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
    public boolean isAlwaysST() {
        return true;
    }

    @Override
    public void trigger (ChipState chip) {

        Playlist playlist = RadioStation.getPlaylist(band);

        if(playlist == null)
            return;

        if(chip.getInput(0)) {
            List<String> players = new ArrayList<String>();
            for(Player player : area.getPlayersInArea())
                players.add(player.getName());
            if(players.size() != listening.size()) {

                Map<String, SearchArea> removals = new HashMap<String, SearchArea>();

                Iterator<Entry<String, SearchArea>> iter = listening.entrySet().iterator();
                while(iter.hasNext()) {
                    Entry<String, SearchArea> ent = iter.next();
                    if(!players.contains(ent.getKey())) {
                        removals.put(ent.getKey(), ent.getValue());
                        iter.remove();
                    }
                }

                for(String player : players) {
                    if(!listening.containsKey(player))
                        listening.put(player, area);
                }

                playlist.removePlayers(removals);
                playlist.addPlayers(listening);
            }
        } else if(listening.size() > 0) {
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