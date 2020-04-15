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

package com.sk89q.craftbook.util.jinglenote.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.util.SearchArea;
import com.sk89q.craftbook.util.jinglenote.Instrument;
import com.sk89q.craftbook.util.jinglenote.JingleNotePlayer;
import com.sk89q.craftbook.util.jinglenote.JingleSequencer;
import com.sk89q.craftbook.util.jinglenote.JingleSequencer.Note;

public class BukkitJingleNotePlayer extends JingleNotePlayer {

    public BukkitJingleNotePlayer (String player, JingleSequencer seq, SearchArea area) {
        super(player, seq, area);
    }

    private Player p = null;

    @Override
    public void play (Note note)  {

        if(!isPlaying()) return;

        p.playSound(p.getLocation(), toSound(note.getInstrument()), SoundCategory.RECORDS, note.getVelocity(), note.getNote());
    }

    @Override
    public boolean isPlaying() {

        if (p == null || !p.isOnline()) {
            p = Bukkit.getPlayerExact(player);
        }
        return !(p == null || !p.isOnline() || area != null && !area.isWithinArea(p.getLocation())) && super.isPlaying();

    }

    private static Sound BANJO;
    private static Sound BIT;
    private static Sound COWBELL;
    private static Sound DIDGERIDOO;
    private static Sound IRON_XYLOPHONE;

    static {
        try {
            for (Sound sound : Sound.class.getEnumConstants()) {
                switch (sound.name()) {
                    case "BLOCK_NOTE_BLOCK_BANJO":
                        BANJO = sound;
                        break;
                    case "BLOCK_NOTE_BLOCK_BIT":
                        BIT = sound;
                        break;
                    case "BLOCK_NOTE_BLOCK_COW_BELL":
                        COWBELL = sound;
                        break;
                    case "BLOCK_NOTE_BLOCK_DIDGERIDOO":
                        DIDGERIDOO = sound;
                        break;
                    case "BLOCK_NOTE_BLOCK_IRON_XYLOPHONE":
                        IRON_XYLOPHONE = sound;
                        break;
                }
            }
            if (BANJO == null) {
                throw new RuntimeException();
            }
        } catch (Throwable t) {
            BANJO = Sound.BLOCK_NOTE_BLOCK_GUITAR;
            BIT = Sound.BLOCK_NOTE_BLOCK_PLING;
            COWBELL = Sound.BLOCK_NOTE_BLOCK_BELL;
            DIDGERIDOO = Sound.BLOCK_NOTE_BLOCK_BASS;
            IRON_XYLOPHONE = Sound.BLOCK_NOTE_BLOCK_XYLOPHONE;
        }
    }

    private static Sound toSound(Instrument instrument) {
        switch(instrument) {
            case BASS:
                return Sound.BLOCK_NOTE_BLOCK_BASS;
            case SNARE:
                return Sound.BLOCK_NOTE_BLOCK_SNARE;
            case HAT:
                return Sound.BLOCK_NOTE_BLOCK_HAT;
            case BANJO:
                return BANJO;
            case BASEDRUM:
                return Sound.BLOCK_NOTE_BLOCK_BASEDRUM;
            case BELL:
                return Sound.BLOCK_NOTE_BLOCK_BELL;
            case BIT:
                return BIT;
            case CHIME:
                return Sound.BLOCK_NOTE_BLOCK_CHIME;
            case COW_BELL:
                return COWBELL;
            case DIDGERIDOO:
                return DIDGERIDOO;
            case FLUTE:
                return Sound.BLOCK_NOTE_BLOCK_FLUTE;
            case XYLOPHONE:
                return Sound.BLOCK_NOTE_BLOCK_XYLOPHONE;
            case IRON_XYLOPHONE:
                return IRON_XYLOPHONE;
            case PLING:
                return Sound.BLOCK_NOTE_BLOCK_PLING;
            case GUITAR:
                return Sound.BLOCK_NOTE_BLOCK_GUITAR;
            case HARP:
            default:
                return Sound.BLOCK_NOTE_BLOCK_HARP;
        }
    }
}