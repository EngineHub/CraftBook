/*
 * CraftBook Copyright (C) 2010-2017 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2017 me4502 <http://www.me4502.com>
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
package com.sk89q.craftbook.sponge.util.jinglenote;

import com.sk89q.craftbook.core.util.jinglenote.Instrument;
import com.sk89q.craftbook.core.util.jinglenote.JingleNotePlayer;
import com.sk89q.craftbook.core.util.jinglenote.JingleSequencer;
import com.sk89q.craftbook.core.util.jinglenote.Note;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.effect.sound.SoundTypes;

import java.util.UUID;

public class SpongeJingleNotePlayer extends JingleNotePlayer {

    public SpongeJingleNotePlayer(UUID player, JingleSequencer seq) {
        super(player, seq);
    }

    @Override
    public void play(Note note) {
        if(!isPlaying()) return;

        Sponge.getServer().getPlayer(getPlayer()).ifPresent(player -> player.playSound(toSound(note.getInstrument()), player.getLocation().getPosition(), note.getVelocity(), note.getNote()));
    }

    private static SoundType toSound(Instrument instrument) {
        switch(instrument) {
            case PIANO:
                return SoundTypes.BLOCK_NOTE_HARP;
            case BASS:
                return SoundTypes.BLOCK_NOTE_BASS;
            case SNARE_DRUM:
                return SoundTypes.BLOCK_NOTE_SNARE;
            case STICKS:
                return SoundTypes.BLOCK_NOTE_HAT;
            case BASS_DRUM:
                return SoundTypes.BLOCK_NOTE_BASEDRUM;
            case BELL:
                return SoundTypes.BLOCK_NOTE_BELL;
            case CHIME:
                return SoundTypes.BLOCK_NOTE_CHIME;
            case FLUTE:
                return SoundTypes.BLOCK_NOTE_FLUTE;
            case XYLOPHONE:
                return SoundTypes.BLOCK_NOTE_XYLOPHONE;
            case PLING:
                return SoundTypes.BLOCK_NOTE_PLING;
            default:
                return SoundTypes.BLOCK_NOTE_HARP;
        }
    }
}
