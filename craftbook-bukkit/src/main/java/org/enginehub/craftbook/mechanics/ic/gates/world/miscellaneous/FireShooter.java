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

package org.enginehub.craftbook.mechanics.ic.gates.world.miscellaneous;

import org.enginehub.craftbook.bukkit.util.CraftBookBukkitUtil;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.SmallFireball;
import org.bukkit.util.Vector;

import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanics.ic.AbstractIC;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.RestrictedIC;
import org.enginehub.craftbook.util.RegexUtil;
import org.enginehub.craftbook.util.SignUtil;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Me4502
 */
public class FireShooter extends AbstractIC {

    private double speed;
    private double spread;
    private double vert;

    public FireShooter(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

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

        return "Fire Shooter";
    }

    @Override
    public String getSignTitle() {

        return "FIRE SHOOTER";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            shootFire(1);
        }
    }

    public void shootFire(int n) {

        Block signBlock = CraftBookBukkitUtil.toSign(getSign()).getBlock();
        BlockFace face = SignUtil.getBack(signBlock);
        Block targetDir = signBlock.getRelative(face).getRelative(face);

        double x = targetDir.getX() - signBlock.getX();
        double z = targetDir.getZ() - signBlock.getZ();
        Location shootLoc = new Location(
                CraftBookBukkitUtil.toSign(getSign()).getWorld(), targetDir.getX() + 0.5,targetDir.getY() + 0.5,targetDir.getZ() + 0.5);

        if(!shootLoc.getChunk().isLoaded())
            return;

        for (short i = 0; i < n; i++) {

            double f2 = Math.sqrt(x * x + vert * vert + z * z);

            double nx = x/f2;
            double ny = vert/f2;
            double nz = z/f2;
            nx += ThreadLocalRandom.current().nextGaussian() * 0.007499999832361937D * spread;
            ny += ThreadLocalRandom.current().nextGaussian() * 0.007499999832361937D * spread;
            nz += ThreadLocalRandom.current().nextGaussian() * 0.007499999832361937D * spread;
            nx *= speed;
            ny *= speed;
            nz *= speed;
            float f3 = (float) Math.sqrt(nx * nx + nz * nz);

            SmallFireball f = CraftBookBukkitUtil.toSign(getSign()).getWorld().spawn(shootLoc, SmallFireball.class);
            f.setVelocity(new Vector(nx,ny,nz));
            f.getLocation().setYaw((float) (Math.atan2(nx, nz) * 180.0D / 3.1415927410125732D));
            f.getLocation().setPitch((float) (Math.atan2(ny, f3) * 180.0D / 3.1415927410125732D));
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new FireShooter(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Shoots a fireball.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"speed:spread", "vertical gain"};
        }
    }
}