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
package org.enginehub.craftbook.core.util.jinglenote;

import java.util.UUID;

public abstract class JingleNotePlayer implements Runnable {

    protected final UUID player;
    private JingleSequencer sequencer;

    /**
     * Constructs a new JingleNotePlayer
     *
     * @param player The player who is hearing this's name.
     * @param seq The JingleSequencer to play.
     */
    public JingleNotePlayer(UUID player, JingleSequencer seq) {
        this.player = player;
        sequencer = seq;
    }

    @Override
    public void run() {
        if(sequencer == null)
            return;
        try {
            try {
                sequencer.play(this);
            } catch (Throwable t) {
                t.printStackTrace();
            }

            while(isPlaying()){
                Thread.sleep(10L);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            stop();
        }
    }

    public boolean isPlaying() {
        return sequencer != null && (sequencer.isPlaying() || !sequencer.hasPlayedBefore());
    }

    public UUID getPlayer() {
        return player;
    }

    public void stop() {
        if (sequencer != null) {
            sequencer.stop(this);
            sequencer = null;
        }
    }

    public abstract void play(Note note);
}
