/*
 * CraftBook Copyright (C) 2010-2018 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2018 me4502 <http://www.me4502.com>
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

import com.sk89q.craftbook.sponge.util.LocationUtil;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.carrier.Dispenser;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class Vacuum extends SimpleDispenserRecipe {

    public Vacuum() {
        super(new ItemStack[]{
                ItemStack.of(ItemTypes.WEB, 1), ItemStack.of(ItemTypes.LEAVES, 1), ItemStack.of(ItemTypes.WEB, 1),
                ItemStack.of(ItemTypes.LEAVES, 1), ItemStack.of(ItemTypes.STICKY_PISTON, 1), ItemStack.of(ItemTypes.LEAVES, 1),
                ItemStack.of(ItemTypes.WEB, 1), ItemStack.of(ItemTypes.LEAVES, 1), ItemStack.of(ItemTypes.WEB, 1),
        });
    }

    @Override
    public boolean doAction(Dispenser dispenser, ItemStack[] recipe) {
        Direction face = dispenser.getLocation().get(Keys.DIRECTION).orElse(Direction.NONE);
        if (face != Direction.NONE) {
            Location<World> offset = dispenser.getLocation().getRelative(face);
            int distance = 0;
            while (offset.getBlockType() == BlockTypes.AIR && distance < 5) {
                for (Entity e : LocationUtil.getEntitiesAtLocation(offset)) {
                    e.setVelocity(face.asOffset().mul(5 - distance).mul(-1.0));
                }
                distance ++;
                offset = offset.getRelative(face);
            }
            return true;
        }
        return false;
    }
}
