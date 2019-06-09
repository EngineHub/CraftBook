package com.sk89q.craftbook.util.jinglenote;

public enum Instrument {

    BANJO,
    BASEDRUM,
    BASS,
    BELL,
    BIT,
    CHIME,
    COW_BELL,
    DIDGERIDOO,
    FLUTE,
    GUITAR,
    HARP,
    HAT,
    IRON_XYLOPHONE,
    PLING,
    SNARE,
    XYLOPHONE;

    public static Instrument toMCSound(byte instrument) {
        switch (instrument) {
            case 1:
                return Instrument.BASS;
            case 2:
                return Instrument.SNARE;
            case 3:
                return Instrument.HAT;
            case 4:
                return Instrument.BASEDRUM;
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
            case 11:
                return Instrument.BANJO;
            case 12:
                return Instrument.BIT;
            case 13:
                return Instrument.COW_BELL;
            case 14:
                return Instrument.DIDGERIDOO;
            case 15:
                return Instrument.IRON_XYLOPHONE;
            case 0:
            default:
                return Instrument.HARP;
        }
    }
}