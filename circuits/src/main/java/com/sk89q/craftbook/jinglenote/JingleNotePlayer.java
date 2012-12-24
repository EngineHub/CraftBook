// $Id$
/*
 * Tetsuuuu plugin for SK's Minecraft Server
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 * All rights reserved.
 */

package com.sk89q.craftbook.jinglenote;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.jinglenote.MidiJingleSequencer.Note;
import com.sk89q.craftbook.util.GeneralUtil;

public class JingleNotePlayer implements Runnable {

    protected final Player player;
    protected JingleSequencer sequencer;

    public JingleNotePlayer(Player player, JingleSequencer seq) {

        this.player = player;
        sequencer = seq;
    }

    @Override
    public void run() {

        try {
            try {
                sequencer.run(this);
            } catch (Throwable t) {
                Bukkit.getLogger().severe(GeneralUtil.getStackTrace(t));
            }

            Thread.sleep(500);
        } catch (InterruptedException e) {
            Bukkit.getLogger().severe(GeneralUtil.getStackTrace(e));
        } finally {
            sequencer.stop();
            sequencer = null;
        }
    }

    public boolean isActive() {

        return player.isOnline();
    }

    public Player getPlayer() {

        return player;
    }

    public void stop() {

        if (sequencer != null) {
            sequencer.stop();
        }
    }

    public void play(Sound instrument, int pitch) {

        if (!player.isOnline() || instrument == null) {
            if(!player.isOnline())
                stop();
            return;
        }
        float np = (float) Math.pow(2.0D, (pitch - 12) / 12.0D);
        player.playSound(player.getLocation(), instrument, instrument == Sound.NOTE_PLING ? 7F : 30F, np);
    }

    public void play(Note note) {

        if (!player.isOnline() || note == null || note.getInstrument() == null) {
            if(!player.isOnline())
                stop();
            return;
        }
        float np = (float) Math.pow(2.0D, (note.getNote() - 12) / 12.0D);
        player.playSound(player.getLocation(), note.getInstrument(), note.getVelocity(), np);
    }
}