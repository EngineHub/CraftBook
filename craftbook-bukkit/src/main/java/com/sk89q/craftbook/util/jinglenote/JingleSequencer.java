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

// $Id$
/*
 * Tetsuuuu plugin for SK's Minecraft Server Copyright (C) 2010 sk89q <http://www.sk89q.com> All rights reserved.
 */

package com.sk89q.craftbook.util.jinglenote;

import java.util.Set;


/**
 * Interface for a sequencer.
 *
 * @author sk89q
 */
public interface JingleSequencer {

    void run() throws InterruptedException;

    void stop();

    void stop(JingleNotePlayer player);

    boolean isPlaying();

    boolean hasPlayedBefore();

    void play(JingleNotePlayer player);

    int getPlayerCount();

    Set<JingleNotePlayer> getPlayers();

    class Note {
        Instrument instrument;
        byte note;
        float velocity;

        public Note(Instrument instrument, byte note, float velocity) {

            this.instrument = instrument;
            this.note = note;
            this.velocity = velocity;
        }

        public Instrument getInstrument() {

            return instrument;
        }

        public float getNote() {

            return (float) Math.pow(2.0D, (note - 12) / 12.0D);
        }

        public float getVelocity() {

            return velocity;
        }
    }
}