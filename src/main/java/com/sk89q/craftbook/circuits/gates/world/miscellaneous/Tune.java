package com.sk89q.craftbook.circuits.gates.world.miscellaneous;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractIC;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.jinglenote.JingleNoteManager;
import com.sk89q.craftbook.circuits.jinglenote.StringJingleSequencer;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.RegexUtil;

public class Tune extends AbstractIC {

    StringJingleSequencer sequencer;
    JingleNoteManager jNote = new JingleNoteManager();

    public Tune(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Tune Player";
    }

    @Override
    public String getSignTitle() {

        return "TUNE";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {

            if (sequencer != null || jNote != null) {
                for (Player player : getServer().getOnlinePlayers()) {
                    jNote.stop(player.getName());
                }
                jNote.stopAll();
            }
            sequencer = new StringJingleSequencer(tune, delay);
            for (Player player : getServer().getOnlinePlayers()) {
                if (player == null) {
                    continue;
                }
                if (radius > 0 && !LocationUtil.isWithinSphericalRadius(BukkitUtil.toSign(getSign()).getLocation(),
                        player.getLocation(), radius)) {
                    continue;
                }
                jNote.play(player.getName(), sequencer, getSign().getBlockVector(), radius);
            }
        } else if (!chip.getInput(0) && sequencer != null) {
            sequencer.stop();
            for (Player player : getServer().getOnlinePlayers()) {
                jNote.stop(player.getName());
            }
            jNote.stopAll();
        }
    }

    int radius;
    int delay;
    String tune;

    @Override
    public void load() {

        try {
            radius = Integer.parseInt(getSign().getLine(3));
        } catch (Exception ignored) {
            radius = -1;
        }

        if (getLine(2).contains(":")) {

            String[] split = RegexUtil.COLON_PATTERN.split(getLine(2), 2);
            try {
                delay = Integer.parseInt(split[0]);
            } catch (Exception e) {
                delay = 2;
            }
            tune = split[1];
        } else {

            tune = getSign().getLine(2);
            delay = 2;
        }
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

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public String getShortDescription() {

            return "Plays a tune.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"Delay:Tune", "Radius"};
        }

        @Override
        public IC create(ChangedSign sign) {

            return new Tune(getServer(), sign, this);
        }
    }
}