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
import org.enginehub.craftbook.BukkitChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractIC;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.ICManager;
import org.enginehub.craftbook.mechanics.ic.RestrictedIC;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class MemoryAccess extends AbstractIC {

    public MemoryAccess(Server server, BukkitChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "ROM Accessor";
    }

    @Override
    public String getSignTitle() {

        return "ROM";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            readMemory(chip);
        }
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

    public boolean readMemory(ChipState chip) {

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
            String line = br.readLine();
            for (int i = 0; i < chip.getOutputCount(); i++) {
                if (line == null || line.length() < i + 1)
                    chip.setOutput(i, false);
                else
                    chip.setOutput(i, line.charAt(i) == '1');
            }
            br.close();
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
                "The '''MC3301''' gets memory that can be set by the ([[../MC3301/]]) set to access the same file.",
                "",
                "This IC reads from a file in the filesystem stored in /plugins/CraftBook/rom/fileName.dat.",
                "This file can be accessed by other services to allow for external programs to interact with redstone."
            };
        }

        @Override
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                "Trigger IC",//Inputs
                "Bit 1 State",//Outputs
                "Bit 2 State",
                "Bit 3 State"
            };
        }

        @Override
        public String getShortDescription() {

            return "Gets the memory state from a file for usage in the MemorySetter/Access IC group.";
        }

        @Override
        public IC create(BukkitChangedSign sign) {

            return new MemoryAccess(getServer(), sign, this);
        }
    }
}
