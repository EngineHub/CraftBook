package com.sk89q.craftbook.mechanics.pipe;

import it.unimi.dsi.fastutil.longs.LongSet;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

public interface PipesApi {

  /**
   * A correct and efficient way to walk pipes which loads chunks/blocks incrementally, keeping
   * them retained if applicable, and honours limits as configured.
   *
   * @param firstBlock         The very first block of the pipe from which to start enumerating outwards.
   * @param visitedBlocks      Pre-allocated set to store visited block-ids in; provide null to create it internally.
   * @param enumerationHandler Handler called at each step of the way.
   * @return When receiving {@link EnumerationResult#EXCEEDED_CACHE_LOAD_LIMIT} or {@link EnumerationResult#NEEDS_CHUNK_LOADING},
   *         simply try again next tick, as to disperse the resource-intensive act of warming up the cache over multiple ticks.
   */
  EnumerationResult enumeratePipeBlocks(Block firstBlock, @Nullable LongSet visitedBlocks, PipeEnumerationHandler enumerationHandler);

  /**
   * The currently configured maximum number of tubes (glass) after which the pipe-block enumerator will
   * stop automatically with a result of {@link EnumerationResult#EXCEEDED_TUBE_COUNT_LIMIT}.
   * @return Limit, or a negative number if unlimited
   */
  int getMaxTubeBlockCount();

  /**
   * The currently configured maximum number of pistons after which the pipe-block enumerator will
   * stop automatically with a result of {@link EnumerationResult#EXCEEDED_PISTON_COUNT_LIMIT}.
   * @return Limit, or a negative number if unlimited
   */
  int getMaxPistonBlockCount();

  /**
   * The currently configured maximum number of blocks to load into the cache at once after wich the
   * pipe-block enumerator will stop automatically with a result of {@link EnumerationResult#EXCEEDED_CACHE_LOAD_LIMIT}.
   * @return Limit, or a negative number if unlimited
   */
  int getMaxCacheLoadCount();

}
