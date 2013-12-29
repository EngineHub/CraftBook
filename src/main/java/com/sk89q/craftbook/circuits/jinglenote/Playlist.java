package com.sk89q.craftbook.circuits.jinglenote;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

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

    protected volatile Map<Player, SearchArea> players; // Super safe code here.. this is going to be accessed across threads.
    private volatile Map<Player, SearchArea> lastPlayers;

    int position;

    List<String> lines = new ArrayList<String>();

    BukkitTask task;

    volatile JingleNoteManager jNote = new JingleNoteManager();
    volatile MidiJingleSequencer midiSequencer;
    volatile StringJingleSequencer stringSequencer;

    boolean stopping = false;

    public Playlist(String name) {

        players = new WeakHashMap<Player, SearchArea>();
        lastPlayers = new WeakHashMap<Player, SearchArea>();
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

    public void startPlaylist() {

        stopping = false;
        position = 0;
        if (task != null)
            task.cancel();
        Runnable show = new PlaylistInterpreter();
        task = Bukkit.getScheduler().runTaskAsynchronously(CraftBookPlugin.inst(), show);
    }

    public void stopPlaylist() {

        lastPlayers.clear();
        jNote.stopAll();
        players.clear();
        position = 0;
        if (task != null)
            task.cancel();
        stopping = true;
    }

    public void setPlayers(Map<Player, SearchArea> players) {

        lastPlayers = new WeakHashMap<Player, SearchArea>(this.players);
        this.players.clear();
        this.players.putAll(players);
    }

    public void addPlayers(Map<Player, SearchArea> players) {

        lastPlayers = new WeakHashMap<Player, SearchArea>(this.players);
        this.players.putAll(players);
    }

    public void removePlayers(Map<Player, SearchArea> players) {

        lastPlayers = new WeakHashMap<Player, SearchArea>(this.players);
        for(Player player : players.keySet())
            this.players.remove(player);
    }

    private class PlaylistInterpreter implements Runnable {

        @Override
        public void run () {

            while (position < lines.size() && !stopping) {

                if(midiSequencer != null) {

                    while(midiSequencer != null && midiSequencer.isSongPlaying()) {

                        if(!areIdentical(players, lastPlayers)) {

                            for(Entry<Player, SearchArea> p : lastPlayers.entrySet()) {

                                if(players.containsKey(p.getKey()))
                                    continue;

                                jNote.stop(p.getKey().getName());
                            }

                            for(Entry<Player, SearchArea> p : players.entrySet()) {

                                if(lastPlayers.containsKey(p.getKey()))
                                    continue;

                                jNote.play(p.getKey().getName(), midiSequencer, p.getValue());
                            }

                            lastPlayers = new WeakHashMap<Player, SearchArea>(players);
                        }

                        try {
                            Thread.sleep(1000L);
                        } catch (InterruptedException e) {
                            BukkitUtil.printStacktrace(e);
                        }
                    }
                    midiSequencer = null;
                }
                if(stringSequencer != null) {

                    while(stringSequencer != null && stringSequencer.isSongPlaying()) {

                        if(!lastPlayers.equals(players)) {

                            for(Entry<Player, SearchArea> p : lastPlayers.entrySet()) {

                                if(players.containsKey(p.getKey()))
                                    continue;

                                jNote.stop(p.getKey().getName());
                            }

                            for(Entry<Player, SearchArea> p : players.entrySet()) {

                                if(lastPlayers.containsKey(p.getKey()))
                                    continue;

                                jNote.play(p.getKey().getName(), stringSequencer, p.getValue());
                            }

                            lastPlayers = new WeakHashMap<Player, SearchArea>(players);
                        }

                        try {
                            Thread.sleep(1000L);
                        } catch (InterruptedException e) {
                            BukkitUtil.printStacktrace(e);
                        }
                    }
                    stringSequencer = null;
                }

                if(position >= lines.size()) {
                    CraftBookPlugin.inst().getLogger().warning("Playlist: " + playlist + " ended unexpectedly! Is your playlist file correct?");
                    break; //He's dead, Jim
                }

                String line = lines.get(position);
                position++;
                if (line.trim().startsWith("#") || line.trim().isEmpty())
                    continue;

                if (line.startsWith("wait ")) {

                    if(!stopping) {
                        PlaylistInterpreter show = new PlaylistInterpreter();
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
                        midiSequencer = new MidiJingleSequencer(file, false);
                        if (!midiSequencer.getSequencer().isOpen()) {
                            midiSequencer.getSequencer().open();
                        }

                        for(Entry<Player, SearchArea> player : players.entrySet())
                            jNote.play(player.getKey().getName(), midiSequencer, player.getValue());

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

                    stringSequencer = new StringJingleSequencer(tune, 0);

                    for(Entry<Player, SearchArea> player : players.entrySet())
                        jNote.play(player.getKey().getName(), stringSequencer, player.getValue());

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

                    for(Player player : players.keySet())
                        player.sendMessage(message);
                } else if (line.startsWith("goto ")) {

                    position = Integer.parseInt(line.replace("goto ", ""));
                }
            }
        }
    }

    public boolean areIdentical(Map<?,?> h1, Map<?,?> h2) {
        if ( h1.size() != h2.size() ) {
            return false;
        }
        Map<?,?> clone = new WeakHashMap(h2);
        Iterator<?> it = h1.entrySet().iterator();
        while (it.hasNext() ){
            Object o = it.next();
            if (clone.containsKey(o)){
                clone.remove(o);
            } else {
                return false;
            }
        }
        return true;
    }
}