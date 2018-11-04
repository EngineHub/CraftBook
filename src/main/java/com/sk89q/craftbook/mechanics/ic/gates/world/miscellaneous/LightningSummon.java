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

package com.sk89q.craftbook.mechanics.ic.gates.world.miscellaneous;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.mechanics.ic.AbstractIC;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
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

public class LightningSummon extends AbstractIC {

    private Location center;
    private BlockVector3 radius;
    private int chance;

    public LightningSummon(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public void load() {

        if (!getLine(2).isEmpty()) {
            radius = ICUtil.parseRadius(getSign()).toBlockPoint();
            if(getLine(2).contains("="))
                center = ICUtil.parseBlockLocation(getSign()).getLocation();
            else
                center = getBackBlock().getLocation();
        } else {
            center = getBackBlock().getLocation();
            radius = BlockVector3.at(1,1,1);
        }

        if(!getLine(3).isEmpty()) {
            try {
                chance = Math.min(Integer.parseInt(getLine(3)), 100);
            } catch(Exception e){
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

            for (int x = -radius.getBlockX() + 1; x < radius.getBlockX(); x++) {
                for (int y = -radius.getBlockY() + 1; y < radius.getBlockY(); y++) {
                    for (int z = -radius.getBlockZ() + 1; z < radius.getBlockZ(); z++) {
                        int rx = center.getBlockX() - x;
                        int ry = center.getBlockY() - y;
                        int rz = center.getBlockZ() - z;
                        Block b = CraftBookBukkitUtil.toSign(getSign()).getWorld().getBlockAt(rx, ry, rz);

                        if(b.getType() != Material.AIR && CraftBookPlugin.inst().getRandom().nextInt(100) <= chance)
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
        public IC create(ChangedSign sign) {

            return new LightningSummon(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Strike location with lightning!";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"+oradius=x:y:z block offset", "+ochance"};
        }
    }
}