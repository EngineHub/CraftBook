// $Id$
/*
 * Tetsuuuu plugin for SK's Minecraft Server
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 * All rights reserved.
 */

package com.sk89q.jinglenote;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class JingleNotePlayer implements Runnable {
    protected final Player player;
    protected final Location loc;
    protected JingleSequencer sequencer;
    protected final int delay;

    public JingleNotePlayer(Player player,
            Location loc, JingleSequencer seq,  int delay) {
        this.player = player;
        this.loc = loc;
        sequencer = seq;
        this.delay = delay;
    }

    @Override
    public void run() {
        try {
            if (delay > 0) {
                Thread.sleep(delay);
            }

            try {
                sequencer.run(this);
            } catch (Throwable t) {
                t.printStackTrace();
            }

            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
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

    public Location getLocation() {
        return loc;
    }

    public void stop(boolean keepMusicBlock) {

        if (sequencer != null) {
            sequencer.stop();
        }
    }

    /*public void play(byte instrument, byte note) {
        if (!player.isOnline()) {
            return;
        }
        player.playNote(loc, instrument, note);
    }*/

    public void play(Sound instrument, int pitch) {
        if (!player.isOnline() || instrument == null) {
            return;
        }
        float np = (float)Math.pow(2.0D, (pitch - 12) / 12.0D);
        player.playSound(loc, instrument, 30f, np);
    }
}