package com.sk89q.craftbook.mechanics.pipe;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.function.Consumer;

public class BlockCache implements Listener {

  private final Consumer<Block> externalInvalidationHandler;
  private final Long2IntMap cachedBlockByCompactId;

  public BlockCache(Consumer<Block> externalInvalidationHandler) {
    this.externalInvalidationHandler = externalInvalidationHandler;
    this.cachedBlockByCompactId = new Long2IntOpenHashMap();
    this.cachedBlockByCompactId.defaultReturnValue(CachedBlock.NULL_SENTINEL);

    Bukkit.getServer().getPluginManager().registerEvents(this, CraftBookPlugin.inst());
  }

  public void clear() {
    this.cachedBlockByCompactId.clear();
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockBreak(BlockBreakEvent event) {
    invalidateCache(event.getBlock());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockPlace(BlockPlaceEvent event) {
    invalidateCache(event.getBlock());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPistonExtend(BlockPistonExtendEvent event) {
    for (var block : event.getBlocks()) {
      invalidateCache(block);
      invalidateCache(block.getRelative(event.getDirection()));
    }
    invalidateCache(event.getBlock());
    invalidateCache(event.getBlock().getRelative(event.getDirection()));
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPistonRetract(BlockPistonRetractEvent event) {
    for (var block : event.getBlocks()) {
      invalidateCache(block);
      invalidateCache(block.getRelative(event.getDirection()));
    }
    invalidateCache(event.getBlock());
    invalidateCache(event.getBlock().getRelative(event.getDirection()));
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onEntityExplode(EntityExplodeEvent event) {
    for (var block : event.blockList())
      invalidateCache(block);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockExplode(BlockExplodeEvent event) {
    for (var block : event.blockList())
      invalidateCache(block);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onSignChange(SignChangeEvent event) {
    invalidateCache(event.getBlock());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockPhysics(BlockPhysicsEvent event) {
    var block = event.getBlock();
    var type = block.getType();

    Block supportingBlock;

    if (Tag.STANDING_SIGNS.isTagged(type))
      supportingBlock = block.getRelative(BlockFace.DOWN);
    else if (Tag.WALL_SIGNS.isTagged(type))
      supportingBlock = block.getRelative(((WallSign) block.getBlockData()).getFacing().getOppositeFace());
    else
      return;

    if (supportingBlock.getType() == Material.AIR)
      invalidateCache(block);
  }

  private void invalidateCache(Block block) {
    cachedBlockByCompactId.remove(CompactId.computeWorldfulBlockId(block));
    externalInvalidationHandler.accept(block);
  }

  public int getCachedBlock(Block block) {
    var compactId = CompactId.computeWorldfulBlockId(block);
    var cachedBlock = cachedBlockByCompactId.get(compactId);

    if (cachedBlock != CachedBlock.NULL_SENTINEL)
      return cachedBlock;

    cachedBlock = CachedBlock.fromBlock(block);

    // Do not cache this intermediate state - it can trip the whole system up.
    // Let's simply get the real state from the world until it finalized.
    if (!CachedBlock.isMaterial(cachedBlock, Material.MOVING_PISTON))
      cachedBlockByCompactId.put(compactId, cachedBlock);

    return cachedBlock;
  }
}
