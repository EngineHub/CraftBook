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
package org.enginehub.craftbook.sponge.util.jinglenote;

import org.enginehub.craftbook.core.util.jinglenote.JingleNoteManager;
import org.enginehub.craftbook.core.util.jinglenote.JingleNotePlayer;
import org.enginehub.craftbook.core.util.jinglenote.JingleSequencer;

import java.util.UUID;

public class SpongeJingleNoteManager extends JingleNoteManager {

    @Override
    protected JingleNotePlayer createNotePlayer(UUID player, JingleSequencer sequencer) {
        JingleNotePlayer notePlayer = new SpongeJingleNotePlayer(player, sequencer);
        Thread thread = new Thread(notePlayer);
        thread.setDaemon(true);
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.setName("JingleNotePlayer for " + player);
        thread.start();

        return notePlayer;
    }
}
