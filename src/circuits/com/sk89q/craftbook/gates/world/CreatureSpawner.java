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

import static com.sk89q.craftbook.ic.TripleInputChipState.input;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Sign;
import org.bukkit.entity.CreatureType;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.worldedit.blocks.BlockType;

public class CreatureSpawner extends AbstractIC {

    protected boolean risingEdge;

    public CreatureSpawner(Server server, Sign sign, boolean risingEdge) {
        super(server, sign);
        this.risingEdge = risingEdge;
    }

    @Override
    public String getTitle() {
        return "Creature Spawner";
    }

    @Override
    public String getSignTitle() {
        return "CREATURE SPAWNER";
    }

    @Override
    public void trigger(ChipState chip) {
        if (risingEdge && input(chip, 0) || (!risingEdge && !input(chip, 0))) {
            String type = getSign().getLine(2).trim();
            String rider = getSign().getLine(3).trim();
            if (CreatureType.fromName(type) != null) {
                Location loc = getSign().getBlock().getLocation();
                int maxY = Math.min(128, loc.getBlockY() + 10);
                int x = loc.getBlockX();
                int z = loc.getBlockZ();

                for (int y = loc.getBlockY() + 1; y <= maxY; y++) {
                    if (BlockType.canPassThrough(getSign().getWorld()
                            .getBlockTypeIdAt(x, y, z))) {
                        // TODO: Doesn't spawn riders yet.
                        if (rider.length() != 0
                                && CreatureType.fromName(rider) != null) {
                            getSign().getWorld()
                                    .spawnCreature(
                                            new Location(getSign().getWorld(),
                                                    x, y, z),
                                            CreatureType.fromName(type));
                        } else {
                            getSign().getWorld()
                                    .spawnCreature(
                                            new Location(getSign().getWorld(),
                                                    x, y, z),
                                            CreatureType.fromName(type));
                        }
                        return;
                    }
                }
            }
        }
    }

    public static class Factory extends AbstractICFactory {

        protected boolean risingEdge;

        public Factory(Server server, boolean risingEdge) {
            super(server);
            this.risingEdge = risingEdge;
        }

        @Override
        public IC create(Sign sign) {
            return new CreatureSpawner(getServer(), sign, risingEdge);
        }
    }

}
