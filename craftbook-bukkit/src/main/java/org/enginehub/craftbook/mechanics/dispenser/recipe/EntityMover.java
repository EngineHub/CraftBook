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
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class EntityMover extends DispenserRecipe {

    private final double velocityMultiplier;

    public EntityMover(String id, double velocityMultiplier, Material[] materials) {
        super(id, materials);
        this.velocityMultiplier = velocityMultiplier;
    }

    @Override
    public void apply(Block block, ItemStack item, BlockFace face) {
        Block searchPoint = block.getRelative(face);
        BoundingBox cubeBox = BoundingBox.of(searchPoint).expand(face, 3);

        for (Entity e : block.getWorld().getNearbyEntities(cubeBox)) {
            Vector dir = new Vector(face.getModX(), face.getModY(), face.getModZ());
            e.setVelocity(e.getVelocity().add(dir).normalize().multiply(velocityMultiplier));
        }
    }
}
