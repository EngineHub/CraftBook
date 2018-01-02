/*
 * CraftBook Copyright (C) 2010-2018 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2018 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
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
package com.sk89q.craftbook.core.util.jinglenote;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class JingleNoteManager {

    /**
     * List of instances.
     */
    private final Map<UUID, JingleNotePlayer> instances = new HashMap<>();

    public boolean isPlaying(UUID player) {
        return instances.containsKey(player) && instances.get(player).isPlaying();
    }

    public boolean isPlaying() {
        return !instances.isEmpty();
    }

    public void play(UUID player, JingleSequencer sequencer) {
        // Existing player found!
        if (instances.containsKey(player)) {
            stop(player);
        }

        instances.put(player, createNotePlayer(player, sequencer));
    }

    protected abstract JingleNotePlayer createNotePlayer(UUID player, JingleSequencer sequencer);

    public boolean stop(UUID player) {
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