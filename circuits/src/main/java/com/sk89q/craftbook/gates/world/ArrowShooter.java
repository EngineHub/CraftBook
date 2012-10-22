// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.gates.world;


import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.SignUtil;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.util.Vector;

public class ArrowShooter extends AbstractIC {

    public ArrowShooter(Server server, Sign sign, ICFactory factory) {

        super(server, sign, factory);
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

        float speed = 0.6F;
        float spread = 12;
        float vert = 0;
        try {
            String[] velocity = getSign().getLine(2).trim().split(":");
            speed = Float.parseFloat(velocity[0]);
            spread = Float.parseFloat(velocity[1]);
            vert = Float.parseFloat(getSign().getLine(3).trim());
        } catch (Exception e) {
            getSign().setLine(2, speed + ":" + spread + ":" + vert);
            getSign().update();
        }

        if (speed > 2.0) {
            speed = 2F;
        } else if (speed < 0.2) {
            speed = 0.2F;
        }
        if (spread > 50) {
            spread = 50;
        } else if (spread < 0) {
            spread = 0;
        }
        if (vert > 1) {
            vert = 1;
        } else if (vert < -1) {
            vert = -1;
        }


        Block signBlock = getSign().getBlock();
        BlockFace face = SignUtil.getBack(signBlock);
        Block targetDir = signBlock.getRelative(face).getRelative(face);

        float x = targetDir.getX() - signBlock.getX();
        float z = targetDir.getZ() - signBlock.getZ();
        Vector velocity = new Vector(x, vert, z);
        Location shootLoc = new Location(getSign().getWorld(), targetDir.getX() + 0.5, targetDir.getY() + 0.5,
                targetDir.getZ() + 0.5);

        for (short i = 0; i < n; i++) {
            getSign().getWorld().spawnArrow(shootLoc, velocity, speed, spread);
        }
    }

    public static class Factory extends AbstractICFactory implements
    RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new ArrowShooter(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Shoots an arrow.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "speed:spread",
                    "vertical gain"
            };
            return lines;
        }
    }
}
