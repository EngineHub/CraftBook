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

package org.enginehub.craftbook.util.jinglenote;

import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.util.SearchArea;
import org.enginehub.craftbook.util.jinglenote.bukkit.BukkitJingleNotePlayer;

import java.util.HashMap;
import java.util.Map;

/**
 * A manager of play instances.
 *
 * @author sk89q
 */
public class JingleNoteManager {

    /**
     * List of instances.
     */
    private final Map<String, JingleNotePlayer> instances = new HashMap<>();

    public boolean isPlaying(String player) {

        return instances.containsKey(player) && instances.get(player).isPlaying();
    }

    public boolean isPlaying() {

        /*if(instances.isEmpty()) return false;
        Iterator<String> iter = instances.keySet().iterator();
        while(iter.hasNext()) {
            String ent = iter.next();
            if(!isPlaying(ent))
                stop(ent);
        }*/
        return !instances.isEmpty();
    }

    public void play(String player, JingleSequencer sequencer, SearchArea area) {

        // Existing player found!
        if (instances.containsKey(player)) {
            stop(player);
        }

        CraftBookPlugin.logDebugMessage("Playing sequencer for player: " + player, "midi");

        JingleNotePlayer notePlayer = new BukkitJingleNotePlayer(player, sequencer, area);
        Thread thread = new Thread(notePlayer);
        thread.setDaemon(true);
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.setName("JingleNotePlayer for " + player);
        thread.start();

        instances.put(player, notePlayer);
    }

    public boolean stop(String player) {

        // Existing player found!
        if (instances.containsKey(player)) {
            instances.remove(player).stop();
            return true;
        }
        return false;
    }

    public void stopAll() {

        for (JingleNotePlayer notePlayer : instances.values()) {
            notePlayer.stop();
        }

        instances.clear();
    }
}