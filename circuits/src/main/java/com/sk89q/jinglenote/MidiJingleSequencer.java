// $Id$
/*
 * Tetsuuuu plugin for SK's Minecraft Server
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 * All rights reserved.
 */

package com.sk89q.jinglenote;

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

/**
 * A sequencer that reads MIDI files.
 * 
 * @author sk89q
 */
public class MidiJingleSequencer implements JingleSequencer {

    private static final int[] instruments = { //Is this right? TODO check.
	0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 2,
	0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0,
	1, 1, 1, 1, 1, 1, 1, 1,
	1, 1, 1, 1, 1, 2, 4, 3,
    };

    /*private static int[] percussion = {
        1, 1, 1,
        2, 3, 2, 1, 3, 1, 3, 1, 3, 1,
        1, 3, 1, 3, 3, 3, 3, 3, 0, 3,
        3, 3, 1, 1, 1, 1, 1, 1, 1, 3,
        3, 3, 3, 4, 4, 3, 3, 3, 3, 3,
        1, 1, 3, 3, 2, 4, 4, 3, 1, 1,
    };*/

    protected final File midiFile;
    private Sequencer sequencer = null;

    public MidiJingleSequencer(File midiFile) throws MidiUnavailableException,
    InvalidMidiDataException, IOException {
	this.midiFile = midiFile;

	try {
	    sequencer = MidiSystem.getSequencer(false);
	    sequencer.open();
	    Sequence seq = MidiSystem.getSequence(midiFile);
	    sequencer.setSequence(seq);
	} catch (MidiUnavailableException e) {
	    sequencer.close();
	    throw e;
	} catch (InvalidMidiDataException e) {
	    sequencer.close();
	    throw e;
	} catch (IOException e) {
	    sequencer.close();
	    throw e;
	}
    }

    public void run(final JingleNotePlayer notePlayer)
	    throws InterruptedException {

	final Map<Integer, Integer> patches = new HashMap<Integer, Integer>();

	try {
	    sequencer.getTransmitter().setReceiver(new Receiver() {
		public void send(MidiMessage message, long timeStamp) {
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
			    //notePlayer.play(toMCPercussion(patches.get(chan)), toMCNote(n));
			} else {
			    notePlayer.play(toMCInstrument(patches.get(chan)), toMCNote(n));
			}
		    }
		}

		public void close() {
		}
	    });

	    sequencer.start();

	    while (sequencer.isRunning() && notePlayer.isActive()) {
		Thread.sleep(1000);
	    }

	    sequencer.stop();
	} catch (MidiUnavailableException e) {
	    e.printStackTrace();
	} finally {
	    sequencer.close();
	}
    }

    public void stop() {
	try
	{
	    if (sequencer != null && sequencer.isOpen()) {
		sequencer.close();
	    }
	}
	catch(Exception e){}
    }

    private static byte toMCNote(int n) {
	if (n < 54) {
	    return (byte) ((n - 6) % (18 - 6));
	} else if (n > 78) {
	    return (byte) ((n - 6) % (18-6) + 12);
	} else {
	    return (byte) (n - 54);
	}
    }

    private static byte toMCInstrument(Integer patch) {
	if (patch == null) {
	    return 0;
	}

	if (patch < 0 || patch >= instruments.length) {
	    return 0;
	}

	return (byte) instruments[patch];
    }

    /*private static byte toMCPercussion(int note) {
        int i = note - 35;
        if (i < 0 || i >= percussion.length) {
            return 1;
        }

        return (byte) percussion[i];
    }*/
}
