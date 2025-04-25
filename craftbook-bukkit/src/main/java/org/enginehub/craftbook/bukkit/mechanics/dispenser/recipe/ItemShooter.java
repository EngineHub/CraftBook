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

package org.enginehub.craftbook.bukkit.mechanics.dispenser.recipe;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public class ItemShooter extends DispenserRecipe {

    private final Material itemId;

    public ItemShooter(String id, Material item, Material[] materials) {
        super(id, materials);
        itemId = item;
    }

    @Override
    public void apply(Block block, ItemStack item, BlockFace face) {
        Item itemEntity = (Item) block.getWorld().spawnEntity(generateLocation(block, face), EntityType.ITEM);
        itemEntity.setItemStack(new ItemStack(itemId, 1));
        itemEntity.setVelocity(generateVelocity(face));
    }
}
