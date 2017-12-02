/*
 * CraftBook Copyright (C) 2010-2017 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2017 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
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
package com.sk89q.craftbook.sponge.mechanics.dispenser;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.block.tileentity.carrier.Dispenser;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.explosive.PrimedTNT;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class Cannon extends SimpleDispenserRecipe {

    public Cannon() {
        super(new ItemStack[]{
                ItemStack.of(ItemTypes.FIRE_CHARGE, 1), ItemStack.of(ItemTypes.GUNPOWDER, 1), ItemStack.of(ItemTypes.FIRE_CHARGE, 1),
                ItemStack.of(ItemTypes.GUNPOWDER, 1), ItemStack.of(ItemTypes.TNT, 1), ItemStack.of(ItemTypes.GUNPOWDER, 1),
                ItemStack.of(ItemTypes.FIRE_CHARGE, 1), ItemStack.of(ItemTypes.GUNPOWDER, 1), ItemStack.of(ItemTypes.FIRE_CHARGE, 1),
        });
    }

    @Override
    public boolean doAction(Dispenser dispenser, ItemStack[] recipe, Vector3d velocity) {
        Direction face = dispenser.getLocation().get(Keys.DIRECTION).orElse(Direction.NONE);
        if (face != Direction.NONE) {
            Location<World> location = dispenser.getLocation().getRelative(face).add(0.5, 0.5, 0.5);
            PrimedTNT tnt = (PrimedTNT) dispenser.getWorld().createEntity(EntityTypes.PRIMED_TNT, location.getPosition());
            tnt.setVelocity(velocity.normalize().mul(2f));
            dispenser.getWorld().spawnEntity(tnt);
            return true;
        }

        return false;
    }
}
