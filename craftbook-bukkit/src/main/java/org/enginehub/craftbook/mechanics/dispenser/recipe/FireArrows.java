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

package org.enginehub.craftbook.mechanics.dispenser.recipe;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class FireArrows extends DispenserRecipe {

    public FireArrows(String id, Material[] materials) {
        super(id, materials);
    }

    @Override
    public void apply(Block block, ItemStack item, BlockFace face) {
        // Use an alternate velocity algorithm with less spread and more speed. Plus Y-axis shots.
        double speed = ThreadLocalRandom.current().nextDouble() * 0.2 + 0.4;
        double x = ThreadLocalRandom.current().nextGaussian() * 0.01 + face.getModX() * speed;
        double y = ThreadLocalRandom.current().nextGaussian() * 0.01 + face.getModY() * speed;
        double z = ThreadLocalRandom.current().nextGaussian() * 0.01 + face.getModZ() * speed;
        Vector velocity = new Vector(x, y, z);

        Arrow arrow = block.getWorld().spawnArrow(generateLocation(block, face), velocity, 1.0f, 0.0f);
        arrow.setFireTicks(5000);
    }
}
