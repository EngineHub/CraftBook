/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package org.enginehub.craftbook.mechanics.ic.gates.world.miscellaneous;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.util.SearchArea;
import org.enginehub.craftbook.util.jinglenote.Playlist;

import java.util.HashMap;
import java.util.Map;

public class Jukebox extends AbstractSelfTriggeredIC {

    public static Map<Location, Playlist> playlists;

    Map<String, SearchArea> players;

    SearchArea area;

    public Jukebox(Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    @Override
    public void unload() {
        if (playlists.containsKey(getBackBlock().getLocation())) {
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
        if (!getLine(3).isEmpty())
            area = SearchArea.createArea(getLocation().getBlock(), getLine(3));

        if (!playlists.containsKey(getBackBlock().getLocation()))
            playlists.put(getBackBlock().getLocation(), new Playlist(plist));

        players = new HashMap<>();
    }

    @Override
    public String getTitle() {
        return "Jukebox";
    }

    @Override
    public String getSignTitle() {
        return "JUKEBOX";
    }

    @Override
    public void trigger(ChipState chip) {

        Playlist playlist = playlists.get(getBackBlock().getLocation());

        if (playlist == null) return; //Heh?

        if (chip.getInput(0) && !playlist.isPlaying())
            playlist.startPlaylist();
        else if (!chip.getInput(0) && playlist.isPlaying())
            playlist.stopPlaylist();

        if (chip.getInput(0)) {

            boolean hasChanged = false;

            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                if (area != null && !area.isWithinArea(p.getLocation())) {
                    if (players.containsKey(p.getName())) {
                        players.remove(p.getName());
                        hasChanged = true;
                    }
                } else if (!players.containsKey(p.getName())) {
                    players.put(p.getName(), area);
                    hasChanged = true;
                }
            }

            if (hasChanged)
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

            return new String[] { "Playlist Name", "Radius" };
        }
    }
}