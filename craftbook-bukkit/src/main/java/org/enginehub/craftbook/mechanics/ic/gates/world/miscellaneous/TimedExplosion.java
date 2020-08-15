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

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractIC;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.RestrictedIC;
import org.enginehub.craftbook.util.BlockUtil;
import org.enginehub.craftbook.util.ICUtil;
import org.enginehub.craftbook.util.RegexUtil;

public class TimedExplosion extends AbstractIC {

    private int ticks;
    private float yield;
    private boolean flamey;

    private Block center;

    public TimedExplosion(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public void load() {

        if (getLine(3).length() > 0 && !getLine(3).contains(":")) {
            getSign().setLine(2, getSign().getLine(2) + ":" + getSign().getLine(3));
            getSign().update(false);
        }
        try {
            ticks = Integer.parseInt(RegexUtil.COLON_PATTERN.split(getSign().getLine(2).replace("!", ""))[0]);
        } catch (Exception e) {
            ticks = -1;
        }

        try {
            yield = Float.parseFloat(RegexUtil.COLON_PATTERN.split(getSign().getLine(2).replace("!", ""))[1]);
        } catch (Exception e) {
            yield = -1;
        }

        try {
            flamey = getSign().getLine(2).endsWith("!");
        } catch (Exception ignored) {
        }

        center = ICUtil.parseBlockLocation(getSign(), 3);
    }

    @Override
    public String getTitle() {

        return "Timed Explosive";
    }

    @Override
    public String getSignTitle() {

        return "TIME BOMB";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            Location loc = center.getLocation();

            if (!loc.getChunk().isLoaded())
                return;

            while (loc.getBlock().getType().isSolid())
                loc = loc.add(0, 1, 0);
            TNTPrimed tnt = (TNTPrimed) loc.getWorld().spawnEntity(BlockUtil.getBlockCentre(loc.getBlock()),
                EntityType.PRIMED_TNT);
            tnt.setIsIncendiary(flamey);
            if (ticks > 0) {
                tnt.setFuseTicks(ticks);
            }
            if (yield > 0) {
                tnt.setYield(yield);
            }
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new TimedExplosion(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Spawn tnt with custom fuse and yield.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "time in ticks:radius (ending with ! makes fire)", "x:y:z offset" };
        }
    }
}
