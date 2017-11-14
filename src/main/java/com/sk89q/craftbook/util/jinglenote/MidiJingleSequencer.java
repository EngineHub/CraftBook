// $Id$
/*
 * Tetsuuuu plugin for SK's Minecraft Server Copyright (C) 2010 sk89q <http://www.sk89q.com> All rights reserved.
 */

package com.sk89q.craftbook.util.jinglenote;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.mechanics.ic.ICMechanic;

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

/**
 * A sequencer that reads MIDI files.
 *
 * @author sk89q
 * @author me4502
 */
public final class MidiJingleSequencer implements JingleSequencer {

    private static final int[] instruments = {
        0, 0, 0, 0, 0, 0, 0, 5, // 8
        9, 9, 9, 9, 9, 6, 0, 9, // 16
        9, 0, 0, 0, 0, 0, 0, 5, // 24
        5, 5, 5, 5, 5, 5, 5, 1, // 32
        1, 1, 1, 1, 1, 1, 1, 5, // 40
        1, 5, 5, 5, 5, 5, 5, 5, // 48
        5, 5, 5, 8, 8, 8, 8, 8, // 56
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

    private Sequencer sequencer;
    private boolean running = false;
    private boolean playedBefore = false;

    private static final Object PLAYER_LOCK = new Object();

    private volatile Set<JingleNotePlayer> players = new HashSet<>();

    public MidiJingleSequencer(File midiFile, boolean loop) throws MidiUnavailableException, InvalidMidiDataException, IOException {
        try {
            sequencer = MidiSystem.getSequencer(false);
            sequencer.open();
            Sequence seq = MidiSystem.getSequence(midiFile);
            sequencer.setSequence(seq);
            if(loop)
                sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
        } catch (MidiUnavailableException | IOException | InvalidMidiDataException e) {
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
                    if (getPlayerCount() == 0) {
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
                        synchronized(PLAYER_LOCK) {
                            if (chan == 9) { // Percussion
                                // Sounds like utter crap
                                if(ICMechanic.instance.usePercussionMidi)
                                    for(JingleNotePlayer player : players)
                                        player.play(new Note(Instrument.toMCSound(toMCPercussion(patches.get(chan))), toMCNote(n),  10 * (msg.getData2() / 127f)));
                            } else {
                                for(JingleNotePlayer player : players)
                                    player.play(new Note(Instrument.toMCSound(toMCInstrument(patches.get(chan))), toMCNote(n), 10 * (msg.getData2() / 127f)));
                            }
                        }
                    }
                }

                @Override
                public void close() {
                    running = false;
                }
            });

            sequencer.addMetaEventListener(meta -> {
                // END_OF_TRACK_MESSAGE
                if (meta.getType() == 47) {
                    running = false;
                }
            });

            try {
                if (sequencer.isOpen()) {
                    sequencer.start();
                    running = true;
                    playedBefore = true;
                    if (CraftBookPlugin.inst().getConfiguration().debugMode) {
                        synchronized (PLAYER_LOCK) {
                            for (JingleNotePlayer player : players)
                                CraftBookPlugin.logDebugMessage("Opening midi sequencer: " + player.player, "midi");
                        }
                    }
                } else
                    throw new IllegalArgumentException("Sequencer is not open!");
            } catch(Exception e){
                BukkitUtil.printStacktrace(e);
            }
        } catch (MidiUnavailableException e) {
            BukkitUtil.printStacktrace(e);
        }
    }

    @Override
    public void stop() {

        if(!running) return;
        synchronized(PLAYER_LOCK) {
            players.clear();
        }
        CraftBookPlugin.logDebugMessage("Stopping MIDI sequencer. (Stop called)", "midi");
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

        if(patch == null)
            return 0;

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
        synchronized(PLAYER_LOCK) {
            players.remove(player);
        }

        if (this.getPlayerCount() == 0) {
            stop();
        }
    }

    @Override
    public void play (JingleNotePlayer player) {
        synchronized(PLAYER_LOCK) {
            players.add(player);
        }
        if(!playedBefore) {
            run();
        }
    }

    @Override
    public int getPlayerCount() {
        return this.players.size();
    }

    @Override
    public Set<JingleNotePlayer> getPlayers () {
        Set<JingleNotePlayer> copy;
        synchronized(PLAYER_LOCK) {
            copy = new HashSet<>(players);
        }
        return copy;
    }
}