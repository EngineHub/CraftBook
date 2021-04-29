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

package org.enginehub.craftbook.mechanics.drops;

import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;
import org.enginehub.craftbook.mechanics.drops.rewards.DropReward;
import org.enginehub.craftbook.util.ItemUtil;
import org.enginehub.craftbook.util.TernaryState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public abstract class CustomDropDefinition {

    public static final DropReward[] EMPTY_DROP_REWARDS = new DropReward[0];

    private DropItemStack[] drops;
    private DropReward[] extraRewards;
    private String name;
    private String permissionNode;

    private boolean append;
    private TernaryState silkTouch;

    // WorldGuard Integration
    private List<String> regions;

    // Requirements
    private List<ItemStack> items;
    private List<Biome> biomes;

    public CustomDropDefinition(String name, List<DropItemStack> drops, List<DropReward> extraRewards, TernaryState silkTouch) {
        this.drops = drops.toArray(new DropItemStack[drops.size()]);
        if (extraRewards != null)
            this.extraRewards = extraRewards.toArray(new DropReward[extraRewards.size()]);
        this.name = name;
        this.silkTouch = silkTouch;
    }

    public void setPermissionNode(String permissionNode) {
        this.permissionNode = permissionNode;
    }

    public String getPermissionNode() {
        return permissionNode;
    }

    public void setAppend(boolean append) {
        this.append = append;
    }

    public boolean getAppend() {
        return append;
    }

    public List<String> getRegions() {
        return regions;
    }

    public void setRegions(List<String> regions) {
        this.regions = regions;
        if (this.regions.isEmpty()) {
            this.regions = null;
        }
    }

    public List<Biome> getBiomes() {
        return this.biomes;
    }

    public void setBiomes(List<Biome> biomes) {
        this.biomes = biomes;
    }

    public List<ItemStack> getItems() {
        return this.items;
    }

    public void setItems(List<ItemStack> items) {
        this.items = items;
        if (this.items.isEmpty()) {
            this.items = null;
        }
    }

    public DropReward[] getRewards() {
        if (extraRewards == null)
            extraRewards = EMPTY_DROP_REWARDS;
        return extraRewards;
    }

    public DropItemStack[] getDrops() {
        return drops;
    }

    public ItemStack[] getRandomDrops() {

        List<ItemStack> ndrops = new ArrayList<>();

        for (DropItemStack drop : drops) {
            if (drop.getChance() < ThreadLocalRandom.current().nextDouble() * 100d) continue;
            ItemStack stack = drop.getStack().clone();
            if (drop.getMaximum() >= 0 && drop.getMinimum() >= 0) {
                int amount = drop.getMinimum() + ThreadLocalRandom.current().nextInt(drop.getMaximum() - drop.getMinimum() + 1);
                if (amount <= 0) continue; //Invalid stack.
                stack.setAmount(amount);
            }

            if (ItemUtil.isStackValid(stack))
                ndrops.add(stack);
        }

        return ndrops.toArray(new ItemStack[ndrops.size()]);
    }

    public TernaryState getSilkTouch() {
        return this.silkTouch;
    }

    public String getName() {

        return name;
    }
}