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

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.enginehub.craftbook.BukkitChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractIC;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.RestrictedIC;
import org.enginehub.craftbook.util.ICUtil;

import java.util.concurrent.ThreadLocalRandom;

public class LightningSummon extends AbstractIC {

    private Location center;
    private BlockVector3 radius;
    private int chance;

    public LightningSummon(Server server, BukkitChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public void load() {

        if (!getLine(2).isEmpty()) {
            radius = ICUtil.parseRadius(getSign()).toBlockPoint();
            if (getLine(2).contains("="))
                center = ICUtil.parseBlockLocation(getSign()).getLocation();
            else
                center = getBackBlock().getLocation();
        } else {
            center = getBackBlock().getLocation();
            radius = BlockVector3.at(1, 1, 1);
        }

        if (!getLine(3).isEmpty()) {
            try {
                chance = Math.min(Integer.parseInt(getLine(3)), 100);
            } catch (Exception e) {
                chance = 100;
            }
        } else {
            chance = 100;
        }
    }

    @Override
    public String getTitle() {

        return "Zeus Bolt";
    }

    @Override
    public String getSignTitle() {

        return "ZEUS BOLT";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {

            for (int x = -radius.x() + 1; x < radius.x(); x++) {
                for (int y = -radius.y() + 1; y < radius.y(); y++) {
                    for (int z = -radius.z() + 1; z < radius.z(); z++) {
                        int rx = center.getBlockX() - x;
                        int ry = center.getBlockY() - y;
                        int rz = center.getBlockZ() - z;
                        Block b = getSign().getBlock().getWorld().getBlockAt(rx, ry, rz);

                        if (b.getType() != Material.AIR && ThreadLocalRandom.current().nextInt(100) <= chance)
                            b.getWorld().strikeLightning(b.getLocation());
                    }
                }
            }
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(BukkitChangedSign sign) {

            return new LightningSummon(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Strike location with lightning!";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "+oradius=x:y:z block offset", "+ochance" };
        }
    }
}