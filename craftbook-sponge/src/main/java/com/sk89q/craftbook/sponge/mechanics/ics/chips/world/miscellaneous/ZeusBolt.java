/*
 * CraftBook Copyright (C) 2010-2018 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2018 me4502 <http://www.me4502.com>
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
package com.sk89q.craftbook.sponge.mechanics.ics.chips.world.miscellaneous;

import com.flowpowered.math.vector.Vector3d;
import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.mechanics.ics.RestrictedIC;
import com.sk89q.craftbook.sponge.mechanics.ics.factory.ICFactory;
import com.sk89q.craftbook.sponge.util.ParsingUtil;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.weather.Lightning;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.concurrent.ThreadLocalRandom;

public class ZeusBolt extends IC {

    private Location<World> center;
    private Vector3d radius;
    private int chance;

    public ZeusBolt(ICFactory<ZeusBolt> icFactory, Location<World> block) {
        super(icFactory, block);
    }

    @Override
    public void load() {
        super.load();

        if (!getLine(2).isEmpty()) {
            radius = ParsingUtil.parseRadius(getSign());
            if(getLine(2).contains("=")) {
                center = ParsingUtil.parseBlockLocation(getSign());
            } else {
                center = getBackBlock();
            }
        } else {
            center = getBackBlock();
            radius = new Vector3d(1, 1, 1);
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
    public void trigger() {
        if (getPinSet().getInput(0, this)) {
            for (int x = -radius.getFloorX() + 1; x < radius.getFloorX(); x++) {
                for (int y = -radius.getFloorY() + 1; y < radius.getFloorY(); y++) {
                    for (int z = -radius.getFloorZ() + 1; z < radius.getFloorZ(); z++) {
                        int rx = center.getBlockX() - x;
                        int ry = center.getBlockY() - y;
                        int rz = center.getBlockZ() - z;
                        Location<World> b = getBlock().getExtent().getLocation(rx, ry, rz);

                        if(b.getBlockType() != BlockTypes.AIR && ThreadLocalRandom.current().nextInt(100) <= chance) {
                            Lightning lightning = (Lightning) b.getExtent().createEntity(EntityTypes.LIGHTNING, b.getPosition());
                            b.getExtent().spawnEntity(lightning);
                        }
                    }
                }
            }
        }
    }

    public static class Factory implements ICFactory<ZeusBolt>, RestrictedIC {

        @Override
        public ZeusBolt createInstance(Location<World> location) {
            return new ZeusBolt(this, location);
        }

        @Override
        public String[] getLineHelp() {
            return new String[] {
                    "radius=x,z,y",
                    "Optional chance (Out of 100)"
            };
        }

        @Override
        public String[][] getPinHelp() {
            return new String[][] {
                    new String[] {
                            "Strike lightning on high"
                    },
                    new String[] {
                            "None"
                    }
            };
        }
    }
}
