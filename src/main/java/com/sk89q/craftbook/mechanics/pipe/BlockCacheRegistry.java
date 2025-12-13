package com.sk89q.craftbook.mechanics.pipe;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.scheduler.BukkitTask;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;

public class BlockCacheRegistry implements Listener {

  public static final int DEFAULT_INITIAL_CHUNK_TICKET_DURATION = 60;
  public static final int DEFAULT_CONTINUED_CHUNK_TICKET_DURATION = 60 * 5;

  static {
    CachedBlock.setupPresetTable();
  }

  public final MethodHandle getChunkAtAsync;
  private final BukkitTask chunkTicketTask;
  private final Map<UUID, BlockCache> blockCacheByWorldUid;

  private int initialChunkTicketDuration = DEFAULT_INITIAL_CHUNK_TICKET_DURATION;
  private int continuedChunkTicketDuration = DEFAULT_CONTINUED_CHUNK_TICKET_DURATION;

  public BlockCacheRegistry() {
    this.getChunkAtAsync = findGetChunkAtAsync();
    this.blockCacheByWorldUid = new HashMap<>();

    this.chunkTicketTask = Bukkit.getScheduler().runTaskTimer(CraftBookPlugin.inst(), () -> {
      for (var cache : blockCacheByWorldUid.values())
        cache.removeExpiredChunkTickets(false);
    }, 0, 20);

    if (getChunkAtAsync == null)
      CraftBookPlugin.logger().log(Level.WARNING, "[Pipes] Could not find API to load chunks asynchronously; use Paper to experience better performance.");

    Bukkit.getServer().getPluginManager().registerEvents(this, CraftBookPlugin.inst());
  }

  public BlockCache getBlockCache(World world) {
    return blockCacheByWorldUid.computeIfAbsent(world.getUID(), k -> new BlockCache(this));
  }

  public void disable() {
    HandlerList.unregisterAll(this);
    this.chunkTicketTask.cancel();

    for (var cache : blockCacheByWorldUid.values())
      cache.disable();

    blockCacheByWorldUid.clear();
  }

  public void setInitialChunkTicketDuration(int duration) {
    this.initialChunkTicketDuration = duration;
  }

  public void setContinuedChunkTicketDuration(int duration) {
    this.continuedChunkTicketDuration = duration;
  }

  public int getInitialChunkTicketDuration() {
    return initialChunkTicketDuration;
  }

  public int getContinuedChunkTicketDuration() {
    return continuedChunkTicketDuration;
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

  @EventHandler
  public void onCacheInvalidation(InvalidateCachedBlockEvent event) {
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
    var blockCache = blockCacheByWorldUid.get(block.getWorld().getUID());

    if (blockCache != null)
      blockCache.invalidateCache(block);
  }

  private MethodHandle findGetChunkAtAsync() {
    try {
      return MethodHandles.lookup().findVirtual(
        World.class,
        "getChunkAtAsync",
        MethodType.methodType(void.class, int.class, int.class, boolean.class, Consumer.class)
      );
    } catch (NoSuchMethodException | IllegalAccessException e) {
      return null;
    }
  }
}
