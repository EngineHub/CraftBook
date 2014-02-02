package com.sk89q.craftbook.circuits.gates.world.miscellaneous;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.ICManager;
import com.sk89q.craftbook.circuits.ic.ICMechanic;
import com.sk89q.craftbook.circuits.ic.ICVerificationException;
import com.sk89q.craftbook.circuits.jinglenote.JingleNoteManager;
import com.sk89q.craftbook.circuits.jinglenote.MidiJingleSequencer;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SearchArea;

/**
 * @author Me4502
 */
public class Melody extends AbstractSelfTriggeredIC {

    public Melody(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Melody Player";
    }

    @Override
    public String getSignTitle() {

        return "MELODY";
    }

    @Override
    public boolean isAlwaysST() {
        return true;
    }

    @Override
    public void unload() {
        if(player.isPlaying())
            player.setPlaying(false);
    }

    SearchArea area;
    File file;
    String midiName;
    boolean forceStart, loop;

    MelodyPlayer player;

    @Override
    public void load() {

        try {
            if(getLine(3).toUpperCase().endsWith(":START")) getSign().setLine(3, getLine(3).replace(":START", ";START"));
            if(getLine(3).toUpperCase().endsWith(":LOOP")) getSign().setLine(3, getLine(3).replace(":LOOP", ";LOOP"));

            String[] split = RegexUtil.SEMICOLON_PATTERN.split(getSign().getLine(3));

            if (!getLine(3).isEmpty()) area = SearchArea.createArea(getLocation().getBlock(), split[0]);
            else area = SearchArea.createEmptyArea();

            for(int i = 1; i < split.length; i++) {
                if(split[i].toUpperCase(Locale.ENGLISH).contains("START")) forceStart = true;
                if(split[i].toUpperCase(Locale.ENGLISH).contains("LOOP")) loop = true;
            }
        } catch (Exception ignored) {
        }

        midiName = getSign().getLine(2);

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

        try {
            player = new MelodyPlayer(new MidiJingleSequencer(file, loop));
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void trigger(ChipState chip) {

        if (file == null || !file.exists()) {
            CraftBookPlugin.logDebugMessage("Midi file not found in melody IC: " + midiName, "midi");
            return;
        }

        if(player == null || player.getSequencer() == null || !player.isPlaying() && player.hasPlayedBefore()) {
            try {
                player = new MelodyPlayer(new MidiJingleSequencer(file, loop));
            } catch (MidiUnavailableException e) {
                e.printStackTrace();
            } catch (InvalidMidiDataException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            if (chip.getInput(0)) {
                if(player.isPlaying()) {
                    for (Player pp : getServer().getOnlinePlayers()) {
                        if (player.isPlaying(pp.getName()) && !area.isWithinArea(pp.getLocation())) {
                            player.stop(pp.getName());
                        } else if(!player.isPlaying(pp.getName()) && area.isWithinArea(pp.getLocation())) {
                            player.play(pp.getName());
                            pp.sendMessage(ChatColor.YELLOW + "Playing " + midiName + "...");
                        }
                    }
                } else if(!player.hasPlayedBefore())
                    Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), player);
            } else if (!chip.getInput(0) && !forceStart) {
                player.setPlaying(false);
            }
        } catch (Throwable e) {
            getServer().getLogger().log(Level.SEVERE, "Midi Failed To Play!");
            BukkitUtil.printStacktrace(e);
        }

        chip.setOutput(0, player.isPlaying());
    }

    private class MelodyPlayer implements Runnable {

        private JingleNoteManager jNote;
        private MidiJingleSequencer sequencer;
        private boolean isPlaying, hasPlayedBefore;

        private final Set<String> toStop, toPlay;

        public MelodyPlayer(MidiJingleSequencer sequencer) {
            this.sequencer = sequencer;
            jNote = new JingleNoteManager();
            toStop = new HashSet<String>();
            toPlay = new HashSet<String>();
            hasPlayedBefore = false;
            isPlaying = false;
            CraftBookPlugin.logDebugMessage("Constructing new player instance.", "ic-mc1270");
        }

        public boolean isPlaying(String player) {
            return isPlaying() && (toPlay.contains(player) || jNote.isPlaying(player));
        }

        public void stop(String player) {
            toStop.add(player);
            toPlay.remove(player);
            CraftBookPlugin.logDebugMessage("Removing " + player + " from melody IC.", "ic-mc1270");
        }

        public void play(String player) {
            toPlay.add(player);
            toStop.remove(player);
            CraftBookPlugin.logDebugMessage("Adding " + player + " to melody IC.", "ic-mc1270");
            if(!hasPlayedBefore)
                hasPlayedBefore = true;
        }

        public boolean isPlaying() {
            return isPlaying || !toPlay.isEmpty() || jNote.isPlaying();
        }

        public void setPlaying(boolean playing) {
            isPlaying = playing;
        }

        public boolean hasPlayedBefore() {
            return hasPlayedBefore && sequencer.hasPlayedBefore();
        }

        @Override
        public void run () {
            try {
                isPlaying = true;

                while(isPlaying) {
                    synchronized(this) {
                        for(String player : toStop)
                            jNote.stop(player);
                        toStop.clear();
                        for(String player : toPlay) {
                            jNote.play(player, sequencer, area);
                            if(!hasPlayedBefore)
                                hasPlayedBefore = true;
                        }
                        toPlay.clear();
                    }
                    try {
                        Thread.sleep(10L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(sequencer == null || (!sequencer.isPlaying() || toPlay.isEmpty() && !jNote.isPlaying()) && sequencer.hasPlayedBefore()) {
                        isPlaying = false;
                        break;
                    }
                }

            } catch(Throwable t) {
                t.printStackTrace();
            } finally {
                if(sequencer != null)
                    sequencer.stop();
                jNote.stopAll();
                sequencer = null;
                synchronized(this) {
                    toPlay.clear();
                    toStop.clear();
                }
                isPlaying = false;
            }
        }

        public JingleNoteManager getJNote() {
            return jNote;
        }

        public MidiJingleSequencer getSequencer() {
            return sequencer;
        }
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new Melody(getServer(), sign, this);
        }

        @Override
        public void checkPlayer(ChangedSign sign, LocalPlayer player) throws ICVerificationException {

            if (sign.getLine(3).trim().isEmpty())
                if (!ICMechanic.hasRestrictedPermissions(player, this, "mc1270"))
                    throw new ICVerificationException("You don't have permission to globally broadcast!");
        }

        @Override
        public String getShortDescription() {

            return "Plays a MIDI.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"MIDI name", "Radius;LOOP;START"};
        }
    }
}
