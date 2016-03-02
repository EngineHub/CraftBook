package com.sk89q.craftbook.util.jinglenote.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
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

    Player p = null;

    @Override
    public void play (Note note)  {

        if(!isPlaying()) return;

        p.playSound(p.getLocation(), toSound(note.getInstrument()), note.getVelocity(), note.getNote());
    }

    @Override
    public boolean isPlaying() {

        if (p == null || !p.isOnline()) {
            p = Bukkit.getPlayerExact(player);
        }
        return !(p == null || !p.isOnline() || area != null && !area.isWithinArea(p.getLocation())) && super.isPlaying();

    }

    public Sound toSound(Instrument instrument) {

        switch(instrument) {
            case PIANO:
                return Sound.BLOCK_NOTE_HARP;
            case GUITAR:
                return Sound.BLOCK_NOTE_PLING;
            case BASS:
                return Sound.BLOCK_NOTE_BASS;
            case BASS_GUITAR:
                return Sound.BLOCK_NOTE_BASS;
            case STICKS:
                return Sound.BLOCK_NOTE_HAT;
            case BASS_DRUM:
                return Sound.BLOCK_NOTE_BASEDRUM;
            case SNARE_DRUM:
                return Sound.BLOCK_NOTE_SNARE;
            default:
                return Sound.BLOCK_NOTE_HARP;
        }
    }
}