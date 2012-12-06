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

package com.sk89q.craftbook.gates.world.blocks;

import org.bukkit.Server;
import org.bukkit.block.Block;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.worldedit.blocks.BlockID;


public class SetBlockAbove extends SetBlock {

    public SetBlockAbove(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Set Block Above";
    }

    @Override
    public String getSignTitle() {

        return "SET BLOCK ABOVE";
    }

    protected void doSet(Block body, int block, byte meta, boolean force) {

        int x = body.getX();
        int y = body.getY();
        int z = body.getZ();

        if (force || body.getWorld().getBlockAt(x, y + 1, z).getTypeId() == BlockID.AIR) {
            body.getWorld().getBlockAt(x, y + 1, z).setTypeId(block);
            if (meta != -1) {
                body.getWorld().getBlockAt(x, y + 1, z).setData(meta);
            }
        }
    }

    public static class Factory extends AbstractICFactory implements
    RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new SetBlockAbove(getServer(), sign, this);
        }
    }

}
