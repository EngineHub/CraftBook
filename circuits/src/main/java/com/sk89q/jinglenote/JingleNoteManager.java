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
import org.bukkit.World;
import org.bukkit.entity.Player;
import com.sk89q.worldedit.blocks.BlockType;

/**
 * A manager of play instances.
 * 
 * @author sk89q
 */
public class JingleNoteManager {
    /**
     * List of instances.
     */
    protected final Map<String, JingleNotePlayer> instances
            = new HashMap<String, JingleNotePlayer>();
    
    public void play(Player player, MidiJingleSequencer sequencer, int delay) {
        String name = player.getName();
        Location loc = findLocation(player);
        
        // Existing player found!
        if (instances.containsKey(name)) {
            JingleNotePlayer existing = instances.get(name);
            Location existingLoc = existing.getLocation();
            
            existing.stop(
                    existingLoc.getBlockX() == loc.getBlockX()
                    && existingLoc.getBlockY() == loc.getBlockY()
                    && existingLoc.getBlockZ() == loc.getBlockZ());
            
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
            existing.stop(false);
            instances.remove(name);
            return true;
        }
        return false;
    }
    
    public void stopAll() {
        for (JingleNotePlayer notePlayer : instances.values()) {
            notePlayer.stop(false);
        }
        
        instances.clear();
    }
    
    private Location findLocation(Player player) {
        World world = player.getWorld();
        Location loc = player.getLocation();
        loc.setY(loc.getY() - 2);
        
        if (!BlockType.canPassThrough(world.getBlockTypeIdAt(loc))) {
            return loc;
        }
        
        loc.setY(loc.getY() + 4);
        
        return loc;
    }
}
