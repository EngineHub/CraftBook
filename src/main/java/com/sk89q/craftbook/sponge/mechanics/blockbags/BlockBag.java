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

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an object that contains an inventory, and can be used as a source of blocks for CraftBook mechanics.
 */
public abstract class BlockBag {

    private long blockBagId;
    private String simpleName;
    private UUID creator;

    /**
     * Gets the ID of this Block Bag.
     *
     * @return The ID
     */
    public long getId() {
        return this.blockBagId;
    }

    /**
     * Sets the ID of this Block Bag.
     *
     * @param id The id
     */
    public void setId(long id) {
        this.blockBagId = id;
    }

    /**
     * Gets the simple name of this Block Bag.
     *
     * @return The simple name
     */
    public String getSimpleName() {
        return this.simpleName;
    }

    /**
     * Gets the creator of this Block Bag.
     *
     * @return The creator
     */
    public UUID getCreator() {
        return this.creator;
    }

    /**
     * Sets the creator of this Block Bag.
     *
     * @param creator The creator
     */
    public void setCreator(UUID creator) {
        this.creator = creator;
    }

    /**
     * Determines if this {@link BlockBag} contains the requested {@link ItemStack}s.
     *
     * @param itemStacks The requested items
     * @return if it contains the items
     */
    public abstract boolean has(List<ItemStack> itemStacks);

    /**
     * Adds the given {@link ItemStack}s to this {@link BlockBag}.
     *
     * @param itemStacks The items to add
     * @return All items that could not be added
     */
    public abstract List<ItemStack> add(List<ItemStack> itemStacks);

    /**
     * Removes the given {@link ItemStack}s from this {@link BlockBag}.
     *
     * @param itemStacks The items to remove
     * @return All items that could not be removed
     */
    public abstract List<ItemStack> remove(List<ItemStack> itemStacks);

    @Override
    public int hashCode() {
        return Objects.hash(this.blockBagId, this.simpleName, this.creator);
    }
}
