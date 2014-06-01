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
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;

public class AndGate extends SimpleAnyInputLogicGate {

    public AndGate(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "And Gate";
    }

    @Override
    public String getSignTitle() {

        return "AND";
    }

    @Override
    protected boolean getResult(int wires, int on) {

        return wires > 0 && on == wires;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new AndGate(getServer(), sign, this);
        }

        @Override
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                    "Input",//Inputs
                    "Input",
                    "Input",
                    "High if all inputs are high, and there is atleast 1 input",//Outputs
            };
        }

        @Override
        public String getShortDescription() {

            return "Outputs high if all inputs are high.";
        }

        @Override
        public String[] getLongDescription() {

            return new String[]{
                    "{| class='wiki-table' style='float: right; clear: both'",
                    "! A !! B !! C !! Output",
                    "|-",
                    "| 0 || 0 || 0 || 0",
                    "|-",
                    "| 0 || 0 || 1 || 0",
                    "|-",
                    "| 0 || 1 || 1 || 0",
                    "|-",
                    "| 0 || 1 || 0 || 0",
                    "|-",
                    "| 1 || 1 || 0 || 0",
                    "|-",
                    "| 1 || 0 || 0 || 0",
                    "|-",
                    "| 1 || 0 || 1 || 0",
                    "|-",
                    "| 1 || 1 || 1 || 1",
                    "|}",
                    "",
                    "The '''MC3002''' outputs a high if and only if all three inputs are high.",
                    "As of Craftbook alpha 3 or equivalent, any combination of inputs is valid and all 3 are not needed.",
                    "",
                    "Equivalent [[../Perlstone/]] script: <code>ABC&&r</code>"
            };
        }
    }
}