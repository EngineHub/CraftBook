package com.sk89q.craftbook.circuits.jinglenote;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.sk89q.craftbook.bukkit.CircuitCore;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.GeneralUtil;

public class Playlist {

    String playlist;

    protected List<Player> players;

    int position;

    List<String> lines = new ArrayList<String>();

    BukkitTask task;

    JingleNoteManager jNote = new JingleNoteManager();
    MidiJingleSequencer midiSequencer;
    StringJingleSequencer stringSequencer;

    public Playlist(String name) {

        playlist = name;
        try {
            readPlaylist();
        } catch (IOException e) {
            Bukkit.getLogger().severe(GeneralUtil.getStackTrace(e));
        }
    }

    public void readPlaylist() throws IOException {

        lines.clear();
        File file = new File(new File(CircuitCore.inst().getMidiFolder(), "playlists"), playlist + ".txt");
        if(!file.exists()) {
            Bukkit.getLogger().severe("Playlist File Not Found! " + file.getName());
            return;
        }
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = "";
        while ((line = br.readLine()) != null) {

            if(line.trim().isEmpty())
                continue;
            lines.add(line);
        }

        br.close();
    }

    public void startPlaylist(List<Player> players) {

        this.players = players;
        position = 0;
        if (task != null)
            task.cancel();
        Runnable show = new PlaylistInterpreter();
        task = Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), show);
    }

    private class PlaylistInterpreter implements Runnable {


        public PlaylistInterpreter() {

        }

        @Override
        public void run () {

            while (position < lines.size()) {

                String line = lines.get(position);
                position++;
                if (line.trim().startsWith("#") || line.trim().isEmpty())
                    continue;

                if (line.startsWith("wait ")) {

                    PlaylistInterpreter show = new PlaylistInterpreter();
                    task = Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), show, Long.parseLong(line.replace("wait ", "")));
                    return;
                } else if (line.startsWith("midi ")) {

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
                        midiSequencer = new MidiJingleSequencer(file);
                    } catch (MidiUnavailableException e) {
                        e.printStackTrace();
                    } catch (InvalidMidiDataException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    for(Player player : players)
                        jNote.play(player, midiSequencer);
                } else if (line.startsWith("tune ")) {

                    String tune = line.replace("tune ", "");

                    stringSequencer = new StringJingleSequencer(tune, 0);

                    for(Player player : players)
                        jNote.play(player, stringSequencer);
                } else if (line.startsWith("send ")) {

                    String message = line.replace("send ", "");

                    for(Player player : players)
                        player.sendMessage(message);
                }
            }
        }
    }
}
