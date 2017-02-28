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
package com.sk89q.craftbook.sponge.mechanics.blockbags;

import org.spongepowered.api.item.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminBlockBag extends BlockBag {

    public AdminBlockBag() {
        this.setCreator(new UUID(0, 0));
    }

    @Override
    public long getId() {
        return -1;
    }

    @Override
    public String getSimpleName() {
        return "Admin";
    }

    @Override
    public boolean has(List<ItemStack> itemStacks) {
        return true;
    }

    @Override
    public List<ItemStack> add(List<ItemStack> itemStacks) {
        return new ArrayList<>();
    }

    @Override
    public List<ItemStack> remove(List<ItemStack> itemStacks) {
        return new ArrayList<>();
    }
}
