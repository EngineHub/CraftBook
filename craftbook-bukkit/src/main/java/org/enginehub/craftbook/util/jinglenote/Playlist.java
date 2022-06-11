/*
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

package org.enginehub.craftbook.util.jinglenote;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanics.ic.ICManager;
import org.enginehub.craftbook.util.SearchArea;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

public class Playlist {

    private String playlist;

    private List<String> lines = new ArrayList<>();

    private BukkitTask task;

    protected PlaylistInterpreter show = new PlaylistInterpreter();

    private boolean playing = false;

    public Playlist(String name) {

        playlist = name;
        try {
            readPlaylist();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PlaylistInterpreter getPlaylistInterpreter() {
        return show;
    }

    private void readPlaylist() throws IOException {
        lines.clear();
        File file = new File(new File(ICManager.inst().getMidiFolder(), "playlists"), playlist + ".txt");
        if (!file.exists()) {
            CraftBook.LOGGER.error("Playlist File Not Found! " + file.getName());
            return;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String line;
        while ((line = br.readLine()) != null) {

            if (line.trim().isEmpty())
                continue;
            lines.add(line);
        }

        br.close();
    }

    public boolean isPlaying() {

        return playing;
    }

    public void startPlaylist() {

        playing = true;
        if (task != null)
            task.cancel();
        show = new PlaylistInterpreter();
        task = Bukkit.getScheduler().runTaskAsynchronously(CraftBookPlugin.inst(), show);
    }

    public void stopPlaylist() {

        show.lastPlayers.clear();
        show.jNote.stopAll();
        show.sequencer = null;
        show.players.clear();
        if (task != null)
            task.cancel();
        show.position = 0;
        playing = false;
    }

    public class PlaylistInterpreter implements Runnable {

        protected volatile int position;

        volatile JingleNoteManager jNote = new JingleNoteManager();
        volatile JingleSequencer sequencer;

        PlaylistInterpreter() {
            position = 0;
            players = new HashMap<>();
            lastPlayers = new HashMap<>();

            CraftBookPlugin.logDebugMessage("Created new playlist interpreter!", "playlist");
        }

        protected volatile Map<String, SearchArea> players; // Super safe code here.. this is going to be accessed across threads.
        private volatile Map<String, SearchArea> lastPlayers;

        public synchronized void setPlayers(Map<String, SearchArea> newPlayers) {

            lastPlayers = new HashMap<>(players);
            players.clear();
            players.putAll(newPlayers);
            CraftBookPlugin.logDebugMessage("Reset player list!", "playlist");
        }

        public synchronized void addPlayers(Map<String, SearchArea> newPlayers) {

            lastPlayers = new HashMap<>(players);
            players.putAll(newPlayers);
            CraftBookPlugin.logDebugMessage("Added player list!", "playlist");
        }

        public synchronized void removePlayers(Map<String, SearchArea> newPlayers) {

            lastPlayers = new HashMap<>(players);
            for (String player : newPlayers.keySet()) {
                players.remove(player);
            }
            CraftBookPlugin.logDebugMessage("Subtracted from player list!", "playlist");
        }

        public Map<String, SearchArea> getPlayers() {

            return players;
        }

        @Override
        public void run() {

            while (position < lines.size() && isPlaying()) {
                if (sequencer != null) {
                    while (sequencer != null && sequencer.isPlaying() && sequencer.getPlayerCount() > 0) {
                        for (Entry<String, SearchArea> p : lastPlayers.entrySet()) {

                            if (players.containsKey(p.getKey()) && jNote.isPlaying(p.getKey()))
                                continue;

                            jNote.stop(p.getKey());

                            CraftBookPlugin.logDebugMessage("Removed player from sequencer: " + p.getKey(), "playlist");
                        }

                        for (Entry<String, SearchArea> p : players.entrySet()) {

                            if (lastPlayers.containsKey(p.getKey()))
                                continue;

                            jNote.play(p.getKey(), sequencer, p.getValue());

                            CraftBookPlugin.logDebugMessage("Added player to sequencer: " + p.getKey(), "playlist");
                        }

                        lastPlayers = new HashMap<>(players);

                        try {
                            Thread.sleep(10L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (!isPlaying()) {
                            CraftBookPlugin.logDebugMessage("No longer playing! Stopping sequencer", "playlist");
                            sequencer.stop();
                            break;
                        }
                    }
                    sequencer = null;
                    CraftBookPlugin.logDebugMessage("Erasing sequencer", "playlist");
                }

                if (sequencer != null) continue; //Don't continue until they've closed.

                if (position >= lines.size()) {
                    CraftBook.LOGGER.warn("Playlist: " + playlist + " ended unexpectedly! Is your playlist file correct?");
                    break; //He's dead, Jim
                }

                String line = lines.get(position);
                position++;
                if (line.trim().startsWith("#") || line.trim().isEmpty())
                    continue;

                while (players.isEmpty()) {
                    if (!isPlaying()) return;
                    CraftBookPlugin.logDebugMessage("Playlist has no players", "playlist");
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (line.startsWith("wait ")) {

                    if (isPlaying()) {
                        task.cancel();
                        PlaylistInterpreter show = new PlaylistInterpreter();
                        show.position = position;
                        show.players = players;
                        show.lastPlayers = lastPlayers;
                        show.sequencer = sequencer;
                        show.jNote = jNote;
                        task = Bukkit.getScheduler().runTaskLaterAsynchronously(CraftBookPlugin.inst(), show, Long.parseLong(line.replace("wait ", "")));
                    }
                    return;
                } else if (line.startsWith("midi ")) {

                    File file = null;
                    String midiName = line.replace("midi ", "");

                    File[] trialPaths = {
                        new File(ICManager.inst().getMidiFolder(), midiName),
                        new File(ICManager.inst().getMidiFolder(), midiName + ".mid"),
                        new File(ICManager.inst().getMidiFolder(), midiName + ".midi"),
                        new File("midi", midiName), new File("midi", midiName + ".mid"),
                        new File("midi", midiName + ".midi"),
                    };

                    for (File f : trialPaths) {
                        if (f.exists()) {
                            file = f;
                            break;
                        }
                    }

                    if (file == null) {
                        CraftBook.LOGGER.warn("Failed to find midi file: " + midiName + " for playlist file: " + playlist + ". Skipping midi!");
                        continue;
                    }

                    try {
                        sequencer = new MidiJingleSequencer(file, false);
                        if (!((MidiJingleSequencer) sequencer).getSequencer().isOpen()) {
                            ((MidiJingleSequencer) sequencer).getSequencer().open();
                        }

                        CraftBookPlugin.logDebugMessage("Player list on midi create: " + players.toString(), "playlist");

                        for (Entry<String, SearchArea> player : players.entrySet()) {
                            jNote.play(player.getKey(), sequencer, player.getValue());
                            CraftBookPlugin.logDebugMessage("Added player to midi sequencer upon creation: " + player.getKey(), "playlist");
                        }

                        lastPlayers = new HashMap<>(players);

                        try {
                            Thread.sleep(1000L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } catch (MidiUnavailableException | IOException | InvalidMidiDataException e) {
                        e.printStackTrace();
                    }
                } else if (line.startsWith("tune ")) {

                    String tune = line.replace("tune ", "");

                    sequencer = new StringJingleSequencer(tune, 0);

                    for (Entry<String, SearchArea> player : players.entrySet()) {
                        jNote.play(player.getKey(), sequencer, player.getValue());
                        CraftBookPlugin.logDebugMessage("Added player to string sequencer upon creation: " + player.getKey(), "playlist");
                    }

                    lastPlayers = new HashMap<>(players);

                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (line.startsWith("send ")) {

                    String message = line.replace("send ", "");

                    for (String player : players.keySet()) {
                        Player pp = Bukkit.getPlayerExact(player);
                        if (pp != null)
                            pp.sendMessage(message);
                    }
                } else if (line.startsWith("goto ")) {

                    position = Integer.parseInt(line.replace("goto ", ""));
                    CraftBookPlugin.logDebugMessage("Setting line to: " + position, "playlist");
                }
            }
        }
    }
}