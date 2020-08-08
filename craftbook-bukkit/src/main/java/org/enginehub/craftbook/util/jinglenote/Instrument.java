/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
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

package org.enginehub.craftbook.util.jinglenote;

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