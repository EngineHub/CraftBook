/*
 * CraftBook Copyright (C) 2010-2016 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2016 me4502 <http://www.me4502.com>
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
package com.sk89q.craftbook.sponge.blockbags;

import java.util.List;

import org.spongepowered.api.item.inventory.ItemStack;

/**
 * Represents an object that contains an inventory, and can be used as a source of blocks for CraftBook mechanics.
 */
public abstract class BlockBag {

    public abstract List<ItemStack> addItems(ItemStack... itemStacks);

    public abstract List<ItemStack> removeItems(ItemStack... itemStacks);
}
