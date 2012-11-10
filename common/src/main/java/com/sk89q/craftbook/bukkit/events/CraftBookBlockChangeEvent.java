package com.sk89q.craftbook.bukkit.events;
import com.sk89q.craftbook.CraftBlock;
import com.sk89q.craftbook.EventTrigger;

public class CraftBookBlockChangeEvent extends CraftBookEvent {

    private final CraftBlock block;
    private final CraftBlock toBlock;

    public CraftBookBlockChangeEvent(CraftBlock block, CraftBlock toBlock,
                                     EventTrigger trigger, boolean isDupeSafe) {

        super(trigger, isDupeSafe);
        this.block = block;
        this.toBlock = toBlock;
    }

    /**
     * Gets the original block
     *
     * @return the original block
     */
    public CraftBlock getBlock() {

        return block;
    }

    /**
     * Gets the new block
     *
     * @return the new block
     */
    public CraftBlock getToBlock() {

        return toBlock;
    }
}
