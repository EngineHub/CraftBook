package com.sk89q.craftbook.mechanics.ic.gates.world.miscellaneous;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.util.SearchArea;
import com.sk89q.craftbook.util.jinglenote.Playlist;

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
            area = SearchArea.createArea(getLocation().getBlock(), getLine(3));
        else
            area = SearchArea.createEmptyArea();

        listening = new HashMap<>();
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
            if(area.getPlayersInArea().size() != listening.size()) {

                Map<String, SearchArea> removals = new HashMap<>();

                for(Entry<String, SearchArea> key : listening.entrySet()) {
                    boolean found = false;
                    for(Player p : area.getPlayersInArea()) {
                        if(p.getName().equals(key.getKey())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) removals.put(key.getKey(), key.getValue());
                }

                if(removals.size() > 0) {
                    playlist.getPlaylistInterpreter().removePlayers(removals);
                    for(String key : removals.keySet())
                        listening.remove(key);
                }

                boolean changed = false;

                for(Player player : area.getPlayersInArea())
                    if(!listening.containsKey(player.getName())) {
                        listening.put(player.getName(), area);
                        changed = true;
                    }

                if(changed)
                    playlist.getPlaylistInterpreter().addPlayers(listening);

                CraftBookPlugin.logDebugMessage("Reset listener list! Size of: " + listening.size(), "ic-mc1277");
            }
        } else if(listening.size() > 0) {
            playlist.getPlaylistInterpreter().removePlayers(listening);
            listening.clear();

            CraftBookPlugin.logDebugMessage("Cleared listener list!", "ic-mc1277");
        }

        chip.setOutput(0, playlist.isPlaying() && !listening.isEmpty());
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