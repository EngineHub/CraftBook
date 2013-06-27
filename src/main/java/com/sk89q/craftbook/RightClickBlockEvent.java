package com.sk89q.craftbook;

import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * An event that overrides the right click actions, allowing an event to override the block that was right clicked.
 */
public class RightClickBlockEvent extends PlayerInteractEvent {

    protected final Block clicked;

    PlayerInteractEvent event;

    public RightClickBlockEvent(PlayerInteractEvent event, Block block) {

        super(event.getPlayer(), event.getAction(), event.getItem(), event.getClickedBlock(), event.getBlockFace());
        if(block == null)
            clicked = event.getClickedBlock();
        else
            clicked = block;

        this.event = event;
    }

    @Override
    public Block getClickedBlock() {

        return clicked;
    }

    @Override
    public boolean isCancelled() {

        return event.isCancelled();
    }

    @Override
    public void setCancelled(boolean stuff) {

        event.setCancelled(stuff);
    }
}