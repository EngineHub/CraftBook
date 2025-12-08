package com.sk89q.craftbook.mechanics.pipe;

import org.bukkit.Chunk;

public class ChunkTicket {

  public final Chunk chunk;
  private long lastUse;

  public ChunkTicket(Chunk chunk) {
    this.chunk = chunk;
    this.lastUse = System.currentTimeMillis();
  }

  public void touch() {
    this.lastUse = System.currentTimeMillis();
  }

  public long getLastUse() {
    return lastUse;
  }
}
