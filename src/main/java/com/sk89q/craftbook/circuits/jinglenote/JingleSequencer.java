// $Id$
/*
 * Tetsuuuu plugin for SK's Minecraft Server Copyright (C) 2010 sk89q <http://www.sk89q.com> All rights reserved.
 */

package com.sk89q.craftbook.circuits.jinglenote;

import org.bukkit.Sound;

/**
 * Interface for a sequencer.
 *
 * @author sk89q
 */
public interface JingleSequencer {

    public void run(JingleNotePlayer player) throws InterruptedException;

    public void stop();

    public class Note {

        Sound instrument;
        byte note;
        float velocity;

        public Note(Sound instrument, byte note, float velocity) {

            this.instrument = instrument;
            this.note = note;
            this.velocity = velocity;
        }

        public Sound getInstrument() {

            return instrument;
        }

        public float getNote() {

            return (float) Math.pow(2.0D, (note - 12) / 12.0D);
        }

        public float getVelocity() {

            if (instrument == Sound.NOTE_PLING) return velocity / 256;
            return velocity / 64;
        }
    }
}