package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.bukkit.CircuitsPlugin;
import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.ic.jinglenote.JingleNoteComponent;
import com.sk89q.ic.jinglenote.MidiJingleSequencer;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Level;

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

    @Override
    public void trigger(ChipState chip) {

        String[] split = ICUtil.COLON_PATTERN.split(getSign().getLine(3));
        try {
            if (sequencer != null && !sequencer.isSongPlaying() && split[1].equalsIgnoreCase("START"))
                return;
        } catch (Exception ignored) {
        }

        int radius = -1;
        try {
            radius = Integer.parseInt(split[0]);
        } catch (Exception ignored) {
        }

        try {
            if (chip.getInput(0)) {
                String midiName = getSign().getLine(2);

                File[] trialPaths = {
                        new File(CircuitsPlugin.getInst().getDataFolder(), "midi/" + midiName),
                        new File(CircuitsPlugin.getInst().getDataFolder(), "midi/" + midiName + ".mid"),
                        new File(CircuitsPlugin.getInst().getDataFolder(), "midi/" + midiName + ".midi"),
                        new File("midi", midiName),
                        new File("midi", midiName + ".mid"),
                        new File("midi", midiName + ".midi"),
                };

                File file = null;

                for (File f : trialPaths)
                    if (f.exists()) {
                        file = f;
                        break;
                    }

                if (file == null) {
                    getServer().getLogger().log(Level.SEVERE, "Midi file not found!");
                    return;
                }

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
        } catch (Exception e) {
            getServer().getLogger().log(Level.SEVERE, "[CraftBookCircuits]: Midi Failed To Play!");
            final Writer result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);
            e.printStackTrace(printWriter);
            getServer().getLogger().log(Level.SEVERE, "[CraftBookCircuits]: " + result.toString());
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