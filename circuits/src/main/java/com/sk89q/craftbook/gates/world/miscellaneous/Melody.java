package com.sk89q.craftbook.gates.world.miscellaneous;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.bukkit.CircuitsPlugin;
import com.sk89q.craftbook.gates.world.sensors.PowerSensor;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.ICUtil;
import com.sk89q.craftbook.jinglenote.JingleNoteComponent;
import com.sk89q.craftbook.jinglenote.MidiJingleSequencer;
import com.sk89q.craftbook.util.GeneralUtil;
import com.sk89q.craftbook.util.LocationUtil;

/**
 * @author Me4502
 */
public class Melody extends AbstractIC {

    MidiJingleSequencer sequencer;
    JingleNoteComponent jNote = new JingleNoteComponent();

    public Melody(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
        jNote.enable();
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

        try {
            sequencer.stop();
            for (Player player : getServer().getOnlinePlayers()) {
                jNote.getJingleNoteManager().stop(player);
            }
            jNote.getJingleNoteManager().stopAll();
        } catch (Exception ignored) {
        }
    }

    int radius = -1;
    File file;
    String midiName;
    boolean forceStart = false;

    @Override
    public void load() {

        try {
            String[] split = ICUtil.COLON_PATTERN.split(getSign().getLine(3));

            try {
                radius = Integer.parseInt(split[0]);
            } catch (Exception ignored) {
            }

            forceStart = split[1].equalsIgnoreCase("START");
        }
        catch(Exception e){}

        midiName = getSign().getLine(2);

        File[] trialPaths = {
                new File(CircuitsPlugin.getInst().midiFolder, midiName),
                new File(CircuitsPlugin.getInst().midiFolder, midiName + ".mid"),
                new File(CircuitsPlugin.getInst().midiFolder, midiName + ".midi"),
                new File("midi", midiName),
                new File("midi", midiName + ".mid"),
                new File("midi", midiName + ".midi"),
        };

        for (File f : trialPaths)
            if (f.exists()) {
                file = f;
                break;
            }
    }

    @Override
    public void trigger(ChipState chip) {

        if (file == null || !file.exists()) {
            getServer().getLogger().log(Level.SEVERE, "Midi file not found!");
            return;
        }

        try {
            if (sequencer != null && !sequencer.isSongPlaying() && forceStart)
                return;
        } catch (Exception ignored) {
        }

        try {
            if (chip.getInput(0)) {

                if (sequencer != null || jNote != null) {
                    for (Player player : getServer().getOnlinePlayers()) {
                        jNote.getJingleNoteManager().stop(player);
                    }
                    jNote.getJingleNoteManager().stopAll();
                }
                sequencer = new MidiJingleSequencer(file);
                for (Player player : getServer().getOnlinePlayers()) {
                    if (player == null) {
                        continue;
                    }
                    if (radius > 0 && !LocationUtil.isWithinRadius(BukkitUtil.toSign(getSign()).getLocation(), player.getLocation(),
                            radius)) {
                        continue;
                    }
                    jNote.getJingleNoteManager().play(player, sequencer, 0);
                    player.sendMessage(ChatColor.YELLOW + "Playing " + midiName + "...");
                }
            } else if (!chip.getInput(0) && sequencer != null) {
                sequencer.stop();
                for (Player player : getServer().getOnlinePlayers()) {
                    jNote.getJingleNoteManager().stop(player);
                }
                jNote.getJingleNoteManager().stopAll();
            }
        } catch (Throwable e) {
            getServer().getLogger().log(Level.SEVERE, "[CraftBookCircuits]: Midi Failed To Play!");
            Bukkit.getLogger().severe(GeneralUtil.getStackTrace(e));
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
        public String getDescription() {

            return "Plays a MIDI.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "MIDI name",
                    "Radius"
            };
            return lines;
        }
    }
}