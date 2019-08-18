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

public class Note {
    private Instrument instrument;
    private byte note;
    private float velocity;

    public Note(Instrument instrument, byte note, float velocity) {
        this.instrument = instrument;
        this.note = note;
        this.velocity = velocity;
    }

    public Instrument getInstrument() {
        return this.instrument;
    }

    public float getNote() {
        return (float) Math.pow(2.0D, (this.note - 12) / 12.0D);
    }

    public float getVelocity() {
        return this.velocity;
    }
}
