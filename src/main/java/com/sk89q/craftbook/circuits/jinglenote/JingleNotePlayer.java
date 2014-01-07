// $Id$
/*
 * Tetsuuuu plugin for SK's Minecraft Server Copyright (C) 2010 sk89q <http://www.sk89q.com> All rights reserved.
 */

package com.sk89q.craftbook.circuits.jinglenote;

import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.jinglenote.JingleSequencer.Note;
import com.sk89q.craftbook.util.SearchArea;

public abstract class JingleNotePlayer implements Runnable {

    protected final String player;
    protected JingleSequencer sequencer;
    protected SearchArea area;

    protected boolean override = false;

    /**
     * Constructs a new JingleNotePlayer
     * 
     * @param player The player who is hearing this's name.
     * @param seq The JingleSequencer to play.
     * @param area The SearchArea for this player. (optional)
     */
    public JingleNotePlayer(String player, JingleSequencer seq, SearchArea area) {

        this.player = player;
        sequencer = seq;
        this.area = area;
        override = false;
    }

    @Override
    public void run() {

        if(sequencer == null)
            return;
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

    public boolean isPlaying() {
        return sequencer.isPlaying();
    }

    public String getPlayer() {

        return player;
    }

    public void stop() {

        override = true;
        //if (sequencer != null) {
        //sequencer.stop();
        //}
    }

    public abstract void play(Note note);
}