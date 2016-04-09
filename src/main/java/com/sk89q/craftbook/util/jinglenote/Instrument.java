package com.sk89q.craftbook.util.jinglenote;

public enum Instrument {

    GUITAR, PIANO, BASS, BASS_GUITAR, STICKS, BASS_DRUM, SNARE_DRUM;

    public static Instrument toMCSound(byte instrument) {
        switch (instrument) {
            case 1:
                return Instrument.BASS_GUITAR;
            case 2:
                return Instrument.SNARE_DRUM;
            case 3:
                return Instrument.STICKS;
            case 4:
                return Instrument.BASS_DRUM;
            case 5:
                return Instrument.GUITAR;
            case 6:
                return Instrument.BASS;
            default:
                return Instrument.PIANO;
        }
    }
}