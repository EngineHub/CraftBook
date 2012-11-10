package com.sk89q.craftbook.bukkit.events;
import com.sk89q.craftbook.EventTrigger;
import org.bukkit.block.Block;

public class CraftBookBlockChangeEvent extends CraftBookEvent {

    private final Block block;
    private final Block toBlock;

    public CraftBookBlockChangeEvent(Block block, Block toBlock, EventTrigger trigger, boolean isDupeSafe) {

        super(trigger, isDupeSafe);
        this.block = block;
        this.toBlock = toBlock;
    }

    /**
     * Gets the original block
     *
     * @return the original block
     */
    public Block getBlock() {

        return block;
    }

    /**
     * Gets the new block
     *
     * @return the new block
     */
    public Block getToBlock() {

        return toBlock;
    }
}
