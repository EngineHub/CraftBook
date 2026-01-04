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

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import org.enginehub.craftbook.bukkit.BukkitChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractIC;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.RestrictedIC;
import org.enginehub.craftbook.util.RegexUtil;
import org.enginehub.craftbook.util.SignUtil;

public class ArrowShooter extends AbstractIC {

    public ArrowShooter(Server server, BukkitChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    double speed;
    double spread;
    double vert;

    @Override
    public void load() {

        try {
            String[] velocity = RegexUtil.COLON_PATTERN.split(getLine(2).trim());
            speed = Double.parseDouble(velocity[0]);
            spread = Double.parseDouble(velocity[1]);
            vert = Double.parseDouble(getLine(3).trim());
        } catch (Exception e) {
            speed = 1.6f;
            spread = 12;
            vert = 0.2f;
            getSign().setLine(2, Component.text(speed + ":" + spread));
            getSign().setLine(3, Component.text(vert));
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

        Block signBlock = getSign().getBlock();
        BlockFace face = SignUtil.getBack(signBlock);
        Block targetDir = signBlock.getRelative(face).getRelative(face);

        double x = targetDir.getX() - signBlock.getX();
        double z = targetDir.getZ() - signBlock.getZ();
        Vector velocity = new Vector(x, vert, z);
        Location shootLoc = new Location(getSign().getBlock().getWorld(), targetDir.getX() + 0.5,
            targetDir.getY() + 0.5,
            targetDir.getZ() + 0.5);

        if (!shootLoc.getChunk().isLoaded())
            return;

        for (short i = 0; i < n; i++)
            getSign().getBlock().getWorld().spawnArrow(shootLoc, velocity, (float) speed, (float) spread);
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(BukkitChangedSign sign) {

            return new ArrowShooter(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Shoots an arrow.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "speed:spread", "vertical gain" };
        }
    }
}
