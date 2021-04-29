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

package org.enginehub.craftbook.mechanics.dispenser.recipe;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;

public class Cannon extends DispenserRecipe {

    public Cannon(String id, Material[] materials) {
        super(id, materials);
    }

    @Override
    public void apply(Block block, ItemStack item, BlockFace face) {
        TNTPrimed a = (TNTPrimed) block.getWorld().spawnEntity(generateLocation(block, face), EntityType.PRIMED_TNT);
        a.setVelocity(generateVelocity(face).normalize().multiply(2));
    }
}
