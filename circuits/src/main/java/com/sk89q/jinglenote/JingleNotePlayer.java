// $Id$
/*
 * Tetsuuuu plugin for SK's Minecraft Server
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 * All rights reserved.
*/

package com.sk89q.jinglenote;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.bukkit.CircuitsPlugin;

public class JingleNotePlayer implements Runnable {
    protected final Player player;
    protected final Location loc;
    protected MidiJingleSequencer sequencer;
    protected final int delay;
    
    protected boolean keepMusicBlock = false;
    
    public JingleNotePlayer(Player player,
            Location loc, MidiJingleSequencer seq,  int delay) {
        this.player = player;
        this.loc = loc;
        this.sequencer = seq;
        this.delay = delay;
    }
    
    public void run() {
        try {
            if (delay > 0) {
                Thread.sleep(delay);
            }
            
            // Create a fake note block
            player.sendBlockChange(loc, 25, (byte) 0);
            Thread.sleep(100);
            
            try {
                sequencer.run(this);
            } catch (Throwable t) {
                t.printStackTrace();
            }
            
            Thread.sleep(500);
            
            if (!keepMusicBlock) {
                // Restore music block
                CircuitsPlugin.server.getScheduler().scheduleSyncDelayedTask(CircuitsPlugin.getInst(), new Runnable() {
                    
                    public void run() {
                        int prevId = player.getWorld().getBlockTypeIdAt(loc);
                        byte prevData = player.getWorld().getBlockAt(loc).getData();
                        player.sendBlockChange(loc, prevId, prevData);
                    }
                });
            }
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
        this.keepMusicBlock = keepMusicBlock;
        
        if (sequencer != null) {
            sequencer.stop();
        }
        
        CircuitsPlugin.server.getScheduler().scheduleSyncDelayedTask(CircuitsPlugin.getInst(), new Runnable() {
            
            public void run() {
                int prevId = player.getWorld().getBlockTypeIdAt(loc);
                byte prevData = player.getWorld().getBlockAt(loc).getData();
                player.sendBlockChange(loc, prevId, prevData);
            }
        });
    }
    
    public void play(byte instrument, byte note) {
        if (!player.isOnline()) {
            return;
        }
        
        player.playNote(loc, instrument, note);
    }
}
