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

import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractIC;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.RestrictedIC;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SignUtil;

public class ArrowShooter extends AbstractIC {

    public ArrowShooter(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    double speed;
    double spread;
    double vert;

    @Override
    public void load() {

        try {
            String[] velocity = RegexUtil.COLON_PATTERN.split(getSign().getLine(2).trim());
            speed = Double.parseDouble(velocity[0]);
            spread = Double.parseDouble(velocity[1]);
            vert = Double.parseDouble(getSign().getLine(3).trim());
        } catch (Exception e) {
            speed = 1.6f;
            spread = 12;
            vert = 0.2f;
            getSign().setLine(2, speed + ":" + spread);
            getSign().setLine(3, String.valueOf(vert));
            getSign().update(false);
        }

        if (speed > 10.0) {
            speed = 10F;
        } else if (speed < 0.1) {
            speed = 0.1F;
        }
        if (spread > 5000) {
            spread = 5000;
        } else if (spread < 0) {
            spread = 0;
        }
        if (vert > 100) {
            vert = 100;
        } else if (vert < -100) {
            vert = -100;
        }
    }

    @Override
    public String getTitle() {

        return "Arrow Shooter";
    }

    @Override
    public String getSignTitle() {

        return "ARROW SHOOTER";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            shootArrows(1);
        }
    }

    public void shootArrows(int n) {

        Block signBlock = CraftBookBukkitUtil.toSign(getSign()).getBlock();
        BlockFace face = SignUtil.getBack(signBlock);
        Block targetDir = signBlock.getRelative(face).getRelative(face);

        double x = targetDir.getX() - signBlock.getX();
        double z = targetDir.getZ() - signBlock.getZ();
        Vector velocity = new Vector(x, vert, z);
        Location shootLoc = new Location(CraftBookBukkitUtil.toSign(getSign()).getWorld(), targetDir.getX() + 0.5,
                targetDir.getY() + 0.5,
                targetDir.getZ() + 0.5);

        if(!shootLoc.getChunk().isLoaded())
            return;

        for (short i = 0; i < n; i++)
            CraftBookBukkitUtil.toSign(getSign()).getWorld().spawnArrow(shootLoc, velocity, (float)speed, (float)spread);
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new ArrowShooter(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Shoots an arrow.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"speed:spread", "vertical gain"};
        }
    }
}
