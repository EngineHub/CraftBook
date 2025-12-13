package com.sk89q.craftbook.mechanics.pipe;

import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;

/**
 * Provides a means for other plugins to invalidate a given block's cache if they
 * have either modified it programmatically or if they themselves seek to lazily
 * refresh their own internal layered-on cache without needlessly loading the chunk
 * now. As an example for the latter case, let the block be a sign, then this event will
 * invalidate the corresponding entries and cause a {@link PipeSignCacheCreatedEvent}
 * at whatever time the sign is used once again.
 */
public class InvalidateCachedBlockEvent extends BlockEvent {

  private static final HandlerList handlers = new HandlerList();

  public InvalidateCachedBlockEvent(Block block) {
    super(block);
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }
}
