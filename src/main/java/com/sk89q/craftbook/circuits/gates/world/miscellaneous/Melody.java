package com.sk89q.craftbook.circuits.gates.world.miscellaneous;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CircuitCore;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.gates.world.sensors.PowerSensor;
import com.sk89q.craftbook.circuits.ic.AbstractIC;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.jinglenote.JingleNoteManager;
import com.sk89q.craftbook.circuits.jinglenote.MidiJingleSequencer;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.RegexUtil;

/**
 * @author Me4502
 */
public class Melody extends AbstractIC {

    JingleNoteManager jNote = new JingleNoteManager();

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
    public void unload() {

        boolean stop = false;
        try {
            Sign s = (Sign)BukkitUtil.toSign(getSign()).getBlock().getState();
            if(s.getLine(0).isEmpty());
            stop = false;
        }
        catch(Exception e){
            stop = true;
        }
        if(stop) {
            try {
                for (Player player : getServer().getOnlinePlayers()) {
                    jNote.stop(player);
                }
                jNote.stopAll();
            } catch (Exception e) {
            }
        }
    }

    int radius;
    File file;
    String midiName;
    boolean forceStart;

    @Override
    public void load() {

        try {
            String[] split = RegexUtil.COLON_PATTERN.split(getSign().getLine(3));

            try {
                radius = Integer.parseInt(split[0]);
            } catch (Exception ignored) {
                radius = -1;
            }

            forceStart = split[1].equalsIgnoreCase("START");
        } catch (Exception e) {
        }

        midiName = getSign().getLine(2);

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
    }

    @Override
    public void trigger(ChipState chip) {

        if (file == null || !file.exists()) {
            getServer().getLogger().log(Level.SEVERE, "Midi file not found!");
            return;
        }

        try {
            if (jNote != null && jNote.getSequencer() != null && ((MidiJingleSequencer) jNote.getSequencer()).isSongPlaying() && forceStart) return;
        } catch (Exception ignored) {
        }

        try {
            if (chip.getInput(0)) {

                if (jNote != null && jNote.getSequencer() != null || jNote != null) {
                    for (Player player : getServer().getOnlinePlayers()) {
                        jNote.stop(player);
                    }
                    jNote.stopAll();
                }
                MidiJingleSequencer sequencer = new MidiJingleSequencer(file);
                for (Player player : getServer().getOnlinePlayers()) {
                    if (player == null) {
                        continue;
                    }
                    if (radius > 0 && !LocationUtil.isWithinSphericalRadius(BukkitUtil.toSign(getSign()).getLocation(), player.getLocation(), radius)) {
                        continue;
                    }
                    jNote.play(player, sequencer);
                    player.sendMessage(ChatColor.YELLOW + "Playing " + midiName + "...");
                }
            } else if (!chip.getInput(0) && jNote.getSequencer() != null && ((MidiJingleSequencer) jNote.getSequencer()).isSongPlaying()) {
                for (Player player : getServer().getOnlinePlayers()) {
                    jNote.stop(player);
                }
                jNote.stopAll();
            }
        } catch (Throwable e) {
            getServer().getLogger().log(Level.SEVERE, "[CraftBookCircuits]: Midi Failed To Play!");
            BukkitUtil.printStacktrace(e);
        }
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            try {
                if (sign.getLine(0).equalsIgnoreCase("POWER SENSOR")) {
                    sign.setLine(1, "[MC1266]");
                    sign.update(false);
                    return new PowerSensor(getServer(), sign, this);
                }
            } catch (Exception ignored) {
            }
            return new Melody(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Plays a MIDI.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {"MIDI name", "Radius"};
            return lines;
        }
    }
}