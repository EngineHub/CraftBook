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

package com.sk89q.craftbook.bukkit;

import java.util.logging.Logger;
import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.util.Vector;

import static com.sk89q.craftbook.cart.CartUtils.*;

import com.sk89q.craftbook.VehiclesConfiguration;
import com.sk89q.craftbook.cart.*;

/**
 * Preprocesses event data coming directly from bukkit and passes it off to
 * appropriate logic in MinecartManager.
 */
public class CraftBookBlockListener extends BlockListener {
    public CraftBookBlockListener(VehiclesPlugin plugin) {
        this.plugin = plugin;
    }

    protected VehiclesPlugin plugin;

    /**
     * Called when a redstone current changes.
     */
    @Override
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        Block block = event.getBlock();
        int power = event.getNewCurrent();

        //check if it's a station block getting power
        ArrayList<Block> poweredBlocks = poweredByRSCEvent(event);
        for (Block railBlock : poweredBlocks) {
            if (railBlock.getType() == Material.RAILS &&
                (railBlock.getFace(BlockFace.DOWN).getType()
                     == plugin.getLocalConfiguration().matStation) &&
                (pickDirector(railBlock, "station")) != null) {
                //so we have rails, a station block, and a sign, this is a station right?
                Entity[] entities = railBlock.getChunk().getEntities();
                for (Entity entity : entities) {
                    if (entity instanceof Minecart &&
                          entity.getLocation().getBlock() == railBlock) {
                        (new CartStation()).launch(((Minecart) entity),
                             pickDirector(railBlock, "station"));
                    }
                }
            }
        }
    }
}
