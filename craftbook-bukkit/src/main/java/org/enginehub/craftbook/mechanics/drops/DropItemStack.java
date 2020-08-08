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

package org.enginehub.craftbook.mechanics.drops;

import org.bukkit.inventory.ItemStack;

public class DropItemStack {

    private ItemStack stack;

    private double chance = 100d;
    private int minA = -1, maxA = -1;
    private String name;

    public DropItemStack(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStack getStack() {
        return stack;
    }

    public void setChance(double chance) {

        this.chance = chance;
    }

    public double getChance() {

        return chance;
    }

    public void setMinimum(int minA) {

        this.minA = minA;
    }

    public int getMinimum() {

        return minA;
    }

    public void setMaximum(int maxA) {

        this.maxA = maxA;
    }

    public int getMaximum() {

        return maxA;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }
}