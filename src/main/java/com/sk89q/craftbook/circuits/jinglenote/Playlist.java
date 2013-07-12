package com.sk89q.craftbook.circuits.jinglenote;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.sk89q.craftbook.bukkit.CircuitCore;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.Tuple2;
import com.sk89q.worldedit.WorldVector;

public class Playlist {

    String playlist;

    protected volatile HashSet<Tuple2<Player, Tuple2<WorldVector, Integer>>> players; // Super safe code here.. this is going to be accessed across threads.
    private volatile HashSet<Tuple2<Player, Tuple2<WorldVector, Integer>>> lastPlayers;

    int position;

    List<String> lines = new ArrayList<String>();

    BukkitTask task;

    volatile JingleNoteManager jNote = new JingleNoteManager();
    volatile MidiJingleSequencer midiSequencer;
    volatile StringJingleSequencer stringSequencer;

    public Playlist(String name) {

        players = new HashSet<Tuple2<Player, Tuple2<WorldVector, Integer>>>();
        lastPlayers = new HashSet<Tuple2<Player, Tuple2<WorldVector, Integer>>>();
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
    }

    @SuppressWarnings("unchecked")
    public void setPlayers(HashSet<Tuple2<Player, Tuple2<WorldVector, Integer>>> players) {

        lastPlayers = (HashSet<Tuple2<Player, Tuple2<WorldVector, Integer>>>) this.players.clone();
        this.players.clear();
        this.players.addAll(players);
    }

    @SuppressWarnings("unchecked")
    public void addPlayers(HashSet<Tuple2<Player, Tuple2<WorldVector, Integer>>> players) {

        lastPlayers = (HashSet<Tuple2<Player, Tuple2<WorldVector, Integer>>>) this.players.clone();
        this.players.addAll(players);
    }

    @SuppressWarnings("unchecked")
    public void removePlayers(HashSet<Tuple2<Player, Tuple2<WorldVector, Integer>>> players) {

        lastPlayers = (HashSet<Tuple2<Player, Tuple2<WorldVector, Integer>>>) this.players.clone();
        this.players.removeAll(players);
    }

    private class PlaylistInterpreter implements Runnable {

        @SuppressWarnings("unchecked")
        @Override
        public void run () {

            while (position < lines.size()) {

                if(midiSequencer != null) {

                    while(midiSequencer != null && midiSequencer.isSongPlaying()) {

                        if(!areIdentical(players, lastPlayers)) {

                            for(Tuple2<Player, Tuple2<WorldVector, Integer>> p : players) {

                                if(lastPlayers.contains(p))
                                    continue;

                                jNote.play(p.a.getName(), midiSequencer, p.b.a, p.b.b);
                            }

                            for(Tuple2<Player, Tuple2<WorldVector, Integer>> p : lastPlayers) {

                                if(players.contains(p))
                                    continue;

                                jNote.stop(p.a.getName());
                            }

                            lastPlayers = (HashSet<Tuple2<Player, Tuple2<WorldVector, Integer>>>) players.clone();
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

                            for(Tuple2<Player, Tuple2<WorldVector, Integer>> p : players) {

                                if(lastPlayers.contains(p))
                                    continue;

                                jNote.play(p.a.getName(), stringSequencer, p.b.a, p.b.b);
                            }

                            for(Tuple2<Player, Tuple2<WorldVector, Integer>> p : lastPlayers) {

                                if(players.contains(p))
                                    continue;

                                jNote.stop(p.a.getName());
                            }

                            lastPlayers = (HashSet<Tuple2<Player, Tuple2<WorldVector, Integer>>>) players.clone();
                        }

                        try {
                            Thread.sleep(1000L);
                        } catch (InterruptedException e) {
                            BukkitUtil.printStacktrace(e);
                        }
                    }
                    stringSequencer = null;
                }
                String line = lines.get(position);
                position++;
                if (line.trim().startsWith("#") || line.trim().isEmpty())
                    continue;

                if (line.startsWith("wait ")) {

                    PlaylistInterpreter show = new PlaylistInterpreter();
                    task = Bukkit.getScheduler().runTaskLaterAsynchronously(CraftBookPlugin.inst(), show, Long.parseLong(line.replace("wait ", "")));
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

                    try {
                        midiSequencer = new MidiJingleSequencer(file, false);
                        if (!midiSequencer.getSequencer().isOpen()) {
                            midiSequencer.getSequencer().open();
                        }

                        for(Tuple2<Player, Tuple2<WorldVector, Integer>> player : players)
                            jNote.play(player.a.getName(), midiSequencer, player.b.a, player.b.b);

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

                    for(Tuple2<Player, Tuple2<WorldVector, Integer>> player : players)
                        jNote.play(player.a.getName(), stringSequencer, player.b.a, player.b.b);

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

                    for(Tuple2<Player, Tuple2<WorldVector, Integer>> player : players)
                        player.a.sendMessage(message);
                } else if (line.startsWith("goto ")) {

                    position = Integer.parseInt(line.replace("goto ", ""));
                }
            }
        }
    }

    public boolean areIdentical(HashSet<?> h1, HashSet<?> h2) {
        if ( h1.size() != h2.size() ) {
            return false;
        }
        HashSet<?> clone = (HashSet<?>) h2.clone();
        Iterator<?> it = h1.iterator();
        while (it.hasNext() ){
            Object o = it.next();
            if (clone.contains(o)){
                clone.remove(o);
            } else {
                return false;
            }
        }
        return true;
    }
}