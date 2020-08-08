/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

// $Id$
/*
 * Tetsuuuu plugin for SK's Minecraft Server Copyright (C) 2010 sk89q <http://www.sk89q.com> All rights reserved.
 */

package com.sk89q.craftbook.util.jinglenote;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.util.SearchArea;
import com.sk89q.craftbook.util.jinglenote.JingleSequencer.Note;

public abstract class JingleNotePlayer implements Runnable {

    protected final String player;
    private JingleSequencer sequencer;
    protected SearchArea area;

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
    }

    @Override
    public void run() {

        if(sequencer == null)
            return;
        try {
            try {
                sequencer.play(this);
            } catch (Throwable t) {
                t.printStackTrace();
            }

            while(isPlaying()){
                Thread.sleep(10L);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            CraftBookPlugin.logDebugMessage("Finished playing for: " + player, "midi.stop");
            stop();
        }
    }

    public boolean isPlaying() {

        return sequencer != null && (sequencer.isPlaying() || !sequencer.hasPlayedBefore());
    }

    public String getPlayer() {

        return player;
    }

    public void stop() {

        if (sequencer != null) {
            sequencer.stop(this);
            sequencer = null;
        }
    }

    public abstract void play(Note note);
}