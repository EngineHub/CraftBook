package com.sk89q.craftbook.mechanics.pipe;

import org.bukkit.block.Block;

@FunctionalInterface
public interface PipeEnumerationHandler {

    EnumerationHandleResult handle(Block pipeBlock, int cachedPipeBlock) throws LoadingChunkException;

}
