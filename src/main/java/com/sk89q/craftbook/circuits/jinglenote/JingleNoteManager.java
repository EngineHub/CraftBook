// $Id$
/*
 * Tetsuuuu plugin for SK's Minecraft Server Copyright (C) 2010 sk89q <http://www.sk89q.com> All rights reserved.
 */

package com.sk89q.craftbook.circuits.jinglenote;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

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

    public void play(Player player, JingleSequencer sequencer) {

        String name = player.getName();

        // Existing player found!
        if (instances.containsKey(name)) {
            JingleNotePlayer existing = instances.get(name);
            existing.stop();
            instances.remove(name);
        }

        JingleNotePlayer notePlayer = new JingleNotePlayer(name, sequencer);
        Thread thread = new Thread(notePlayer);
        thread.setDaemon(true);
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.setName("JingleNotePlayer for " + player.getName());
        thread.start();

        instances.put(name, notePlayer);
    }

    public boolean stop(Player player) {

        String name = player.getName();

        // Existing player found!
        if (instances.containsKey(name)) {
            JingleNotePlayer existing = instances.get(name);
            existing.stop();
            instances.remove(name);
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