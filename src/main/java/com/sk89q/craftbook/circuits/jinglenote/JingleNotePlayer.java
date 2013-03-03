// $Id$
/*
 * Tetsuuuu plugin for SK's Minecraft Server Copyright (C) 2010 sk89q <http://www.sk89q.com> All rights reserved.
 */

package com.sk89q.craftbook.circuits.jinglenote;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.jinglenote.JingleSequencer.Note;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.worldedit.Vector;

public class JingleNotePlayer implements Runnable {

    protected final String player;
    protected JingleSequencer sequencer;
    protected Location centre;
    protected int radius;

    /**
     * Constructs a new JingleNotePlayer
     * 
     * @param player The player who is hearing this's name.
     * @param seq The JingleSequencer to play.
     * @param centre The source of the sound. (Optional)
     * @param radius The radius this sound can be heard from. 0 or less means limitless.
     */
    public JingleNotePlayer(String player, JingleSequencer seq, Location centre, int radius) {

        this.player = player;
        sequencer = seq;
        this.centre = centre;
        this.radius = radius;
    }

    @Override
    public void run() {

        try {
            try {
                sequencer.run(this);
            } catch (Throwable t) {
                BukkitUtil.printStacktrace(t);
            }

            Thread.sleep(500);
        } catch (InterruptedException e) {
            BukkitUtil.printStacktrace(e);
        } finally {
            sequencer.stop();
            sequencer = null;
        }
    }

    public String getPlayer() {

        return player;
    }

    public void stop() {

        if (sequencer != null) {
            sequencer.stop();
        }
    }

    Player p = null;

    public void play(Note note) {

        if (p == null || !p.isOnline())
            p = Bukkit.getPlayer(player);

        if (p == null || !p.isOnline() || note == null) {
            return;
        }

        if(centre != null && radius > 0) {
            if(!LocationUtil.isWithinRadius(centre, p.getLocation(), new Vector(radius,radius,radius)))
                return;
        }

        p.playSound(p.getLocation(), note.getInstrument(), note.getVelocity(), note.getNote());
    }
}