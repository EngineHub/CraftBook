package com.sk89q.craftbook.mechanics.ic.gates.world.miscellaneous;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.mechanics.ic.*;
import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SearchArea;
import com.sk89q.craftbook.util.jinglenote.JingleNoteManager;
import com.sk89q.craftbook.util.jinglenote.MidiJingleSequencer;

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
        if(player != null && player.isPlaying())
            player.setPlaying(false);
        player = null;
    }

    SearchArea area;
    File file;
    String midiName;
    boolean forceStart, loop, hasRun;

    MelodyPlayer player;

    boolean foundFile = false;

    @Override
    public void load() {

        if(getLine(3).contains(":START")) getSign().setLine(3, getLine(3).replace(":START", ";START"));
        if(getLine(3).contains(":LOOP")) getSign().setLine(3, getLine(3).replace(":LOOP", ";LOOP"));

        String[] split = RegexUtil.SEMICOLON_PATTERN.split(getSign().getLine(3));

        if (!getLine(3).isEmpty()) {
            if(SearchArea.isValidArea(getLocation().getBlock(), split[0]))
                area = SearchArea.createArea(getLocation().getBlock(), split[0]);
            else
                return;
        } else area = SearchArea.createEmptyArea();

        for(int i = 1; i < split.length; i++) {
            if(split[i].toUpperCase(Locale.ENGLISH).contains("START")) forceStart = true;
            else if(split[i].toUpperCase(Locale.ENGLISH).contains("LOOP")) loop = true;
        }

        midiName = getSign().getLine(2);

        File[] trialPaths = {
                new File(ICManager.inst().getMidiFolder(), midiName),
                new File(ICManager.inst().getMidiFolder(), midiName + ".mid"),
                new File(ICManager.inst().getMidiFolder(), midiName + ".midi"),
                new File("midi", midiName),
                new File("midi", midiName + ".mid"),
                new File("midi", midiName + ".midi"),
        };

        for (File f : trialPaths) {
            if (f.exists()) {
                file = f;
                break;
            }
        }

        if(file != null && file.exists())
            foundFile = true;
        else
            CraftBookPlugin.logDebugMessage("Midi file not found in melody IC: " + midiName, "midi");
    }

    @Override
    public void trigger(ChipState chip) {

        if (!foundFile || area == null)
            return;

        if(player == null || !player.isValid() && (loop || chip.isTriggered(0) && chip.getInput(0))) {
            try {
                player = new MelodyPlayer(new MidiJingleSequencer(file, loop));
                hasRun = false;
            } catch (MidiUnavailableException | IOException | InvalidMidiDataException e) {
                CraftBookPlugin.logger().warning("Found invalid MIDI file: " + file);
                e.printStackTrace();
                foundFile = false;
            }
        }

        if(player == null) return;

        if (chip.getInput(0)) {
            if(player.isPlaying() || !hasRun) {
                for (Player pp : getServer().getOnlinePlayers()) {
                    if (player.isPlaying(pp.getName()) && !area.isWithinArea(pp.getLocation())) {
                        player.stop(pp.getName());
                    } else if(!player.isPlaying(pp.getName()) && area.isWithinArea(pp.getLocation())) {
                        player.play(pp.getName());
                        if (((Factory) getFactory()).showPlayingMessage) {
                            pp.sendMessage(ChatColor.YELLOW + "Playing " + midiName + "...");
                        }
                        if(!hasRun) {
                            Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), player);
                            hasRun = true;
                        }
                    }
                }
            }
        } else if (!chip.getInput(0) && !forceStart) {
            player.setPlaying(false);
        }

        chip.setOutput(0, player.isPlaying());
    }

    private class MelodyPlayer implements Runnable {

        private JingleNoteManager jNote;
        private MidiJingleSequencer sequencer;
        private boolean isPlaying;

        private final Set<String> toStop, toPlay;

        public MelodyPlayer(MidiJingleSequencer sequencer) {
            this.sequencer = sequencer;
            jNote = new JingleNoteManager();
            toStop = new HashSet<>();
            toPlay = new HashSet<>();
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

            if(jNote.isPlaying(player) || toPlay.contains(player)) return;
            toPlay.add(player);
            toStop.remove(player);
            CraftBookPlugin.logDebugMessage("Adding " + player + " to melody IC.", "ic-mc1270");
        }

        public boolean isPlaying() {
            return isPlaying && /*(!toPlay.isEmpty() || jNote.isPlaying() ||*/ sequencer != null && (sequencer.isPlaying() || !sequencer.hasPlayedBefore())/*)*/;
        }

        public void setPlaying(boolean playing) {
            isPlaying = playing;
        }

        @Override
        public void run () {
            try {
                isPlaying = true;
                CraftBookPlugin.logDebugMessage("Starting run of player instance.", "ic-mc1270");

                while(isPlaying) {
                    for(String player : toStop)
                        jNote.stop(player);
                    toStop.clear();
                    for(String player : toPlay) {
                        jNote.play(player, sequencer, area);
                    }
                    toPlay.clear();

                    if(!isValid() || !isPlaying() && sequencer.hasPlayedBefore()) {
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
                toPlay.clear();
                toStop.clear();
                isPlaying = false;
            }
        }

        public boolean isValid() {
            return sequencer != null && (sequencer.isPlaying() || !sequencer.hasPlayedBefore());
        }
    }

    public static class Factory extends AbstractICFactory implements ConfigurableIC {

        private boolean showPlayingMessage;

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new Melody(getServer(), sign, this);
        }

        @Override
        public void checkPlayer(ChangedSign sign, CraftBookPlayer player) throws ICVerificationException {

            if (sign.getLine(3).trim().isEmpty())
                if (!ICMechanic.hasRestrictedPermissions(player, this, "mc1270"))
                    throw new ICVerificationException("You don't have permission to globally broadcast!");
        }

        @Override
        public String getShortDescription() {

            return "Plays the MIDI file entered on the sign.";
        }

        @Override
        public String[] getLongDescription() {

            return new String[]{
                    "The '''MC1270''' will play a MIDI file listed on the IC in a specified area, or globally. It can be set to loop, or for the input to only play the MIDI file and not turn it off.",
                    "",
                    "This IC is one of the MIDI playing ICs, and can only play a single MIDI file, if you would like to play a playlist see [[../MC1273|The Jukebox IC]]",
                    "",
                    "MIDI files are stored in the CraftBook MIDI folder, which is inside the 'plugins/CraftBook' folder. This folder is only generated when ICs are enabled, so if you are missing the folder that is why.",
                    "In the main plugin configuration, there is an option to enable percussion. Some percussion instruments will still be played when this is disabled, but the seperate percussion portion of the MIDI file will not be read."
            };
        }

        @Override
        public void addConfiguration(YAMLProcessor config, String path) {
            showPlayingMessage = config.getBoolean(path + "show-playing-message", true);
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"MIDI name", "SearchArea;LOOP;START"};
        }

        @Override
        public String[] getPinDescription(ChipState state) {

            return new String[]{
                    "Play/Stop the MIDI", //Inputs
                    "High if currently playing" //Outputs
            };
        }
    }
}
