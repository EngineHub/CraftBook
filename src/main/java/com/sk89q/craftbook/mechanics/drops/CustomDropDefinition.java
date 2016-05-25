package com.sk89q.craftbook.mechanics.drops;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.mechanics.drops.rewards.DropReward;
import com.sk89q.craftbook.util.ItemUtil;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class CustomDropDefinition {

    private DropItemStack[] drops;
    private DropReward[] extraRewards;
    private String name;

    private boolean append;

    public CustomDropDefinition(String name, List<DropItemStack> drops, List<DropReward> extraRewards) {
        this.drops = drops.toArray(new DropItemStack[drops.size()]);
        if(extraRewards != null)
            this.extraRewards = extraRewards.toArray(new DropReward[extraRewards.size()]);
        this.name = name;
    }

    public void setAppend(boolean append) {

        this.append = append;
    }

    public boolean getAppend() {

        return append;
    }

    public DropReward[] getRewards() {
        if(extraRewards == null)
            extraRewards = new DropReward[0];
        return extraRewards;
    }

    public DropItemStack[] getDrops() {
        return drops;
    }

    public ItemStack[] getRandomDrops() {

        List<ItemStack> ndrops = new ArrayList<ItemStack>();

        for(DropItemStack drop : drops) {
            if(drop.getChance() < CraftBookPlugin.inst().getRandom().nextInt(100)) continue;
            ItemStack stack = drop.getStack().clone();
            if(drop.getMaximum() >= 0 && drop.getMinimum() >= 0) {
                int amount = drop.getMinimum() + CraftBookPlugin.inst().getRandom().nextInt(drop.getMaximum() - drop.getMinimum() + 1);
                if(amount <= 0) continue; //Invalid stack.
                stack.setAmount(amount);
            }

            if(ItemUtil.isStackValid(stack))
                ndrops.add(stack);
        }

        return ndrops.toArray(new ItemStack[ndrops.size()]);
    }

    public String getName() {

        return name;
    }
}