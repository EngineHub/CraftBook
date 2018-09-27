package com.sk89q.craftbook.util.events;

import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;

public class SelfTriggerPingEvent extends BlockEvent {

    public SelfTriggerPingEvent(Block theBlock) {
        super(theBlock);
    }

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}