package com.sk89q.craftbook.circuits.jinglenote;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Sound;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;

/**
 * @author Me4502 with code borrowed from CraftBook Extra
 */
public class StringJingleSequencer implements JingleSequencer {

    String tune;
    int delay;
    int position;
    int taskID;
    boolean isPlaying = false;

    List<Note> song;

    public StringJingleSequencer(String tune, int delay) {

        this.tune = tune;
        this.delay = delay;
        song = parseTune(tune);
    }

    @Override
    public void run(final JingleNotePlayer player) throws InterruptedException {

        position = 0;

        isPlaying = true;
        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(CraftBookPlugin.inst(), new Runnable() {

            @Override
            public void run() {

                if (position >= song.size()) {
                    Bukkit.getScheduler().cancelTask(taskID);
                    isPlaying = false;
                    return;
                }
                player.play(song.get(position));
                position++;
            }

        }, delay, delay);
    }

    public ArrayList<Note> parseTune(String tune) {

        if (tune == null) return null;

        ArrayList<Note> musicKeys = new ArrayList<Note>();

        byte instrument = -1;
        for (int i = 0; i < tune.length(); i++) {
            char first = tune.charAt(i);
            if (first >= '0' && first <= '9') {
                // instrument?
                instrument = getTypeFromChar(first);
            } else if (i + 1 < tune.length()) {
                // note?
                if (instrument == -1) return null;

                int pitch = getPitchFromChar(first);
                boolean skip = false;
                if (pitch == -1) {
                    switch (first) {
                        case '-':
                        case ' ':
                            skip = true;
                            break;
                        default:
                            return null;
                    }
                }

                int octave;
                try {
                    octave = Integer.parseInt(Character.toString(tune.charAt(i + 1)));
                } catch (NumberFormatException e) {
                    octave = 2;
                }

                if (skip) {
                    musicKeys.add(new Note(Sound.FIZZ, (byte) 0, 0));
                    if (octave == 0) octave = 10;

                } else {

                    if (octave < 2) octave = 2;

                    pitch += (octave - 2) * 12;

                    if (pitch < 0) pitch = 0;
                    else if (pitch > 24) pitch = 24;

                    musicKeys.add(new Note(toMCSound(instrument), (byte) pitch, 60F));
                }

                i++;
            }
        }

        if (musicKeys.size() == 0) return null;

        return musicKeys;
    }

    public byte getTypeFromChar(char type) {

        byte instrument = -1;
        switch (type) {
            case '9':
            case '8':
            case '7':
            case '0':
                instrument = 0;
                break;
            case '1':
                instrument = 1;
                break;
            case '2':
                instrument = 2;
                break;
            case '3':
                instrument = 3;
                break;
            case '4':
                instrument = 4;
                break;
            case '5':
                instrument = 5;
                break;
            case '6':
                instrument = 6;
                break;
        }

        return instrument;
    }

    public int getPitchFromChar(char charPitch) {

        int pitch = 0;
        switch (charPitch) {
            case 'f':
                pitch++;
            case 'e':
                pitch++;
            case 'D':
                pitch++;
            case 'd':
                pitch++;
            case 'C':
                pitch++;
            case 'c':
                pitch++;
            case 'b':
                pitch++;
            case 'A':
                pitch++;
            case 'a':
                pitch++;
            case 'G':
                pitch++;
            case 'g':
                pitch++;
            case 'F':
                break;
            default:
                pitch = -1;
                break;
        }

        return pitch;
    }

    public Sound toMCSound(byte instrument) {

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
                return Sound.NOTE_PIANO;
        }
    }

    @Override
    public void stop() {

    }

    public boolean isSongPlaying () {
        return isPlaying;
    }
}
