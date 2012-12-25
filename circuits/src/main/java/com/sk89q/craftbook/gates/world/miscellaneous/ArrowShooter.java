// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.gates.world.miscellaneous;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.ICUtil;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.util.SignUtil;

public class ArrowShooter extends AbstractIC {

    public ArrowShooter (Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    float speed = 1.6F;
    float spread = 12;
    float vert = 0.2F;

    @Override
    public void load () {
        try {
            String[] velocity = ICUtil.COLON_PATTERN.split(getSign().getLine(2).trim());
            speed = Float.parseFloat(velocity[0]);
            spread = Float.parseFloat(velocity[1]);
            vert = Float.parseFloat(getSign().getLine(3).trim());
        } catch (Exception e) {
            getSign().setLine(2, speed + ":" + spread + ":" + vert);
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
    public String getTitle () {

        return "Arrow Shooter";
    }

    @Override
    public String getSignTitle () {

        return "ARROW SHOOTER";
    }

    @Override
    public void trigger (ChipState chip) {

        if (chip.getInput(0)) {
            shootArrows(1);
        }
    }

    public void shootArrows (int n) {

        Block signBlock = BukkitUtil.toSign(getSign()).getBlock();
        BlockFace face = SignUtil.getBack(signBlock);
        Block targetDir = signBlock.getRelative(face).getRelative(face);

        float x = targetDir.getX() - signBlock.getX();
        float z = targetDir.getZ() - signBlock.getZ();
        Vector velocity = new Vector(x, vert, z);
        Location shootLoc = new Location(BukkitUtil.toSign(getSign()).getWorld(), targetDir.getX() + 0.5, targetDir.getY() + 0.5,
                targetDir.getZ() + 0.5);

        for (short i = 0; i < n; i++) {
            BukkitUtil.toSign(getSign()).getWorld().spawnArrow(shootLoc, velocity, speed, spread);
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory (Server server) {

            super(server);
        }

        @Override
        public IC create (ChangedSign sign) {

            return new ArrowShooter(getServer(), sign, this);
        }

        @Override
        public String getDescription () {

            return "Shoots an arrow.";
        }

        @Override
        public String[] getLineHelp () {

            String[] lines = new String[] { "speed:spread", "vertical gain" };
            return lines;
        }
    }
}
