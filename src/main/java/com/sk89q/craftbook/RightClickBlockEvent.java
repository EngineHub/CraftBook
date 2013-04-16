package com.sk89q.craftbook;

import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * An event that overrides the right click actions, allowing an event to override the block that was right clicked.
 */
public class RightClickBlockEvent extends PlayerInteractEvent {

    @SuppressWarnings("unused")
    private static final long serialVersionUID = 1031838877588760298L;

    protected final Block clicked;

    PlayerInteractEvent event;

    public RightClickBlockEvent(PlayerInteractEvent event, Block block) {

        super(event.getPlayer(), event.getAction(), event.getItem(), event.getClickedBlock(), event.getBlockFace());
        if(block == null)
            clicked = event.getClickedBlock();
        else
            clicked = block;
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