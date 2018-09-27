package com.sk89q.craftbook.mechanics.ic.gates.world.miscellaneous;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SearchArea;
import com.sk89q.craftbook.util.jinglenote.JingleNoteManager;
import com.sk89q.craftbook.util.jinglenote.StringJingleSequencer;

public class Tune extends AbstractSelfTriggeredIC {

    StringJingleSequencer sequencer;
    JingleNoteManager jNote;

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
    public boolean isAlwaysST() {
        return true;
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            if(sequencer == null)
                sequencer = new StringJingleSequencer(tune, delay);
            if(sequencer.isPlaying() || !sequencer.hasPlayedBefore()) {
                for (Player player : getServer().getOnlinePlayers()) {
                    if (!area.isWithinArea(player.getLocation())) {
                        if(jNote.isPlaying(player.getName()))
                            jNote.stop(player.getName());
                    } else if (!jNote.isPlaying(player.getName())) {
                        jNote.play(player.getName(), sequencer, area);
                    }
                }
            }
        } else if (!chip.getInput(0) && sequencer != null) {
            sequencer.stop();
            jNote.stopAll();
            sequencer = null;
        }

        chip.setOutput(0, sequencer != null && sequencer.isPlaying());
    }

    SearchArea area;
    int delay;
    String tune;

    @Override
    public void load() {

        if (!getLine(3).isEmpty()) area = SearchArea.createArea(getLocation().getBlock(), getLine(3));
        else area = SearchArea.createEmptyArea();

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

        jNote = new JingleNoteManager();
    }

    @Override
    public void unload() {

        try {
            jNote.stopAll();
            sequencer.stop();
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