package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.bukkit.CircuitsPlugin;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.jinglenote.JingleNoteComponent;
import com.sk89q.jinglenote.MidiJingleSequencer;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.block.Sign;
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

    public Melody(Server server, Sign block) {

        super(server, block);
        jNote = new JingleNoteComponent();
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
            if (jNote != null) jNote.getJingleNoteManager().stopAll();
        } catch (Exception ignored) {

        }
    }

    @Override
    public void trigger(ChipState chip) {

        try {
            if (jNote == null) jNote = new JingleNoteComponent();
            jNote.enable();

            if (chip.getInput(0) && (sequencer == null || getSign().getLine(3).equalsIgnoreCase("START"))) {
                String midiName = getSign().getLine(2);

                int radius = 0;
                try {
                    radius = Integer.parseInt(getSign().getLine(3));
                } catch (Exception ignored) {
                }

                File[] trialPaths = {
                        new File(CircuitsPlugin.getInst().getDataFolder(),
                                "midi/" + midiName),
                        new File(CircuitsPlugin.getInst().getDataFolder(),
                                "midi/" + midiName + ".mid"),
                        new File(CircuitsPlugin.getInst().getDataFolder(),
                                "midi/" + midiName + ".midi"),
                        new File("midi", midiName),
                        new File("midi", midiName + ".mid"),
                        new File("midi", midiName + ".midi"),};

                File file = null;

                for (File f : trialPaths) {
                    if (f.exists()) {
                        file = f;
                        break;
                    }
                }

                if (file == null) {
                    getServer().getLogger().log(Level.SEVERE,
                            "Midi file not found!");
                    return;
                }

                if (sequencer != null || jNote != null) {
                    for (Player player : getServer().getOnlinePlayers()) {
                        if (getSign().getLine(3).equalsIgnoreCase("START")) break;
                        jNote.getJingleNoteManager().stop(player);
                    }
                    if (!getSign().getLine(3).equalsIgnoreCase("START"))
                        jNote.getJingleNoteManager().stopAll();
                }
                sequencer = new MidiJingleSequencer(file);
                for (Player player : getServer().getOnlinePlayers()) {
                    if (player == null)
                        continue;
                    if (radius > 0 && player.getLocation().distance(getSign().getLocation()) > radius) continue;
                    jNote.getJingleNoteManager().play(player, sequencer, 0);
                    player.sendMessage(ChatColor.YELLOW + "Playing " + midiName
                            + "...");
                }
            } else if (sequencer != null && chip.getInput(0) && !getSign().getLine(3).equalsIgnoreCase("START")) {
                if (getSign().getLine(3).equalsIgnoreCase("START")) return;
                sequencer.stop();
                sequencer = null;
                for (Player player : getServer().getOnlinePlayers()) {
                    jNote.getJingleNoteManager().stop(player);
                }
                jNote.getJingleNoteManager().stopAll();
            }
        } catch (Exception e) {
            getServer().getLogger().log(Level.SEVERE,
                    "[CraftBookCircuits]: Midi Failed To Play!");
            final Writer result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);
            e.printStackTrace(printWriter);
            getServer().getLogger().log(Level.SEVERE,
                    "[CraftBookCircuits]: " + result.toString());
        }
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new Melody(getServer(), sign);
        }
    }

}
