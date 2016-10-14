package com.sk89q.craftbook.mechanics.drops;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.mechanics.drops.rewards.DropReward;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.TernaryState;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class CustomDropDefinition {

    public static final DropReward[] EMPTY_DROP_REWARDS = new DropReward[0];

    private DropItemStack[] drops;
    private DropReward[] extraRewards;
    private String name;

    private boolean append;
    private TernaryState silkTouch;

    // WorldGuard Integration
    private List<String> regions;

    public CustomDropDefinition(String name, List<DropItemStack> drops, List<DropReward> extraRewards, TernaryState silkTouch) {
        this.drops = drops.toArray(new DropItemStack[drops.size()]);
        if(extraRewards != null)
            this.extraRewards = extraRewards.toArray(new DropReward[extraRewards.size()]);
        this.name = name;
        this.silkTouch = silkTouch;
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
    }

    public DropReward[] getRewards() {
        if(extraRewards == null)
            extraRewards = EMPTY_DROP_REWARDS;
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

    public TernaryState getSilkTouch() {
        return this.silkTouch;
    }

    public String getName() {

        return name;
    }
}