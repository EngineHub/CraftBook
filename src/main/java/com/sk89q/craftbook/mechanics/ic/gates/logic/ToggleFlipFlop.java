// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

package com.sk89q.craftbook.mechanics.ic.gates.logic;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractIC;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;

public class ToggleFlipFlop extends AbstractIC {

    protected final boolean risingEdge;

    public ToggleFlipFlop(Server server, ChangedSign sign, boolean risingEdge, ICFactory factory) {

        super(server, sign, factory);
        this.risingEdge = risingEdge;
    }

    @Override
    public String getTitle() {

        return "Toggle Flip Flop";
    }

    @Override
    public String getSignTitle() {

        return "TOGGLE";
    }

    @Override
    public void trigger(ChipState chip) {

        if (risingEdge == chip.getInput(0)) {
            chip.setOutput(0, !chip.getOutput(0));
        }
    }

    public static class Factory extends AbstractICFactory {

        protected final boolean risingEdge;

        public Factory(Server server, boolean risingEdge) {

            super(server);
            this.risingEdge = risingEdge;
        }

        @Override
        public IC create(ChangedSign sign) {

            return new ToggleFlipFlop(getServer(), sign, risingEdge, this);
        }

        @Override
        public String[] getLongDescription() {

            return new String[]{
                    "The '''" + (risingEdge ? "MC1017" : "MC1018") + "''' is a toggle flip flop that toggles its output state between low and high whenever the input (the \"clock\") changes from "  + (risingEdge ? "low to high." : "high to low"),
                    "",
                    "Equivalent [[../Perlstone/]] script: <code>"  + (risingEdge ? "Lp1AdSp1.0100[Lp0!dSp0r]Lp0r" : "Lp1AdSp1.0010[Lp0!dSp0r]Lp0r") + "</code>",
                    "",
                    "== Uses ==",
                    "* Connect two buttons to this IC to have the buttons toggle a door when used."
            };
        }

        @Override
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                    "Trigger IC on " + (risingEdge ? "high" : "low"),//Inputs
                    "Output",//Outputs
            };
        }

        @Override
        public String getShortDescription() {

            return "Toggles output on " + (risingEdge ? "high." : "low.");
        }
    }
}