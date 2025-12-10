package com.sk89q.craftbook.mechanics.pipe;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PipeSignCacheCreatedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Block pistonBlock;
    private final Sign pipeSign;
    private final String[] lines;

    public PipeSignCacheCreatedEvent(Block pistonBlock, Sign pipeSign, String[] lines) {
        this.pistonBlock = pistonBlock;
        this.pipeSign = pipeSign;
        this.lines = lines;
    }

    public String getLine(int index) {
        if (index < 0 || index > 3)
            return "";

        return lines[index];
    }

    public Sign getPipeSign() {
        return pipeSign;
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
