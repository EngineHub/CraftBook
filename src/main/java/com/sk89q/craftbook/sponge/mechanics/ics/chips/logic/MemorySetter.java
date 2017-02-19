/*
 * CraftBook Copyright (C) 2010-2017 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2017 me4502 <http://www.me4502.com>
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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class MemorySetter extends IC {

    private File accessFile;

    public MemorySetter(Factory factory, Location<World> location) {
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
        try(PrintWriter pw = new PrintWriter(accessFile, "UTF-8")) {
            for (int i = 0; i < getPinSet().getInputCount(); i++) {
                pw.print(getPinSet().getInput(i, this) ? "1" : "0");
            }
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class Factory implements ICFactory<MemorySetter> {

        @Override
        public MemorySetter createInstance(Location<World> location) {
            return new MemorySetter(this, location);
        }
    }
}