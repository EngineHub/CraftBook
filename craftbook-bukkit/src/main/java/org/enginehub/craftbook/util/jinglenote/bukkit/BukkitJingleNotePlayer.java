/*
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

package org.enginehub.craftbook.util.jinglenote.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.enginehub.craftbook.util.SearchArea;
import org.enginehub.craftbook.util.jinglenote.Instrument;
import org.enginehub.craftbook.util.jinglenote.JingleNotePlayer;
import org.enginehub.craftbook.util.jinglenote.JingleSequencer;
import org.enginehub.craftbook.util.jinglenote.JingleSequencer.Note;

public class BukkitJingleNotePlayer extends JingleNotePlayer {

    public BukkitJingleNotePlayer(String player, JingleSequencer seq, SearchArea area) {
        super(player, seq, area);
    }

    private Player p = null;

    @Override
    public void play(Note note) {

        if (!isPlaying()) return;

        p.playSound(p.getLocation(), toSound(note.getInstrument()), SoundCategory.RECORDS, note.getVelocity(), note.getNote());
    }

    @Override
    public boolean isPlaying() {

        if (p == null || !p.isOnline()) {
            p = Bukkit.getPlayerExact(player);
        }
        return !(p == null || !p.isOnline() || area != null && !area.isWithinArea(p.getLocation())) && super.isPlaying();

    }

    private static Sound toSound(Instrument instrument) {
        return switch (instrument) {
            case BASS -> Sound.BLOCK_NOTE_BLOCK_BASS;
            case SNARE -> Sound.BLOCK_NOTE_BLOCK_SNARE;
            case HAT -> Sound.BLOCK_NOTE_BLOCK_HAT;
            case BANJO -> Sound.BLOCK_NOTE_BLOCK_BANJO;
            case BASEDRUM -> Sound.BLOCK_NOTE_BLOCK_BASEDRUM;
            case BELL -> Sound.BLOCK_NOTE_BLOCK_BELL;
            case BIT -> Sound.BLOCK_NOTE_BLOCK_BIT;
            case CHIME -> Sound.BLOCK_NOTE_BLOCK_CHIME;
            case COW_BELL -> Sound.BLOCK_NOTE_BLOCK_COW_BELL;
            case DIDGERIDOO -> Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO;
            case FLUTE -> Sound.BLOCK_NOTE_BLOCK_FLUTE;
            case XYLOPHONE -> Sound.BLOCK_NOTE_BLOCK_XYLOPHONE;
            case IRON_XYLOPHONE -> Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE;
            case PLING -> Sound.BLOCK_NOTE_BLOCK_PLING;
            case GUITAR -> Sound.BLOCK_NOTE_BLOCK_GUITAR;
            case TRUMPET -> Sound.BLOCK_NOTE_BLOCK_TRUMPET;
            case TRUMPET_DISTORTED -> Sound.BLOCK_NOTE_BLOCK_TRUMPET_EXPOSED;
            case TROMBONE -> Sound.BLOCK_NOTE_BLOCK_TRUMPET_OXIDIZED;
            case TROMBONE_DISTORTED -> Sound.BLOCK_NOTE_BLOCK_TRUMPET_WEATHERED;
            default -> Sound.BLOCK_NOTE_BLOCK_HARP;
        };
    }
}
