package com.sk89q.craftbook.circuits.gates.world.miscellaneous;

import java.io.File;
import java.util.Locale;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
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

    MidiJingleSequencer sequencer;
    JingleNoteManager jNote;

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

        try {
            sequencer.stop();
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
            if(getLine(3).toUpperCase().endsWith(":START")) getSign().setLine(3, getLine(3).replace(":START", ";START"));
            if(getLine(3).toUpperCase().endsWith(":LOOP")) getSign().setLine(3, getLine(3).replace(":LOOP", ";LOOP"));

            String[] split = RegexUtil.SEMICOLON_PATTERN.split(getSign().getLine(3));

            if (!getLine(3).isEmpty()) area = SearchArea.createArea(getBackBlock(), split[0]);
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

        jNote = new JingleNoteManager();
    }

    @Override
    public void trigger(ChipState chip) {

        if (file == null || !file.exists()) {
            getServer().getLogger().log(Level.SEVERE, "Midi file not found!");
            return;
        }

        try {
            if (sequencer != null && sequencer.isPlaying() && forceStart) return;
        } catch (Exception ignored) {
        }

        try {
            if (chip.getInput(0)) {
                if(sequencer == null)
                    sequencer = new MidiJingleSequencer(file, loop);
                if(sequencer.isPlaying() || !sequencer.hasPlayedBefore()) {
                    for (Player player : getServer().getOnlinePlayers()) {
                        if (area != null && !area.isWithinArea(player.getLocation())) {
                            if(jNote.isPlaying(player.getName()))
                                jNote.stop(player.getName());
                            continue;
                        } else if(!jNote.isPlaying(player.getName())) {
                            jNote.play(player.getName(), sequencer, area);
                            player.sendMessage(ChatColor.YELLOW + "Playing " + midiName + "...");
                        }
                    }
                }
            } else if (!chip.getInput(0) && !forceStart && sequencer != null) {
                sequencer.stop();
                jNote.stopAll();
                sequencer = null;
            }
        } catch (Throwable e) {
            getServer().getLogger().log(Level.SEVERE, "Midi Failed To Play!");
            BukkitUtil.printStacktrace(e);
        }

        chip.setOutput(0, sequencer != null && sequencer.isPlaying());
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