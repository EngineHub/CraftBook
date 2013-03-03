// $Id$
/*
 * Tetsuuuu plugin for SK's Minecraft Server Copyright (C) 2010 sk89q <http://www.sk89q.com> All rights reserved.
 */

package com.sk89q.craftbook.circuits.jinglenote;

import java.util.HashMap;
import java.util.Map;

import com.sk89q.craftbook.circuits.jinglenote.bukkit.BukkitJingleNotePlayer;
import com.sk89q.worldedit.WorldVector;

/**
 * A manager of play instances.
 *
 * @author sk89q
 */
public class JingleNoteManager {

    /**
     * List of instances.
     */
    protected final Map<String, JingleNotePlayer> instances = new HashMap<String, JingleNotePlayer>();

    public void play(String player, JingleSequencer sequencer, WorldVector centre, int radius) {

        // Existing player found!
        if (instances.containsKey(player)) {
            JingleNotePlayer existing = instances.get(player);
            existing.stop();
            instances.remove(player);
        }

        JingleNotePlayer notePlayer = new BukkitJingleNotePlayer(player, sequencer, centre, radius);
        Thread thread = new Thread(notePlayer);
        thread.setDaemon(true);
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.setName("JingleNotePlayer for " + player);
        thread.start();

        instances.put(player, notePlayer);
    }

    public boolean stop(String player) {

        // Existing player found!
        if (instances.containsKey(player)) {
            JingleNotePlayer existing = instances.get(player);
            existing.stop();
            instances.remove(player);
            return true;
        }
        return false;
    }

    public void stopAll() {

        for (JingleNotePlayer notePlayer : instances.values()) {
            notePlayer.stop();
        }

        instances.clear();
    }
}