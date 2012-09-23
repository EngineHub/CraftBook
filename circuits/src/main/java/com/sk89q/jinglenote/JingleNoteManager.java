// $Id$
/*
 * Tetsuuuu plugin for SK's Minecraft Server
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 * All rights reserved.
 */

package com.sk89q.jinglenote;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * A manager of play instances.
 *
 * @author sk89q
 */
public class JingleNoteManager {

    public JingleNoteManager() {

    }

    /**
     * List of instances.
     */
    protected final Map<String, JingleNotePlayer> instances
    = new HashMap<String, JingleNotePlayer>();

    public void play(Player player, MidiJingleSequencer sequencer, int delay, Location loc) {

        String name = player.getName();

        // Existing player found!
        if (instances.containsKey(name)) {
            JingleNotePlayer existing = instances.get(name);

            existing.stop();

            instances.remove(name);
        }

        JingleNotePlayer notePlayer = new JingleNotePlayer(player, loc, sequencer, delay);
        Thread thread = new Thread(notePlayer);
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

        for (JingleNotePlayer notePlayer : instances.values()) notePlayer.stop();

        instances.clear();
    }
}
