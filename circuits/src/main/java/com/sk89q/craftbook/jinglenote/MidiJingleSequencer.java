// $Id$
/*
 * Tetsuuuu plugin for SK's Minecraft Server Copyright (C) 2010 sk89q <http://www.sk89q.com> All rights reserved.
 */

package com.sk89q.craftbook.jinglenote;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;

import org.bukkit.Bukkit;
import org.bukkit.Sound;

import com.sk89q.craftbook.util.GeneralUtil;

/**
 * A sequencer that reads MIDI files.
 * 
 * @author sk89q
 */
public class MidiJingleSequencer implements JingleSequencer {

    private static final int[] instruments = { 0, 0, 0, 0, 0, 0, 0, 5, // 8
        6, 0, 0, 0, 0, 0, 0, 0, // 16
        0, 0, 0, 0, 0, 0, 0, 5, // 24
        5, 5, 5, 5, 5, 5, 5, 5, // 32
        6, 6, 6, 6, 6, 6, 6, 6, // 40
        5, 5, 5, 5, 5, 5, 5, 2, // 48
        5, 5, 5, 5, 0, 0, 0, 0, // 56
        0, 0, 0, 0, 0, 0, 0, 0, // 64
        0, 0, 0, 0, 0, 0, 0, 0, // 72
        0, 0, 0, 0, 0, 0, 0, 0, // 80
        0, 0, 0, 0, 0, 0, 0, 0, // 88
        0, 0, 0, 0, 0, 0, 0, 0, // 96
        0, 0, 0, 0, 0, 0, 0, 0, // 104
        0, 0, 0, 0, 0, 0, 0, 0, // 112
        1, 1, 1, 3, 1, 1, 1, 5, // 120
        1, 1, 1, 1, 1, 2, 4, 3, // 128

        // 16
    };

    /*
     * private static int[] percussion = { 1, 1, 1, 2, 3, 2, 1, 3, 1, 3, 1, 3, 1, 1, 3, 1, 3, 3, 3, 3, 3, 0, 3, 3, 3, 1, 1, 1, 1, 1, 1, 1, 3, 3, 3, 3,
     * 4, 4, 3, 3, 3, 3, 3, 1, 1, 3, 3, 2, 4, 4, 3, 1, 1, };
     */

    protected final File midiFile;
    private Sequencer sequencer = null;

    public MidiJingleSequencer (File midiFile) throws MidiUnavailableException, InvalidMidiDataException, IOException {

        this.midiFile = midiFile;

        try {
            sequencer = MidiSystem.getSequencer(false);
            sequencer.open();
            Sequence seq = MidiSystem.getSequence(midiFile);
            sequencer.setSequence(seq);
        } catch (MidiUnavailableException e) {
            if (sequencer.isOpen()) {
                sequencer.close();
            }
            throw e;
        } catch (InvalidMidiDataException e) {
            if (sequencer.isOpen()) {
                sequencer.close();
            }
            throw e;
        } catch (IOException e) {
            if (sequencer.isOpen()) {
                sequencer.close();
            }
            throw e;
        }
    }

    // private long lastTime = 0;

    @Override
    public void run (final JingleNotePlayer notePlayer) throws InterruptedException {

        // lastTime = 0;
        final Map<Integer, Integer> patches = new HashMap<Integer, Integer>();
        // final List<Note> notes = new ArrayList<Note>();

        try {
            if (!sequencer.isOpen()) {
                sequencer.open();
            }
            sequencer.getTransmitter().setReceiver(new Receiver() {

                @Override
                public void send (MidiMessage message, long timeStamp) {

                    if ((message.getStatus() & 0xF0) == ShortMessage.PROGRAM_CHANGE) {

                        ShortMessage msg = (ShortMessage) message;
                        int chan = msg.getChannel();
                        int patch = msg.getData1();
                        patches.put(chan, patch);
                    } else if ((message.getStatus() & 0xF0) == ShortMessage.NOTE_ON) {

                        ShortMessage msg = (ShortMessage) message;
                        int chan = msg.getChannel();
                        int n = msg.getData1();
                        if (chan == 9) { // Percussion
                            // Sounds like utter crap
                            // notePlayer.play(toMCPercussion(patches.get(chan)), 10);
                            // notePlayer.play(toMCInstrument(patches.get(chan)), toMCNote(n));
                        } else {
                            // if(msg.getData2() == 0) {
                            // notes.remove(new Note(toMCSound(toMCInstrument(chan)),toMCNote(n),msg.getData2()));
                            // }
                            // else
                            notePlayer.play(new Note(toMCSound(toMCInstrument(chan)), toMCNote(n), msg.getData2()));
                            // notes.add(new Note(toMCSound(toMCInstrument(chan)),toMCNote(n),msg.getData2()));
                        }
                    } else if ((message.getStatus() & 0xF0) == ShortMessage.NOTE_OFF) {

                        // ShortMessage msg = (ShortMessage) message;
                        // int chan = msg.getChannel();
                        // int n = msg.getData1();
                        // notes.remove(new Note(toMCSound(toMCInstrument(chan)),toMCNote(n),msg.getData2()));
                    }

                    /*
                     * if(lastTime < timeStamp) { for(Note note : notes) {
                     * 
                     * notePlayer.play(note); } lastTime = timeStamp; }
                     */
                }

                @Override
                public void close () {

                }
            });

            sequencer.start();

            while (sequencer.isRunning() && notePlayer.isActive()) {
                Thread.sleep(1000);
            }

            if (sequencer.isRunning()) {
                sequencer.stop();
            }
        } catch (MidiUnavailableException e) {
            Bukkit.getLogger().severe(GeneralUtil.getStackTrace(e));
        } finally {
            if (sequencer.isOpen()) {
                sequencer.close();
            }
        }
    }

    @Override
    public void stop () {

        if (sequencer != null && sequencer.isOpen()) {
            sequencer.close();
        }
    }

    private static byte toMCNote (int n) {

        if (n < 54) return (byte) ((n - 6) % (18 - 6));
        else if (n > 78) return (byte) ((n - 6) % (18 - 6) + 12);
        else return (byte) (n - 54);
    }

    private static byte toMCInstrument (Integer patch) {

        if (patch == null) return 0;

        if (patch < 0 || patch >= instruments.length) return 0;

        return (byte) instruments[patch];
    }

    private static Sound toMCSound (byte instrument) {

        switch (instrument) {
            case 0:
                return Sound.NOTE_PIANO;
            case 1:
                return Sound.NOTE_BASS_GUITAR;
            case 2:
                return Sound.NOTE_SNARE_DRUM;
            case 3:
                return Sound.NOTE_STICKS;
            case 4:
                return Sound.NOTE_BASS_DRUM;
            case 5:
                return Sound.NOTE_PLING;
            case 6:
                return Sound.NOTE_BASS;
            default:
                return null;
        }
    }

    /*
     * private static int toMCPercussion(int note) { int i = note - 35; if (i < 0 || i >= percussion.length) { return 1; }
     * 
     * return percussion[i]; }
     */

    public boolean isSongPlaying () {

        return sequencer.isRunning();
    }

    public class Note {

        Sound instrument;
        byte note;
        int velocity;

        public Note (Sound instrument, byte note, int velocity) {

            this.instrument = instrument;
            this.note = note;
            this.velocity = velocity;
        }

        public Sound getInstrument () {

            return instrument;
        }

        public byte getNote () {

            return note;
        }

        public int getVelocity () {

            if (instrument == Sound.NOTE_PLING) return velocity / 4;
            return velocity;
        }

        @Override
        public boolean equals (Object o) {

            if (o instanceof Note) {

                Note n = (Note) o;
                if (n.getInstrument() == getInstrument()) return true;
            }
            return false;
        }

        @Override
        public int hashCode () {
            // Constants correspond to glibc's lcg algorithm parameters
            return (instrument.hashCode() * 1103515245 + 12345) * 1103515245 + 12345;
        }
    }
}