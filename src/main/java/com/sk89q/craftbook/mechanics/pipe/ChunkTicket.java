package com.sk89q.craftbook.mechanics.pipe;

import org.bukkit.Chunk;

public class ChunkTicket {

    public final Chunk chunk;
    private long expiryStamp;

    public ChunkTicket(Chunk chunk, long initialDuration) {
        this.chunk = chunk;
        this.expiryStamp = System.currentTimeMillis() + initialDuration;
    }

    public void touch(long continuedDuration) {
        this.expiryStamp = System.currentTimeMillis() + continuedDuration;
    }

    public long getExpiryStamp() {
        return expiryStamp;
    }
}
