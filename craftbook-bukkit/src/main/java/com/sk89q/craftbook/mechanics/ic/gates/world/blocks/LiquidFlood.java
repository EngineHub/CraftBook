/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
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

package com.sk89q.craftbook.mechanics.ic.gates.world.blocks;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.RestrictedIC;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;

public class LiquidFlood extends AbstractSelfTriggeredIC {

    BlockVector3 radius;
    String liquid;
    Location centre;

    public LiquidFlood(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Liquid Flooder";
    }

    @Override
    public String getSignTitle() {

        return "LIQUID FLOOD";
    }

    @Override
    public void load() {

        centre = ICUtil.parseBlockLocation(getSign()).getLocation();
        radius = ICUtil.parseRadius(getSign()).toBlockPoint();

        liquid = getSign().getLine(2).equalsIgnoreCase("lava") ? "lava" : "water";
    }

    public void doStuff(ChipState chip) {

        if (chip.getInput(0)) {
            for (int x = -radius.getBlockX() + 1; x < radius.getBlockX(); x++) {
                for (int y = -radius.getBlockY() + 1; y < radius.getBlockY(); y++) {
                    for (int z = -radius.getBlockZ() + 1; z < radius.getBlockZ(); z++) {
                        int rx = centre.getBlockX() - x;
                        int ry = centre.getBlockY() - y;
                        int rz = centre.getBlockZ() - z;
                        Block b = CraftBookBukkitUtil.toSign(getSign()).getWorld().getBlockAt(rx, ry, rz);
                        if (b.getType() == Material.AIR || b.getType() == (liquid.equalsIgnoreCase("water") ? Material.WATER : Material.LAVA)) {
                            b.setType(liquid.equalsIgnoreCase("water") ? Material.WATER : Material.LAVA);
                        }
                    }
                }
            }
        } else if (!chip.getInput(0)) {
            for (int x = -radius.getBlockX() + 1; x < radius.getBlockX(); x++) {
                for (int y = -radius.getBlockY() + 1; y < radius.getBlockY(); y++) {
                    for (int z = -radius.getBlockZ() + 1; z < radius.getBlockZ(); z++) {
                        int rx = centre.getBlockX() - x;
                        int ry = centre.getBlockY() - y;
                        int rz = centre.getBlockZ() - z;
                        Block b = CraftBookBukkitUtil.toSign(getSign()).getWorld().getBlockAt(rx, ry, rz);
                        if (b.getType() == (liquid.equalsIgnoreCase("water") ? Material.WATER : Material.LAVA)) {
                            b.setType(Material.AIR);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void trigger(ChipState chip) {

        doStuff(chip);
    }

    @Override
    public void think(ChipState state) {

        doStuff(state);
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new LiquidFlood(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Floods an area with a liquid.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"+owater/lava", "+oradius=x:y:z offset"};
        }
    }
}