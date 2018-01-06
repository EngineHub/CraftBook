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

import com.sk89q.craftbook.sponge.CraftBookPlugin;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import javax.annotation.Nullable;

public abstract class DataPrompt<T extends DataSerializable> {

    private int minSize;
    private int maxSize;
    private String title;
    @Nullable private BiPredicate<Player, List<ItemStack>> customValidityCheck;

    public DataPrompt(int minSize, int maxSize, String title) {
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.title = title;
    }

    public DataPrompt(int minSize, int maxSize, String title, @Nullable BiPredicate<Player, List<ItemStack>> customValidityCheck) {
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.title = title;
        this.customValidityCheck = customValidityCheck;
    }

    private void createInventory(Player player, Consumer<List<ItemStack>> consumer) {
        int height = (int) Math.ceil(maxSize / 9.0);
        int width = 9; //maxSize >= 9 ? 9 : maxSize;

        Consumer<InteractInventoryEvent.Close> closeListener = close -> {
            Inventory closedInventory = close.getTargetInventory();
            Optional<ItemStack> itemStack;
            List<ItemStack> foundItems = new ArrayList<>();
            while ((itemStack = closedInventory.first().poll()).isPresent()) {
                foundItems.add(itemStack.get());
            }
            consumer.accept(foundItems);
        };

        Inventory inventory = Inventory.builder()
                    .of(InventoryArchetypes.CHEST)
                    .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(this.title)))
                    .property(InventoryDimension.PROPERTY_NAME, InventoryDimension.of(width, height))
                    .listener(InteractInventoryEvent.Close.class, closeListener)
                    .build(CraftBookPlugin.spongeInst().container);

        player.openInventory(inventory);
    }

    public boolean isValid(Player player, List<ItemStack> items) {
        if (items.isEmpty()) {
            player.sendMessage(Text.of("You must provide an item!"));
            return false;
        }
        if (items.size() > this.maxSize) {
            player.sendMessage(Text.of("Provided too many items. Can only provide up to " + maxSize));
        }
        if (items.size() < this.minSize) {
            player.sendMessage(Text.of("Provided too few items. Must provide at least " + minSize));
        }
        if (this.customValidityCheck != null && !this.customValidityCheck.test(player, items)) {
            return false;
        }

        return true;
    }

    public abstract List<T> convertData(List<ItemStack> items);

    public void getData(Player player, Consumer<List<T>> callback) {
        createInventory(player, itemStacks -> {
            if (isValid(player, itemStacks)) {
                callback.accept(convertData(itemStacks));
            }
        });
    }
}
