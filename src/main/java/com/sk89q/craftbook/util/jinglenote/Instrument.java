package com.sk89q.craftbook.util.jinglenote;

import org.bukkit.Sound;

public enum Instrument {

    /*
        0 = BLOCK_NOTE_HARP = PIANO
        1 = BLOCK_NOTE_BASS = BASS
        2 = BLOCK_NOTE_SNARE = SNARE_DRUM
        3 = BLOCK_NOTE_HAT = STICKS
        4 = BLOCK_NOTE_BASEDRUM = BASS_DRUM
        5 = BLOCK_NOTE_GUITAR = GUITAR
        6 = BLOCK_NOTE_BELL = BELL
        7 = BLOCK_NOTE_CHIME = CHIME
        8 = BLOCK_NOTE_FLUTE = FLUTE
        9 = BLOCK_NOTE_XYLOPHONE = XYLOPHONE
        10 = BLOCK_NOTE_PLING = PLING
     */

    PIANO, BASS, SNARE_DRUM, STICKS, BASS_DRUM, GUITAR,
    BELL, CHIME, FLUTE, XYLOPHONE, PLING;

    public static Instrument toMCSound(byte instrument) {
        switch (instrument) {
            case 0:
                return Instrument.PIANO;
            case 1:
                return Instrument.BASS;
            case 2:
                return Instrument.SNARE_DRUM;
            case 3:
                return Instrument.STICKS;
            case 4:
                return Instrument.BASS_DRUM;
            case 5:
                return Instrument.GUITAR;
            case 6:
                return Instrument.BELL;
            case 7:
                return Instrument.CHIME;
            case 8:
                return Instrument.FLUTE;
            case 9:
                return Instrument.XYLOPHONE;
            case 10:
                return Instrument.PLING;
            default:
                return Instrument.PIANO;
        }
    }
}