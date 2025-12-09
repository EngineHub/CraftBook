package com.sk89q.craftbook.mechanics.pipe;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.bukkit.*;
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
import java.util.function.Consumer;
import java.util.logging.Level;

public class BlockCache implements Listener {

    public static final int DEFAULT_CHUNK_TICKET_DURATION = 60 * 5;

    private static final MethodHandle getChunkAtAsync = findGetChunkAtAsync();

    private final Long2ObjectMap<ChunkTicket> chunkTicketByCompactId;
    private final Consumer<Block> externalInvalidationHandler;
    private final Long2IntMap cachedBlockByCompactId;
    private final BukkitTask chunkTicketTask;

    private int cacheLoadCounter = 0;
    private int chunkTicketDuration = DEFAULT_CHUNK_TICKET_DURATION;

    static {
        CachedBlock.setupPresetTable();
    }

    public BlockCache(Consumer<Block> externalInvalidationHandler) {
        this.chunkTicketByCompactId = new Long2ObjectOpenHashMap<>();
        this.externalInvalidationHandler = externalInvalidationHandler;
        this.cachedBlockByCompactId = new Long2IntOpenHashMap();
        this.cachedBlockByCompactId.defaultReturnValue(CachedBlock.NULL_SENTINEL);
        this.chunkTicketTask = Bukkit.getScheduler().runTaskTimer(CraftBookPlugin.inst(), () -> removeExpiredChunkTickets(false), 0, 20);

        Bukkit.getServer().getPluginManager().registerEvents(this, CraftBookPlugin.inst());

        if (getChunkAtAsync == null)
            CraftBookPlugin.logger().log(Level.WARNING, "[Pipes] Could not find API to load chunks asynchronously; use Paper to experience better performance.");
    }

    public void setChunkTicketDuration(int chunkTicketDuration) {
        if (chunkTicketDuration <= 0) {
            this.chunkTicketDuration = DEFAULT_CHUNK_TICKET_DURATION;
            return;
        }

        this.chunkTicketDuration = chunkTicketDuration;
    }

    private void removeExpiredChunkTickets(boolean all) {
        var now = System.currentTimeMillis();

        for (var iterator = chunkTicketByCompactId.values().iterator(); iterator.hasNext(); ) {
            var chunkTicket = iterator.next();

            if (all || (now - chunkTicket.getLastUse()) / 1000 >= chunkTicketDuration) {
                iterator.remove();

                if (!chunkTicket.chunk.removePluginChunkTicket(CraftBookPlugin.inst()))
                    CraftBookPlugin.logger().log(Level.WARNING, "Could not remove plugin-ticket from chunk at " + chunkTicket.chunk.getX() + " " + chunkTicket.chunk.getZ());
            }
        }
    }

    public void disable() {
        HandlerList.unregisterAll(this);
        this.cachedBlockByCompactId.clear();
        this.chunkTicketTask.cancel();
        removeExpiredChunkTickets(true);
    }

    public void resetCacheLoadCounter() {
        cacheLoadCounter = 0;
    }

    public int getCacheLoadCounter() {
        return cacheLoadCounter;
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

    public int getCachedBlock(Block block) throws LoadingChunkException {
        var compactId = CompactId.computeWorldfulBlockId(block);
        var cachedBlock = cachedBlockByCompactId.get(compactId);

        if (cachedBlock != CachedBlock.NULL_SENTINEL)
            return cachedBlock;

        handleChunkLoading(block);

        cachedBlock = CachedBlock.fromBlock(block);

        // Do not cache this intermediate state - it can trip the whole system up.
        // Let's simply get the real state from the world until it finalized.
        if (!CachedBlock.isMaterial(cachedBlock, Material.MOVING_PISTON)) {
            cachedBlockByCompactId.put(compactId, cachedBlock);
            ++cacheLoadCounter;
        }

        return cachedBlock;
    }

    private void handleChunkLoading(Block block) throws LoadingChunkException {
        int chunkX = block.getX() >> 4;
        int chunkZ = block.getZ() >> 4;
        World world = block.getWorld();

        var compactChunkId = CompactId.computeWorldfulChunkId(world, chunkX, chunkZ);

        if (world.isChunkLoaded(chunkX, chunkZ)) {
            var chunkTicket = chunkTicketByCompactId.get(compactChunkId);

            if (chunkTicket != null)
                chunkTicket.touch();

            return;
        }

        if (getChunkAtAsync != null) {
            try {
                getChunkAtAsync.invoke(world, chunkX, chunkZ, true, (Consumer<Chunk>) chunk -> addChunkTicket(chunk, compactChunkId));
            } catch (Throwable e) {
                CraftBookPlugin.logger().log(Level.SEVERE, "An error occurred while trying to load the chunk at " + chunkX + "," + chunkZ + " asynchronously ", e);
                return;
            }
        } else {
            addChunkTicket(world.getChunkAt(chunkX, chunkZ, true), compactChunkId);
        }

        // Stop walking the pipe despite loading sync also, as to not completely starve the tick-loop
        throw new LoadingChunkException();
    }

    private void addChunkTicket(Chunk chunk, long compactChunkId) {
        // The request to load the chunk could've been called twice if loading took longer
        // than the remaining tick-time since encountering this pipe-block (if async).
        // Ensure to not try to register the ticket twice, which would print the warning below.
        if (chunkTicketByCompactId.containsKey(compactChunkId))
            return;

        chunkTicketByCompactId.put(compactChunkId, new ChunkTicket(chunk));

        if (!chunk.addPluginChunkTicket(CraftBookPlugin.inst()))
            CraftBookPlugin.logger().log(Level.WARNING, "Could not add plugin-ticket to chunk at " + chunk.getX() + " " + chunk.getZ());
    }

    private static MethodHandle findGetChunkAtAsync() {
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
