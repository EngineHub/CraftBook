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
package org.enginehub.craftbook.sponge.util.prompt;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class ItemStackSnapshotDataPrompt extends DataPrompt<ItemStackSnapshot> {

    public ItemStackSnapshotDataPrompt(int minSize, int maxSize, String title) {
        super(minSize, maxSize, title);
    }

    public ItemStackSnapshotDataPrompt(int minSize, int maxSize, String title, @Nullable BiPredicate<Player, List<ItemStack>> customValidityCheck) {
        super(minSize, maxSize, title, customValidityCheck);
    }

    @Override
    public List<ItemStackSnapshot> convertData(List<ItemStack> items) {
        return items.stream().map(ItemStack::createSnapshot).collect(Collectors.toList());
    }
}
