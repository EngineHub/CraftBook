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

package com.sk89q.craftbook.mechanics.dispenser;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.entity.Arrow;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * @author Me4502
 */
public class FireArrows extends Recipe {

    public FireArrows(Material[] recipe) {

        super(recipe);
    }

    public FireArrows() {

        super(new Material[] {
                Material.AIR,            Material.FIRE_CHARGE,     Material.AIR,
                Material.FIRE_CHARGE,     Material.ARROW,           Material.FIRE_CHARGE,
                Material.AIR,            Material.FIRE_CHARGE,     Material.AIR
        });
    }

    @Override
    public boolean doAction(Block block, ItemStack item, Vector velocity, BlockDispenseEvent event) {
        Dispenser disp = (Dispenser) block.getBlockData();
        BlockFace face = disp.getFacing();
        Location location = block.getRelative(face).getLocation().add(0.5, 0.5, 0.5);
        Arrow a = block.getWorld().spawnArrow(location, velocity, 1.0f, 0.0f);
        a.setFireTicks(5000);
        return true;
    }
}