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

    private static Sound toSound(Instrument instrument) {
        switch(instrument) {
            case PIANO:
                return Sound.BLOCK_NOTE_BLOCK_HARP;
            case BASS:
                return Sound.BLOCK_NOTE_BLOCK_BASS;
            case SNARE_DRUM:
                return Sound.BLOCK_NOTE_BLOCK_SNARE;
            case STICKS:
                return Sound.BLOCK_NOTE_BLOCK_HAT;
            case BASS_DRUM:
                return Sound.BLOCK_NOTE_BLOCK_BASEDRUM;
            case BELL:
                return Sound.BLOCK_NOTE_BLOCK_BELL;
            case CHIME:
                return Sound.BLOCK_NOTE_BLOCK_CHIME;
            case FLUTE:
                return Sound.BLOCK_NOTE_BLOCK_FLUTE;
            case XYLOPHONE:
                return Sound.BLOCK_NOTE_BLOCK_XYLOPHONE;
            case PLING:
                return Sound.BLOCK_NOTE_BLOCK_PLING;
            case GUITAR:
                return Sound.BLOCK_NOTE_BLOCK_GUITAR;
            default:
                return Sound.BLOCK_NOTE_BLOCK_HARP;
        }
    }
}