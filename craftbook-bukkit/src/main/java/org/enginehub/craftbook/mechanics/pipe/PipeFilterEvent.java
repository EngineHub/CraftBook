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

package org.enginehub.craftbook.mechanics.pipe;

import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

public class PipeFilterEvent extends PipeEvent {

    private static final HandlerList handlers = new HandlerList();

    private Set<ItemStack> includeFilters;
    private Set<ItemStack> excludeFilters;
    private List<ItemStack> filteredItems;

    public PipeFilterEvent(Block theBlock, List<ItemStack> items, Set<ItemStack> includeFilters, Set<ItemStack> excludeFilters, List<ItemStack> filteredItems) {
        super(theBlock, items);

        this.includeFilters = includeFilters;
        this.excludeFilters = excludeFilters;
        this.filteredItems = filteredItems;
    }

    public Set<ItemStack> getIncludeFilters() {
        return includeFilters;
    }

    public Set<ItemStack> getExcludeFilters() {
        return excludeFilters;
    }

    public List<ItemStack> getFilteredItems() {
        return filteredItems;
    }

    public void setFilteredItems(List<ItemStack> filteredItems) {
        this.filteredItems = filteredItems;
    }

    @Override
    @Nonnull
    public HandlerList getHandlers() {
        return handlers;
    }

    @Nonnull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
