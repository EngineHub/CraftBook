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

import com.sk89q.worldedit.registry.Keyed;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public abstract class DispenserRecipe implements Keyed {

    private final String id;
    private final Material[] recipe;

    public DispenserRecipe(String id, Material[] materials) {
        this.id = id;
        this.recipe = materials;
    }

    protected Vector generateVelocity(BlockFace direction) {
        double speed = ThreadLocalRandom.current().nextDouble() * 0.1 + 0.2;
        double x = ThreadLocalRandom.current().nextGaussian() * 0.044999999 + direction.getModX() * speed;
        double y = ThreadLocalRandom.current().nextGaussian() * 0.044999999 + 0.20000000298023224;
        double z = ThreadLocalRandom.current().nextGaussian() * 0.044999999 + direction.getModZ() * speed;

        return new Vector(x, y, z);
    }

    protected Location generateLocation(Block block, BlockFace direction) {
        Location firePosition = block.getLocation().toCenterLocation();
        // Offset the position based on the dispenser direction.
        // This math is based off the vanilla MC math.
        firePosition.add(0.7 * direction.getModX(), 0.7 * direction.getModY(), 0.7 * direction.getModZ());
        firePosition.setY(firePosition.getY() - (direction.getModY() != 0 ? 0.125D : 0.15625D));

        return firePosition;
    }

    /**
     * Apply the recipe action to the world.
     * @param block the dispenser firing the item
     * @param item the original item to be fired
     * @param face the face to fire from
     */
    public abstract void apply(Block block, ItemStack item, BlockFace face);

    /**
     * Gets the contents of this recipe as a 9-element array representing the 3x3 dispenser grid.
     *
     * @return the recipe contents
     */
    public Material[] getRecipe() {
        return this.recipe;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof DispenserRecipe && Objects.equals(this.id, ((DispenserRecipe) o).getId()) && Arrays.equals(recipe, ((DispenserRecipe) o).recipe);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(recipe);
    }
}
