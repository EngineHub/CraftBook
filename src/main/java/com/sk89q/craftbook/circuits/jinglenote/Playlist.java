package com.sk89q.craftbook.circuits.jinglenote;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.sk89q.craftbook.bukkit.CircuitCore;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.SearchArea;

public class Playlist {

    String playlist;

    protected volatile Map<String, SearchArea> players; // Super safe code here.. this is going to be accessed across threads.
    private volatile  Map<String, SearchArea> lastPlayers;

    List<String> lines = new ArrayList<String>();

    BukkitTask task;

    protected PlaylistInterpreter show = new PlaylistInterpreter();

    private boolean playing = false;

    public Playlist(String name) {

        players = new HashMap<String, SearchArea>();
        lastPlayers = new HashMap<String, SearchArea>();
        playlist = name;
        try {
            readPlaylist();
        } catch (IOException e) {
            BukkitUtil.printStacktrace(e);
        }
    }

    public void readPlaylist() throws IOException {

        lines.clear();
        File file = new File(new File(CircuitCore.inst().getMidiFolder(), "playlists"), playlist + ".txt");
        if(!file.exists()) {
            CraftBookPlugin.logger().severe("Playlist File Not Found! " + file.getName());
            return;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String line = "";
        while ((line = br.readLine()) != null) {

            if(line.trim().isEmpty())
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

        lastPlayers.clear();
        show.jNote.stopAll();
        if(show.sequencer != null)
            show.sequencer.stop();
        players.clear();
        if (task != null)
            task.cancel();
        show.position = 0;
        playing = false;
    }

    public void setPlayers(Map<String, SearchArea> newPlayers) {

        lastPlayers = new HashMap<String, SearchArea>(players);
        players.clear();
        players.putAll(newPlayers);
        CraftBookPlugin.logDebugMessage("Reset player list!", "playlist");
    }

    public void addPlayers(Map<String, SearchArea> newPlayers) {

        lastPlayers = new HashMap<String, SearchArea>(players);
        players.putAll(newPlayers);
        CraftBookPlugin.logDebugMessage("Added player list!", "playlist");
    }

    public void removePlayers(Map<String, SearchArea> newPlayers) {

        lastPlayers = new HashMap<String, SearchArea>(players);
        for(String player : newPlayers.keySet())
            players.remove(player);
        CraftBookPlugin.logDebugMessage("Subtracted from player list!", "playlist");
    }

    private class PlaylistInterpreter implements Runnable {

        protected volatile int position;

        volatile JingleNoteManager jNote = new JingleNoteManager();
        volatile JingleSequencer sequencer;

        public PlaylistInterpreter() {
            position = 0;
        }

        @Override
        public void run () {

            while (position < lines.size() && isPlaying()) {

                if(sequencer != null) {

                    while(sequencer != null && sequencer.isPlaying()) {

                        if(!players.equals(lastPlayers)) {

                            CraftBookPlugin.logDebugMessage("Old Players: " + players.toString(), "playlist");
                            CraftBookPlugin.logDebugMessage("Old LastPlayers: " + lastPlayers.toString(), "playlist");

                            for(Entry<String, SearchArea> p : lastPlayers.entrySet()) {

                                if(players.containsKey(p.getKey()))
                                    continue;

                                jNote.stop(p.getKey());

                                CraftBookPlugin.logDebugMessage("Removed player from sequencer: " + p.getKey(), "playlist");
                            }

                            for(Entry<String, SearchArea> p : players.entrySet()) {

                                if(lastPlayers.containsKey(p.getKey()))
                                    continue;

                                jNote.play(p.getKey(), sequencer, p.getValue());

                                CraftBookPlugin.logDebugMessage("Added player to sequencer: " + p.getKey(), "playlist");
                            }

                            lastPlayers = new HashMap<String, SearchArea>(players);
                        }

                        try {
                            Thread.sleep(10L);
                        } catch (InterruptedException e) {
                            BukkitUtil.printStacktrace(e);
                        }
                    }
                    sequencer = null;
                    CraftBookPlugin.logDebugMessage("Erasing sequencer", "playlist");
                }

                if(sequencer != null) continue; //Don't continue until they've closed.

                if(position >= lines.size()) {
                    CraftBookPlugin.inst().getLogger().warning("Playlist: " + playlist + " ended unexpectedly! Is your playlist file correct?");
                    break; //He's dead, Jim
                }

                String line = lines.get(position);
                position++;
                if (line.trim().startsWith("#") || line.trim().isEmpty())
                    continue;

                if (line.startsWith("wait ")) {

                    if(isPlaying()) {
                        task.cancel();
                        PlaylistInterpreter show = new PlaylistInterpreter();
                        show.position = position;
                        task = Bukkit.getScheduler().runTaskLaterAsynchronously(CraftBookPlugin.inst(), show, Long.parseLong(line.replace("wait ", "")));
                    }
                    return;
                } else if (line.startsWith("midi ")) {

                    if(players.isEmpty()) {
                        try {
                            Thread.sleep(1000L);
                        } catch (InterruptedException e) {
                            BukkitUtil.printStacktrace(e);
                        }
                        continue;
                    }
                    File file = null;
                    String midiName = line.replace("midi ", "");

                    File[] trialPaths = {
                            new File(CircuitCore.inst().getMidiFolder(), midiName),
                            new File(CircuitCore.inst().getMidiFolder(), midiName + ".mid"),
                            new File(CircuitCore.inst().getMidiFolder(), midiName + ".midi"),
                            new File("midi", midiName), new File("midi", midiName + ".mid"),
                            new File("midi", midiName + ".midi"),
                    };

                    for (File f : trialPaths) {
                        if (f.exists()) {
                            file = f;
                            break;
                        }
                    }

                    if(file == null) {
                        CraftBookPlugin.inst().getLogger().warning("Failed to find midi file: " + midiName + " for playlist file: " + playlist + ". Skipping midi!");
                        continue;
                    }

                    try {
                        sequencer = new MidiJingleSequencer(file, false);
                        if (!((MidiJingleSequencer) sequencer).getSequencer().isOpen()) {
                            ((MidiJingleSequencer) sequencer).getSequencer().open();
                        }

                        CraftBookPlugin.logDebugMessage("Player list on midi create: " + players.toString(), "playlist");

                        for(Entry<String, SearchArea> player : players.entrySet()) {
                            jNote.play(player.getKey(), sequencer, player.getValue());
                            CraftBookPlugin.logDebugMessage("Added player to midi sequencer upon creation: " + player.getKey(), "playlist");
                        }

                        lastPlayers = new HashMap<String, SearchArea>(players);

                        try {
                            Thread.sleep(1000L);
                        } catch (InterruptedException e) {
                            BukkitUtil.printStacktrace(e);
                        }
                    } catch (MidiUnavailableException e) {
                        BukkitUtil.printStacktrace(e);
                    } catch (InvalidMidiDataException e) {
                        BukkitUtil.printStacktrace(e);
                    } catch (IOException e) {
                        BukkitUtil.printStacktrace(e);
                    }
                } else if (line.startsWith("tune ")) {

                    if(players.isEmpty()) {
                        try {
                            Thread.sleep(1000L);
                        } catch (InterruptedException e) {
                            BukkitUtil.printStacktrace(e);
                        }
                        continue;
                    }
                    String tune = line.replace("tune ", "");

                    sequencer = new StringJingleSequencer(tune, 0);

                    for(Entry<String, SearchArea> player : players.entrySet()) {
                        jNote.play(player.getKey(), sequencer, player.getValue());
                        CraftBookPlugin.logDebugMessage("Added player to string sequencer upon creation: " + player.getKey(), "playlist");
                    }

                    lastPlayers = new HashMap<String, SearchArea>(players);

                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        BukkitUtil.printStacktrace(e);
                    }
                } else if (line.startsWith("send ")) {

                    if(players.isEmpty()) {
                        try {
                            Thread.sleep(1000L);
                        } catch (InterruptedException e) {
                            BukkitUtil.printStacktrace(e);
                        }
                        continue;
                    }
                    String message = line.replace("send ", "");

                    for(String player : players.keySet()) {
                        Player pp = Bukkit.getPlayerExact(player);
                        if(pp != null)
                            pp.sendMessage(message);
                    }
                } else if (line.startsWith("goto ")) {

                    position = Integer.parseInt(line.replace("goto ", ""));
                }
            }
        }
    }

    public boolean areIdentical(Map<String, SearchArea> h1, Map<String, SearchArea> h2) {
        if ( h1.size() != h2.size() )
            return false;
        Map<String, SearchArea> clone = new HashMap<String, SearchArea>(h2);
        Iterator<String> it = h1.keySet().iterator();
        while (it.hasNext() ){
            String o = it.next();
            if (clone.containsKey(o)){
                clone.remove(o);
            } else {
                return false;
            }
        }
        return clone.isEmpty();
    }
}