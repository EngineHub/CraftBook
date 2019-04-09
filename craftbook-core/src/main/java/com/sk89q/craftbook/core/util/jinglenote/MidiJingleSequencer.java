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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;

public final class MidiJingleSequencer implements JingleSequencer {

    private static final int[] instruments = {
            0, 0, 0, 0, 0, 0, 0, 5, // 8
            9, 9, 9, 9, 9, 6, 0, 9, // 16
            9, 0, 0, 0, 0, 0, 0, 5, // 24
            5, 5, 5, 5, 5, 5, 5, 1, // 32
            1, 1, 1, 1, 1, 1, 1, 5, // 40
            1, 5, 5, 5, 5, 5, 5, 5, // 48
            10, 5, 5, 8, 8, 8, 8, 8, // 56
            8, 8, 8, 8, 8, 8, 8, 8, // 64
            8, 8, 8, 8, 8, 8, 8, 8, // 72
            8, 8, 8, 8, 8, 8, 8, 8, // 80
            0, 0, 0, 0, 0, 0, 0, 0, // 88
            0, 0, 0, 0, 0, 0, 0, 0, // 96
            0, 0, 0, 0, 0, 0, 0, 5, // 104
            5, 5, 5, 9, 8, 5, 8, 6, // 112
            6, 3, 3, 2, 2, 2, 6, 5, // 120
            1, 1, 1, 6, 1, 2, 4, 7, // 128
    };


    private static final int[] percussion = {
            9, 6, 4, 4, 3, 2, 3, 2, //40 - Electric Snare
            2, 2, 2, 2, 2, 2, 2, 2, //48 - Hi Mid Tom
            7, 2, 7, 7, 6, 3, 7, 6, //56 - Cowbell
            7, 3, 7, 2, 2, 3, 3, 3, //64 - Low Conga
            2, 2, 6, 6, 2, 2, 0, 0, //72 - Long Whistle
            3, 3, 3, 3, 3, 3, 5, 5, //80 - Open Cuica
            10, 10,                 //82 - Open Triangle
    };

    private Sequencer sequencer = null;
    private boolean running = false;
    private boolean playedBefore = false;

    private Set<JingleNotePlayer> players = new HashSet<>();

    public MidiJingleSequencer(File midiFile, boolean loop) throws MidiUnavailableException, InvalidMidiDataException, IOException {
        try {
            sequencer = MidiSystem.getSequencer(false);
            sequencer.open();
            Sequence seq = MidiSystem.getSequence(midiFile);
            sequencer.setSequence(seq);
            if(loop)
                sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
        } catch (MidiUnavailableException | InvalidMidiDataException | IOException e) {
            stop();
            throw e;
        }
    }

    @Override
    public void run() {

        final Map<Integer, Integer> patches = new HashMap<>();

        try {
            if(sequencer == null || sequencer.getSequence() == null)
                return;

            if (!sequencer.isOpen())
                sequencer.open();

            sequencer.getTransmitter().setReceiver(new Receiver() {

                @Override
                public void send(MidiMessage message, long timeStamp) {
                    if(players.isEmpty()) {
                        running = false;
                        return;
                    }

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
                            for(JingleNotePlayer player : players)
                                player.play(new Note(Instrument.toMCSound(toMCPercussion(patches.get(chan))), toMCNote(n),  10 * (msg.getData2() / 127f)));
                        } else {
                            for(JingleNotePlayer player : players)
                                player.play(new Note(Instrument.toMCSound(toMCInstrument(patches.get(chan))), toMCNote(n), 10 * (msg.getData2() / 127f)));
                        }
                    }
                }

                @Override
                public void close() {
                    running = false;
                }
            });

            try {
                if (sequencer.isOpen()) {
                    sequencer.start();
                    running = true;
                    playedBefore = true;
                } else {
                    throw new IllegalArgumentException("Sequencer is not open!");
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        if(!running) return;
        players.clear();
        if (sequencer != null) {
            try {
                if(sequencer.isRunning())
                    sequencer.stop();
                if(sequencer.isOpen())
                    sequencer.close();
                sequencer = null;
            } catch(Exception ignored){}
        }
        running = false;
    }

    private static byte toMCNote(int n) {
        if (n < 54) return (byte) ((n - 6) % (18 - 6));
        else if (n > 78) return (byte) ((n - 6) % (18 - 6) + 12);
        else return (byte) (n - 54);
    }

    private static byte toMCInstrument(Integer patch) {
        if (patch == null) return 0;

        if (patch < 0 || patch >= instruments.length) return 0;

        return (byte) instruments[patch];
    }

    private static byte toMCPercussion(Integer patch) {
        if(patch == null) {
            return 0;
        }

        int i = patch - 33;
        if (i < 0 || i >= percussion.length) {
            return 1;
        }

        return (byte) percussion[i];
    }

    Sequencer getSequencer() {
        return sequencer;
    }

    @Override
    public boolean isPlaying () {
        return running && sequencer != null;
    }

    @Override
    public boolean hasPlayedBefore () {
        return playedBefore;
    }

    @Override
    public void stop (JingleNotePlayer player) {
        players.remove(player);
        if(players.isEmpty()) {
            stop();
        }
    }

    @Override
    public void play (JingleNotePlayer player) {
        players.add(player);
        if(!playedBefore) {
            run();
        }
    }

    @Override
    public Set<JingleNotePlayer> getPlayers () {
        return players;
    }
}
