// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

package com.sk89q.craftbook.mech.ic.world;

import com.sk89q.craftbook.BlockType;
import com.sk89q.craftbook.access.Configuration;
import com.sk89q.craftbook.access.ServerInterface;
import com.sk89q.craftbook.access.WorldInterface;
import com.sk89q.craftbook.mech.ic.*;
import com.sk89q.craftbook.util.SignText;
import com.sk89q.craftbook.util.Vector;

/**
 * Mob spawner.
 *
 * @author sk89q
 */
public class MC1200 extends BaseIC {

    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {

        return "MOB SPAWNER";
    }

    /**
     * Returns true if this IC requires permission to use.
     *
     * @return
     */
    public boolean requiresPermission() {

        return true;
    }

    /**
     * Validates the IC's environment. The position of the sign is given.
     * Return a string in order to state an error message and deny
     * creation, otherwise return null to allow.
     *
     * @param sign
     *
     * @return
     */
    public String validateEnvironment(ServerInterface i, WorldInterface world, Vector pos, SignText sign) {

        Configuration c = i.getConfiguration();

        String id = sign.getLine3();
        String rider = sign.getLine4();

        if (id.length() == 0) {
            return "Specify a mob type on the third line.";
        } else if (!c.isValidMob(id)) {
            return "Not a valid mob type: " + id + ".";
        } else if (rider.length() != 0 && !c.isValidMob(rider)) {
            return "Not a valid rider type: " + rider + ".";
        }

        return null;
    }

    /**
     * Think.
     *
     * @param chip
     */
    public void think(ChipState chip) {

        Configuration c = chip.getServer().getConfiguration();
        WorldInterface world = chip.getWorld();

        if (chip.getIn(1).is()) {
            String id = chip.getText().getLine3();
            String rider = chip.getText().getLine4();

            if (c.isValidMob(id)) {
                Vector pos = chip.getBlockPosition();
                int maxY = Math.min(128, pos.getBlockY() + 10);
                int x = pos.getBlockX();
                int z = pos.getBlockZ();

                for (int y = pos.getBlockY() + 1; y <= maxY; y++) {
                    if (BlockType.canPassThrough(world.getId(x, y, z))) {
                        if (rider.length() != 0 && c.isValidMob(rider)) {
                            world.spawnMob(x, y, z, id, rider);
                        } else {
                            world.spawnMob(x, y, z, id);
                        }
                        return;
                    }
                }
            }
        }
    }
}
