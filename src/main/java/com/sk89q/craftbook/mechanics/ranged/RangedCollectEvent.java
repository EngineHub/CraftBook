package com.sk89q.craftbook.mechanics.ranged;

import com.sk89q.craftbook.mechanics.pipe.PipeRequestEvent;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class RangedCollectEvent extends PipeRequestEvent {
    private final Item item;

    public RangedCollectEvent(Block theBlock, Item item, List<ItemStack> itemstacks, Block sucked) {
        super(theBlock, itemstacks, sucked);
        this.item = item;
    }

    public Item getItem() {
        return item;
    }
}
