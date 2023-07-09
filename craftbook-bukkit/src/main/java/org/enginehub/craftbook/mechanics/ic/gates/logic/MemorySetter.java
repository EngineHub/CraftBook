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

package org.enginehub.craftbook.mechanics.ic.gates.logic;

import org.bukkit.Server;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractIC;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.ICManager;
import org.enginehub.craftbook.mechanics.ic.RestrictedIC;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class MemorySetter extends AbstractIC {

    public MemorySetter(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Memory Setter";
    }

    @Override
    public String getSignTitle() {

        return "MEMORY SET";
    }

    @Override
    public void trigger(ChipState chip) {

        setMemory(chip);
    }

    File f;

    @Override
    public void load() {

        f = new File(ICManager.inst().getRomFolder(), getLine(2) + ".dat");
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean setMemory(ChipState chip) {

        try {
            PrintWriter pw = new PrintWriter(f, "UTF-8");
            for (int i = 0; i < chip.getInputCount(); i++)
                pw.print(chip.getInput(i) ? "1" : "0");
            pw.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public String[] getLongDescription() {

            return new String[] {
                "The '''MC3300''' sets memory that can be read by the ([[../MC3301/]]) set to access the same file.",
                "",
                "This IC writes to a file in the filesystem stored in /plugins/CraftBook/rom/fileName.dat.",
                "This file can be accessed by other services to allow for external programs to interact with redstone."
            };
        }

        @Override
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                "Bit to set 1",//Inputs
                "Bit to set 2",
                "Bit to set 3",
                "Nothing"//Outputs
            };
        }

        @Override
        public String getShortDescription() {

            return "Sets the memory state for a file for usage in the MemorySetter/Access IC group.";
        }

        @Override
        public IC create(ChangedSign sign) {

            return new MemorySetter(getServer(), sign, this);
        }
    }
}