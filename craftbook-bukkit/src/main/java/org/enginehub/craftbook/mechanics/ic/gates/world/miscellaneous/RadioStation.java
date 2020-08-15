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

import org.bukkit.Server;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.util.jinglenote.Playlist;

import java.util.HashMap;
import java.util.Map;

public class RadioStation extends AbstractSelfTriggeredIC {

    String band;

    public static final Map<String, Playlist> stations = new HashMap<>();

    public RadioStation(Server server, ChangedSign sign, ICFactory factory) {
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
    public String getTitle() {
        return "Radio Station";
    }

    @Override
    public String getSignTitle() {
        return "RADIO STATION";
    }

    @Override
    public void trigger(ChipState chip) {

        Playlist playlist = null;

        if (!stations.containsKey(band)) {
            playlist = new Playlist(getLine(2));
            stations.put(band, playlist);
        } else
            playlist = stations.get(band);

        if (chip.getInput(0) && !playlist.isPlaying())
            playlist.startPlaylist();
        else if (!chip.getInput(0) && playlist.isPlaying())
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

            return new String[] { "Playlist Name", "Radio Band" };
        }
    }
}