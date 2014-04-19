package com.sk89q.craftbook.mech.drops;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.ItemUtil;

public abstract class CustomDropDefinition {

    private DropItemStack[] drops;
    private String name;

    private boolean append;

    public CustomDropDefinition(String name, List<DropItemStack> drops) {
        this.drops = drops.toArray(new DropItemStack[drops.size()]);
        this.name = name;
    }

    public void setAppend(boolean append) {

        this.append = append;
    }

    public boolean getAppend() {

        return append;
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
                int amount = drop.getMinimum() + (int)(CraftBookPlugin.inst().getRandom().nextDouble() * (drop.getMaximum() - drop.getMinimum() + 1));
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