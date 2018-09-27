package com.sk89q.craftbook.util.events;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;

public class SelfTriggerUnregisterEvent extends BlockEvent implements Cancellable {

    private UnregisterReason reason;

    public SelfTriggerUnregisterEvent (Block theBlock, UnregisterReason reason) {
        super(theBlock);

        this.reason = reason;
    }

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public UnregisterReason getReason() {
        return reason;
    }

    @Override
    public boolean isCancelled () {
        return cancelled;
    }

    @Override
    public void setCancelled (boolean arg0) {
        cancelled = arg0;
    }

    private boolean cancelled;

    public enum UnregisterReason {

        UNLOAD,BREAK,ERROR,NOT_HANDLED,UNKNOWN;
    }
}