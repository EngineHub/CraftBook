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

package org.enginehub.craftbook.mechanics.dispenser;

import org.enginehub.craftbook.util.EntityUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.entity.Entity;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * @author Me4502
 */
public class Fan extends Recipe {

    public Fan(Material[] recipe) {
        super(recipe);
    }

    public Fan() {
        super(new Material[] {
                Material.COBWEB,    Material.OAK_LEAVES,         Material.COBWEB,
                Material.OAK_LEAVES, Material.PISTON,    Material.OAK_LEAVES,
                Material.COBWEB,    Material.OAK_LEAVES,         Material.COBWEB
        });
    }

    @Override
    public boolean doAction(Block block, ItemStack item, Vector velocity, BlockDispenseEvent event) {
        Dispenser d = (Dispenser) block.getBlockData();
        BlockFace face = d.getFacing();
        Location dispenserLoc = block.getRelative(face).getLocation();
        for (Entity e : block.getWorld().getChunkAt(dispenserLoc).getEntities()) {
            if (EntityUtil.isEntityInBlock(e, dispenserLoc.getBlock())) {
                Vector dir = new Vector(d.getFacing().getModX(), d.getFacing().getModY(), d.getFacing().getModZ());
                e.setVelocity(e.getVelocity().add(dir).normalize().multiply(10));
            }
        }
        return true;
    }
}