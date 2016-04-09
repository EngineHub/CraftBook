package com.sk89q.craftbook.util.jinglenote;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.mechanics.ic.ICManager;
import com.sk89q.craftbook.util.SearchArea;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class Playlist {

    private String playlist;

    private List<String> lines = new ArrayList<String>();

    private BukkitTask task;

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

    private void readPlaylist() throws IOException {
        lines.clear();
        File file = new File(new File(ICManager.inst().getMidiFolder(), "playlists"), playlist + ".txt");
        if(!file.exists()) {
            CraftBookPlugin.logger().severe("Playlist File Not Found! " + file.getName());
            return;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String line;
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

        PlaylistInterpreter() {
            position = 0;
            players = new HashMap<String, SearchArea>();
            lastPlayers = new HashMap<String, SearchArea>();

            CraftBookPlugin.logDebugMessage("Created new playlist interpreter!", "playlist");
        }

        protected volatile Map<String, SearchArea> players; // Super safe code here.. this is going to be accessed across threads.
        private volatile  Map<String, SearchArea> lastPlayers;

        public synchronized void setPlayers(Map<String, SearchArea> newPlayers) {

            lastPlayers = new HashMap<String, SearchArea>(players);
            players.clear();
            players.putAll(newPlayers);
            CraftBookPlugin.logDebugMessage("Reset player list!", "playlist");
        }

        public synchronized void addPlayers(Map<String, SearchArea> newPlayers) {

            lastPlayers = new HashMap<String, SearchArea>(players);
            players.putAll(newPlayers);
            CraftBookPlugin.logDebugMessage("Added player list!", "playlist");
        }

        public synchronized void removePlayers(Map<String, SearchArea> newPlayers) {

            lastPlayers = new HashMap<String, SearchArea>(players);
            for(String player : newPlayers.keySet()) {
                players.remove(player);
            }
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

                            if(players.containsKey(p.getKey()) && jNote.isPlaying(p.getKey()))
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

                        try {
                            Thread.sleep(10L);
                        } catch (InterruptedException e) {
                            BukkitUtil.printStacktrace(e);
                        }

                        if(!isPlaying()) {
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

                while(players.isEmpty()) {
                    if(!isPlaying()) return;
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
                        task = Bukkit.getScheduler().runTaskLaterAsynchronously(CraftBookPlugin.inst(), show, Long.parseLong(StringUtils.replace(line, "wait ", "")));
                    }
                    return;
                } else if (line.startsWith("midi ")) {

                    File file = null;
                    String midiName = StringUtils.replace(line, "midi ", "");

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

                    String tune = StringUtils.replace(line, "tune ", "");

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

                    String message = StringUtils.replace(line, "send ", "");

                    for(String player : players.keySet()) {
                        Player pp = Bukkit.getPlayerExact(player);
                        if(pp != null)
                            pp.sendMessage(message);
                    }
                } else if (line.startsWith("goto ")) {

                    position = Integer.parseInt(StringUtils.replace(line, "goto ", ""));
                }
            }
        }
    }
}