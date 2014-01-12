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

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.ICManager;
import com.sk89q.craftbook.util.SearchArea;

public class Playlist {

    String playlist;

    List<String> lines = new ArrayList<String>();

    BukkitTask task;

    protected PlaylistInterpreter show = new PlaylistInterpreter();

    private boolean playing = false;

    public Playlist(String name) {

        playlist = name;
        try {
            readPlaylist();
        } catch (IOException e) {
            BukkitUtil.printStacktrace(e);
        }
    }

    public PlaylistInterpreter getPlaylistInterpreter() {

        return show;
    }

    public void readPlaylist() throws IOException {

        lines.clear();
        File file = new File(new File(ICManager.INSTANCE.getMidiFolder(), "playlists"), playlist + ".txt");
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

        public PlaylistInterpreter() {
            position = 0;
            players = new HashMap<String, SearchArea>();
            lastPlayers = new HashMap<String, SearchArea>();

            CraftBookPlugin.logDebugMessage("Created new playlist interpreter!", "playlist");
        }

        protected volatile Map<String, SearchArea> players; // Super safe code here.. this is going to be accessed across threads.
        private volatile  Map<String, SearchArea> lastPlayers;

        public synchronized void setPlayers(Map<String, SearchArea> newPlayers) {

            lastPlayers = new HashMap<String, SearchArea>(getPlayers());
            getPlayers().clear();
            getPlayers().putAll(newPlayers);
            CraftBookPlugin.logDebugMessage("Reset player list!", "playlist");
        }

        public synchronized void addPlayers(Map<String, SearchArea> newPlayers) {

            lastPlayers = new HashMap<String, SearchArea>(getPlayers());
            getPlayers().putAll(newPlayers);
            CraftBookPlugin.logDebugMessage("Added player list!", "playlist");
        }

        public synchronized void removePlayers(Map<String, SearchArea> newPlayers) {

            lastPlayers = new HashMap<String, SearchArea>(getPlayers());
            for(String player : newPlayers.keySet())
                getPlayers().remove(player);
            CraftBookPlugin.logDebugMessage("Subtracted from player list!", "playlist");
        }

        public Map<String, SearchArea> getPlayers() {

            return players;
        }

        @Override
        public void run () {

            while (position < lines.size() && isPlaying()) {

                if(sequencer != null) {

                    while(sequencer != null && sequencer.isPlaying() && !sequencer.getPlayers().isEmpty()) {

                        for(Entry<String, SearchArea> p : lastPlayers.entrySet()) {

                            if(getPlayers().containsKey(p.getKey()) && jNote.isPlaying(p.getKey()))
                                continue;

                            jNote.stop(p.getKey());

                            CraftBookPlugin.logDebugMessage("Removed player from sequencer: " + p.getKey(), "playlist");
                        }

                        for(Entry<String, SearchArea> p : getPlayers().entrySet()) {

                            if(lastPlayers.containsKey(p.getKey()))
                                continue;

                            jNote.play(p.getKey(), sequencer, p.getValue());

                            CraftBookPlugin.logDebugMessage("Added player to sequencer: " + p.getKey(), "playlist");
                        }

                        lastPlayers = new HashMap<String, SearchArea>(getPlayers());

                        try {
                            Thread.sleep(10L);
                        } catch (InterruptedException e) {
                            BukkitUtil.printStacktrace(e);
                        }

                        if(isPlaying() == false) {
                            CraftBookPlugin.logDebugMessage("No longer playing! Stopping sequencer", "playlist");
                            sequencer.stop();
                            break;
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

                while(getPlayers().isEmpty()) {
                    if(isPlaying() == false) return;
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        BukkitUtil.printStacktrace(e);
                    }
                }

                if (line.startsWith("wait ")) {

                    if(isPlaying()) {
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
                            new File(ICManager.INSTANCE.getMidiFolder(), midiName),
                            new File(ICManager.INSTANCE.getMidiFolder(), midiName + ".mid"),
                            new File(ICManager.INSTANCE.getMidiFolder(), midiName + ".midi"),
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

                        CraftBookPlugin.logDebugMessage("Player list on midi create: " + getPlayers().toString(), "playlist");

                        for(Entry<String, SearchArea> player : getPlayers().entrySet()) {
                            jNote.play(player.getKey(), sequencer, player.getValue());
                            CraftBookPlugin.logDebugMessage("Added player to midi sequencer upon creation: " + player.getKey(), "playlist");
                        }

                        lastPlayers = new HashMap<String, SearchArea>(getPlayers());

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

                    String tune = line.replace("tune ", "");

                    sequencer = new StringJingleSequencer(tune, 0);

                    for(Entry<String, SearchArea> player : getPlayers().entrySet()) {
                        jNote.play(player.getKey(), sequencer, player.getValue());
                        CraftBookPlugin.logDebugMessage("Added player to string sequencer upon creation: " + player.getKey(), "playlist");
                    }

                    lastPlayers = new HashMap<String, SearchArea>(getPlayers());

                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        BukkitUtil.printStacktrace(e);
                    }
                } else if (line.startsWith("send ")) {

                    String message = line.replace("send ", "");

                    for(String player : getPlayers().keySet()) {
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