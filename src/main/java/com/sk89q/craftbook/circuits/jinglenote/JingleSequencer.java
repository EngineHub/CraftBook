// $Id$
/*
 * Tetsuuuu plugin for SK's Minecraft Server Copyright (C) 2010 sk89q <http://www.sk89q.com> All rights reserved.
 */

package com.sk89q.craftbook.circuits.jinglenote;

import java.util.Set;


/**
 * Interface for a sequencer.
 *
 * @author sk89q
 */
public interface JingleSequencer {

    public void run() throws InterruptedException;

    public void stop();

    public void stop(JingleNotePlayer player);

    public boolean isPlaying();

    public boolean hasPlayedBefore();

    public void play(JingleNotePlayer player);

    public Set<JingleNotePlayer> getPlayers();

    public class Note {

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