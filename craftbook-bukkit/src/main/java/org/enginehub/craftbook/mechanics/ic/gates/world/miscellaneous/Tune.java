/*
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package org.enginehub.craftbook.mechanics.ic.gates.world.miscellaneous;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.enginehub.craftbook.bukkit.BukkitChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.util.RegexUtil;
import org.enginehub.craftbook.util.SearchArea;
import org.enginehub.craftbook.util.jinglenote.JingleNoteManager;
import org.enginehub.craftbook.util.jinglenote.StringJingleSequencer;

public class Tune extends AbstractSelfTriggeredIC {

    StringJingleSequencer sequencer;
    JingleNoteManager jNote;

    public Tune(Server server, BukkitChangedSign sign, ICFactory factory) {

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
            if (sequencer == null)
                sequencer = new StringJingleSequencer(tune, delay);
            if (sequencer.isPlaying() || !sequencer.hasPlayedBefore()) {
                for (Player player : getServer().getOnlinePlayers()) {
                    if (!area.isWithinArea(player.getLocation())) {
                        if (jNote.isPlaying(player.getName()))
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

        if (!getLine(3).isEmpty())
            area = SearchArea.createArea(getLocation().getBlock(), getLine(3));
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

            tune = getLine(2);
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

            return new String[] { "Delay:Tune", "Radius" };
        }

        @Override
        public IC create(BukkitChangedSign sign) {

            return new Tune(getServer(), sign, this);
        }
    }
}