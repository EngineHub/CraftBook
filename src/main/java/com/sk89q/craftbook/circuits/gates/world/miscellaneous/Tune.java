package com.sk89q.craftbook.circuits.gates.world.miscellaneous;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.circuits.ic.AbstractIC;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.jinglenote.JingleNoteManager;
import com.sk89q.craftbook.circuits.jinglenote.StringJingleSequencer;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SearchArea;

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
                if (area != null && !area.isWithinArea(player.getLocation())) {
                    continue;
                }
                jNote.play(player.getName(), sequencer, area);
            }
        } else if (!chip.getInput(0) && sequencer != null) {
            sequencer.stop();
            for (Player player : getServer().getOnlinePlayers()) {
                jNote.stop(player.getName());
            }
            jNote.stopAll();
        }
    }

    SearchArea area;
    int delay;
    String tune;

    @Override
    public void load() {

        if (!getLine(3).isEmpty()) area = SearchArea.createArea(getBackBlock(), getLine(3));

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