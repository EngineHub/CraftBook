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
package com.sk89q.craftbook.sponge.util.prompt;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class BlockStateDataPrompt extends DataPrompt<BlockState> {

    public BlockStateDataPrompt(int minSize, int maxSize, String title) {
        super(minSize, maxSize, title);
    }

    public BlockStateDataPrompt(int minSize, int maxSize, String title, @Nullable BiPredicate<Player, List<ItemStack>> customValidityCheck) {
        super(minSize, maxSize, title, customValidityCheck);
    }

    @Override
    public boolean isValid(Player player, List<ItemStack> items) {
        if (!super.isValid(player, items)) {
            return false;
        }

        for (ItemStack item : items) {
            if (!item.supports(Keys.ITEM_BLOCKSTATE) && !item.getType().getBlock().isPresent()) {
                player.sendMessage(Text.of("Item '" + item.getType().getName() + "' is not a block!"));
                return false;
            }
        }

        return true;
    }

    @Override
    public List<BlockState> convertData(List<ItemStack> items) {
        return items.stream()
                .map(item -> item.get(Keys.ITEM_BLOCKSTATE).orElse(item.getType().getBlock().orElse(BlockTypes.AIR).getDefaultState()))
                .collect(Collectors.toList());
    }
}
