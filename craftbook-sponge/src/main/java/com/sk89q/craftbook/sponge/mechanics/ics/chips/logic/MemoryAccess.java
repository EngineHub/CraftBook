/*
 * CraftBook Copyright (C) 2010-2018 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2018 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
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
package com.sk89q.craftbook.sponge.mechanics.ics.chips.logic;

import com.sk89q.craftbook.core.CraftBookAPI;
import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.mechanics.ics.InvalidICException;
import com.sk89q.craftbook.sponge.mechanics.ics.factory.ICFactory;
import com.sk89q.craftbook.sponge.util.SignUtil;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class MemoryAccess extends IC {

    private File accessFile;

    public MemoryAccess(Factory factory, Location<World> location) {
        super(factory, location);
    }

    @Override
    public void create(Player player, List<Text> lines) throws InvalidICException {
        super.create(player, lines);

        if (SignUtil.getTextRaw(lines.get(2)).trim().isEmpty()) {
            throw new InvalidICException("Must enter filename on 3rd line.");
        }
    }

    @Override
    public void load() {
        super.load();

        accessFile = new File(new File(CraftBookAPI.inst().getWorkingDirectory(), "rom"), getLine(2) + ".dat");
        if (!accessFile.exists())  {
            try {
                accessFile.getParentFile().mkdirs();
                accessFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void trigger() {
        if (getPinSet().getInput(0, this)) {
            try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(accessFile), "UTF-8"))) {
                String line = br.readLine();
                for (int i = 0; i < getPinSet().getOutputCount(); i++) {
                    if (line == null || line.length() < i + 1) {
                        getPinSet().setOutput(i, false, this);
                    } else {
                        getPinSet().setOutput(i, line.charAt(i) == '1', this);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class Factory implements ICFactory<MemoryAccess> {

        @Override
        public MemoryAccess createInstance(Location<World> location) {
            return new MemoryAccess(this, location);
        }

        @Override
        public String[] getLineHelp() {
            return new String[] {
                    "Accessed Filename",
                    ""
            };
        }

        @Override
        public String[][] getPinHelp() {
            return new String[][] {
                    new String[] {
                            "High to read from file"
                    },
                    new String[] {
                            "First digit of file",
                            "Second digit of file",
                            "Third digit of file"
                    }
            };
        }
    }
}
