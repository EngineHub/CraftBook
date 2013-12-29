package com.sk89q.craftbook.circuits.gates.world.miscellaneous;

import java.io.File;
import java.util.Locale;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CircuitCore;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractIC;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.ICMechanic;
import com.sk89q.craftbook.circuits.ic.ICVerificationException;
import com.sk89q.craftbook.circuits.jinglenote.JingleNoteManager;
import com.sk89q.craftbook.circuits.jinglenote.MidiJingleSequencer;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SearchArea;

/**
 * @author Me4502
 */
public class Melody extends AbstractIC {

    MidiJingleSequencer sequencer;
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

        try {
            sequencer.stop();
            for (Player player : getServer().getOnlinePlayers()) {
                jNote.stop(player.getName());
            }
            jNote.stopAll();
        } catch (Exception ignored) {
        }
    }

    SearchArea area;
    File file;
    String midiName;
    boolean forceStart, loop;

    @Override
    public void load() {

        try {
            String[] split = RegexUtil.COLON_PATTERN.split(getSign().getLine(3),2);

            if (!getLine(3).isEmpty()) area = SearchArea.createArea(getBackBlock(), getLine(3));

            if(split.length > 1) {
                forceStart = split[1].toUpperCase(Locale.ENGLISH).contains("START");
                loop = split[1].toUpperCase(Locale.ENGLISH).contains("LOOP");
            }
        } catch (Exception ignored) {
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
            if (sequencer != null && sequencer.isSongPlaying() && forceStart) return;
        } catch (Exception ignored) {
        }

        try {
            if (chip.getInput(0)) {

                if (sequencer != null || jNote != null) {
                    for (Player player : getServer().getOnlinePlayers()) {
                        jNote.stop(player.getName());
                    }
                    jNote.stopAll();
                }
                sequencer = new MidiJingleSequencer(file, loop);
                for (Player player : getServer().getOnlinePlayers()) {
                    if (player == null || !player.isOnline()) {
                        continue;
                    }
                    if (area != null && !area.isWithinArea(player.getLocation()))
                        continue;
                    jNote.play(player.getName(), sequencer, area);
                    player.sendMessage(ChatColor.YELLOW + "Playing " + midiName + "...");
                }
            } else if (!chip.getInput(0) && sequencer != null) {
                sequencer.stop();
                for (Player player : getServer().getOnlinePlayers()) {
                    jNote.stop(player.getName());
                }
                jNote.stopAll();
            }
        } catch (Throwable e) {
            getServer().getLogger().log(Level.SEVERE, "Midi Failed To Play!");
            BukkitUtil.printStacktrace(e);
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

            return new String[] {"MIDI name", "Radius"};
        }
    }
}