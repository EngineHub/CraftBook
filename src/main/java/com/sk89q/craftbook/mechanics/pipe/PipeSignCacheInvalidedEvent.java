package com.sk89q.craftbook.mechanics.pipe;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PipeSignCacheInvalidedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Block pistonBlock;

    public PipeSignCacheInvalidedEvent(Block pistonBlock) {
        this.pistonBlock = pistonBlock;
    }

    public Block getPistonBlock() {
        return pistonBlock;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
