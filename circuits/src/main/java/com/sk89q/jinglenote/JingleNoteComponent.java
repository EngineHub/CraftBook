/*
 * CommandBook
 * Copyright (C) 2011 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.jinglenote;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class JingleNoteComponent implements Listener {

    private JingleNoteManager jingleNoteManager;

    public void enable() {
        // Jingle note manager
        jingleNoteManager = new JingleNoteManager();
    }

    public void disable() {
        jingleNoteManager.stopAll();
    }

    /**
     * Get the jingle note manager.
     *
     * @return
     */
    public JingleNoteManager getJingleNoteManager() {
        return jingleNoteManager;
    }

    /*@EventHandler
    public void onJoin(PlayerJoinEvent event) {
        MidiJingleSequencer sequencer;

        try {
            File file = new File(CircuitsPlugin.getInst().getDataFolder(), "intro.mid");
            if (file.exists()) {
                sequencer = new MidiJingleSequencer(file);
                getJingleNoteManager().play(event.getPlayer(), sequencer, 2000);
            }
        } catch (MidiUnavailableException e) {
        	CircuitsPlugin.getInst().getLogger().log(Level.WARNING, "Failed to access MIDI: "
                    + e.getMessage());
        } catch (InvalidMidiDataException e) {
        	CircuitsPlugin.getInst().getLogger().log(Level.WARNING, "Failed to read intro MIDI file: "
                    + e.getMessage());
        } catch (FileNotFoundException ignored) {
        } catch (IOException e) {
        	CircuitsPlugin.getInst().getLogger().log(Level.WARNING, "Failed to read intro MIDI file: "
                    + e.getMessage());
        }
    }*/

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        getJingleNoteManager().stop(event.getPlayer());
    }
}
