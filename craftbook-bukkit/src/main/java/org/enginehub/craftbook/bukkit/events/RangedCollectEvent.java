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

package org.enginehub.craftbook.bukkit.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.enginehub.craftbook.mechanics.pipe.PipeRequestEvent;

import java.util.List;

public class RangedCollectEvent extends PipeRequestEvent {
    private final Item item;

    public RangedCollectEvent(Block theBlock, Item item, List<ItemStack> itemstacks, Block sucked) {
        super(theBlock, itemstacks, sucked);
        this.item = item;
    }

    public Item getItem() {
        return item;
    }
}
