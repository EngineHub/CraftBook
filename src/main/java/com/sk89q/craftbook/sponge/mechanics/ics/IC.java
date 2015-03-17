package com.sk89q.craftbook.sponge.mechanics.ics;

import org.spongepowered.api.block.BlockLoc;

public abstract class IC {

    ICType<? extends IC> type;
    BlockLoc block;

    public IC(ICType<? extends IC> type, BlockLoc block) {
        this.type = type;
        this.block = block;
    }

    public BlockLoc getBlock() {
        return block;
    }

    public ICType<? extends IC> getType() {
        return type;
    }

    public abstract void trigger(PinSet pinset);
}
