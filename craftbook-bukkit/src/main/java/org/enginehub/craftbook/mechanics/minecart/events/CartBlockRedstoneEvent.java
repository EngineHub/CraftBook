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

package org.enginehub.craftbook.mechanics.minecart.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Minecart;
import org.bukkit.event.HandlerList;
import org.enginehub.craftbook.mechanics.minecart.blocks.CartMechanismBlocks;
import org.enginehub.craftbook.util.events.SourcedBlockRedstoneEvent;
import org.jspecify.annotations.Nullable;

public class CartBlockRedstoneEvent extends SourcedBlockRedstoneEvent {
    private static final HandlerList handlers = new HandlerList();

    private final CartMechanismBlocks blocks;
    private final Minecart minecart;

    public CartBlockRedstoneEvent(Block source, Block block, int old, int n, CartMechanismBlocks blocks, Minecart minecart) {
        super(source, block, old, n);

        this.blocks = blocks;
        this.minecart = minecart;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public CartMechanismBlocks getBlocks() {
        return this.blocks;
    }

    /**
     * The minecart at this mechanic, if present.
     *
     * @return the minecart, if present
     */
    public @Nullable Minecart getMinecart() {
        return this.minecart;
    }
}
