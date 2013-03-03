package com.sk89q.craftbook.circuits.jinglenote.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.circuits.jinglenote.Instrument;
import com.sk89q.craftbook.circuits.jinglenote.JingleNotePlayer;
import com.sk89q.craftbook.circuits.jinglenote.JingleSequencer;
import com.sk89q.craftbook.circuits.jinglenote.JingleSequencer.Note;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.bukkit.BukkitUtil;

public class BukkitJingleNotePlayer extends JingleNotePlayer {

    public BukkitJingleNotePlayer (String player, JingleSequencer seq, WorldVector centre, int radius) {
        super(player, seq, centre, radius);
    }

    Player p = null;

    @Override
    public void play (Note note)  {

        if (p == null || !p.isOnline())
            p = Bukkit.getPlayer(player);

        if (p == null || !p.isOnline() || note == null) {
            return;
        }

        if(centre != null && radius > 0) {
            if(!LocationUtil.isWithinRadius(BukkitUtil.toLocation(centre), p.getLocation(), new Vector(radius,radius,radius)))
                return;
        }

        p.playSound(p.getLocation(), toSound(note.getInstrument()), note.getVelocity(), note.getNote());
    }

    public Sound toSound(Instrument instrument) {

        switch(instrument) {
            case PIANO:
                return Sound.NOTE_PIANO;
            case GUITAR:
                return Sound.NOTE_PLING;
            case BASS:
                return Sound.NOTE_BASS;
            case BASS_GUITAR:
                return Sound.NOTE_BASS_GUITAR;
            case STICKS:
                return Sound.NOTE_STICKS;
            case BASS_DRUM:
                return Sound.NOTE_BASS_DRUM;
            case SNARE_DRUM:
                return Sound.NOTE_SNARE_DRUM;
            default:
                return Sound.NOTE_PIANO;
        }
    }
}