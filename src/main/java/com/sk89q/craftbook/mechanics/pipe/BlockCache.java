package com.sk89q.craftbook.mechanics.pipe;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
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

    private static final BlockFace[] DIRECT_FACES = new BlockFace[] {
      BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST
    };

    public static final int DEFAULT_INITIAL_CHUNK_TICKET_DURATION = 60;
    public static final int DEFAULT_CONTINUED_CHUNK_TICKET_DURATION = 60 * 5;

    private static final MethodHandle getChunkAtAsync = findGetChunkAtAsync();

    private final Long2ObjectMap<ChunkTicket> chunkTicketByCompactId;
    private final Long2IntMap cachedBlockByCompactId;
    private final Long2ObjectMap<PipeSign> pipeSignByPistonCompactId;
    private final BukkitTask chunkTicketTask;

    private int cacheLoadCounter = 0;
    private int initialChunkTicketDuration = DEFAULT_INITIAL_CHUNK_TICKET_DURATION;
    private int continuedChunkTicketDuration = DEFAULT_CONTINUED_CHUNK_TICKET_DURATION;

    static {
        CachedBlock.setupPresetTable();
    }

    public BlockCache() {
        this.chunkTicketByCompactId = new Long2ObjectOpenHashMap<>();
        this.cachedBlockByCompactId = new Long2IntOpenHashMap();
        this.cachedBlockByCompactId.defaultReturnValue(CachedBlock.NULL_SENTINEL);
        this.pipeSignByPistonCompactId = new Long2ObjectOpenHashMap<>();
        this.chunkTicketTask = Bukkit.getScheduler().runTaskTimer(CraftBookPlugin.inst(), () -> removeExpiredChunkTickets(false), 0, 20);

        Bukkit.getServer().getPluginManager().registerEvents(this, CraftBookPlugin.inst());

        if (getChunkAtAsync == null)
            CraftBookPlugin.logger().log(Level.WARNING, "[Pipes] Could not find API to load chunks asynchronously; use Paper to experience better performance.");
    }

    public void setInitialChunkTicketDuration(int duration) {
        this.initialChunkTicketDuration = duration;
    }

    public void setContinuedChunkTicketDuration(int duration) {
        this.continuedChunkTicketDuration = duration;
    }

    private void removeExpiredChunkTickets(boolean all) {
        var now = System.currentTimeMillis();

        for (var iterator = chunkTicketByCompactId.values().iterator(); iterator.hasNext(); ) {
            var chunkTicket = iterator.next();

            if (all || (now >= chunkTicket.getExpiryStamp())) {
                iterator.remove();

                if (!chunkTicket.chunk.removePluginChunkTicket(CraftBookPlugin.inst()))
                    CraftBookPlugin.logger().log(Level.WARNING, "Could not remove plugin-ticket from chunk at " + chunkTicket.chunk.getX() + " " + chunkTicket.chunk.getZ());
            }
        }
    }

    public void disable() {
        HandlerList.unregisterAll(this);
        this.cachedBlockByCompactId.clear();
        this.pipeSignByPistonCompactId.clear();
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

        BlockData blockData = block.getBlockData();

        if (blockData instanceof org.bukkit.block.data.type.Sign) {
            // Since, unfortunately, pipe-signs are accepted above and below, we have to invalidate both possibilities
            invalidateSignBlock(block, BlockFace.UP);
            invalidateSignBlock(block, BlockFace.DOWN);
            return;
        }

        if (blockData instanceof WallSign wallSign)
            invalidateSignBlock(block, wallSign.getFacing().getOppositeFace());
    }

    private void invalidateSignBlock(Block signBlock, BlockFace mountingFace) {
        Block pistonBlock = signBlock.getRelative(mountingFace);

        if (pipeSignByPistonCompactId.remove(CompactId.computeWorldfulBlockId(pistonBlock)) != null)
            Bukkit.getPluginManager().callEvent(new PipeSignCacheInvalidedEvent(pistonBlock));
    }

    public int getCachedBlock(Block block) throws LoadingChunkException {
        var compactId = CompactId.computeWorldfulBlockId(block);
        var cachedBlock = cachedBlockByCompactId.get(compactId);

        if (cachedBlock != CachedBlock.NULL_SENTINEL) {
            if (CachedBlock.shouldContinueToRetainChunks(cachedBlock))
                ensureChunkIsLoaded(block);

            return cachedBlock;
        }

        ensureChunkIsLoaded(block);

        cachedBlock = CachedBlock.fromBlock(block);

        // Do not cache this intermediate state - it can trip the whole system up.
        // Let's simply get the real state from the world until it finalized.
        if (!CachedBlock.isMaterial(cachedBlock, Material.MOVING_PISTON)) {
            cachedBlockByCompactId.put(compactId, cachedBlock);
            ++cacheLoadCounter;
        }

        return cachedBlock;
    }

    public PipeSign getSignOnPiston(Block pistonBlock, int cachedPistonBlock) throws LoadingChunkException {
        long pistonCompactId = CompactId.computeWorldfulBlockId(pistonBlock);

        PipeSign cachedSign = pipeSignByPistonCompactId.get(pistonCompactId);

        if (cachedSign != null)
            return cachedSign;

        BlockFace facing = CachedBlock.getFacing(cachedPistonBlock);

        for (BlockFace face : DIRECT_FACES) {
            if (face == facing)
                continue;

            Block faceBlock = pistonBlock.getRelative(face);
            int cachedFaceBlock = getCachedBlock(faceBlock);

            if (CachedBlock.isStandingSign(cachedFaceBlock)) {
                // Standing-signs may only be on or under the piston
                if (face != BlockFace.UP && face != BlockFace.DOWN)
                    continue;
            } else if (CachedBlock.isWallSign(cachedFaceBlock)) {
                // Wall-signs may only be attached N/E/S/W on the piston
                if (face == BlockFace.UP || face == BlockFace.DOWN)
                    continue;

                // The sign has to be mounted on this piston, not on an adjacent one
                if (CachedBlock.getFacing(cachedFaceBlock) != face)
                    continue;
            } else {
                // Not a sign at all, do not needlessly try to get its state
                continue;
            }

            if (!(faceBlock.getState() instanceof Sign sign))
                continue;

            String[] lines = sign.getLines();

            if (!lines[1].equalsIgnoreCase("[Pipe]"))
                continue;

            cachedSign = PipeSign.fromSign(sign, lines);
            Bukkit.getPluginManager().callEvent(new PipeSignCacheCreatedEvent(pistonBlock, sign, lines));
            break;
        }

        if (cachedSign == null)
            cachedSign = PipeSign.NO_SIGN;

        pipeSignByPistonCompactId.put(pistonCompactId, cachedSign);

        return cachedSign;
    }

    private void ensureChunkIsLoaded(Block block) throws LoadingChunkException {
        int chunkX = block.getX() >> 4;
        int chunkZ = block.getZ() >> 4;
        World world = block.getWorld();

        var compactChunkId = CompactId.computeWorldfulChunkId(world, chunkX, chunkZ);

        if (world.isChunkLoaded(chunkX, chunkZ)) {
            addOrTouchChunkTicket(block, compactChunkId);
            return;
        }

        if (getChunkAtAsync != null) {
            try {
                getChunkAtAsync.invoke(world, chunkX, chunkZ, true, (Consumer<Chunk>) chunk -> addOrTouchChunkTicket(block, compactChunkId));
            } catch (Throwable e) {
                CraftBookPlugin.logger().log(Level.SEVERE, "An error occurred while trying to load the chunk at " + chunkX + "," + chunkZ + " asynchronously ", e);
                return;
            }
        } else {
            addOrTouchChunkTicket(block, compactChunkId);
        }

        // Stop walking the pipe despite loading sync also, as to not completely starve the tick-loop
        throw new LoadingChunkException();
    }

    private void addOrTouchChunkTicket(Block block, long compactChunkId) {
        var existingTicket = chunkTicketByCompactId.get(compactChunkId);

        if (existingTicket != null && continuedChunkTicketDuration > 0) {
            existingTicket.touch(continuedChunkTicketDuration);
            return;
        }

        if (initialChunkTicketDuration <= 0)
            return;

        var chunk = block.getChunk();

        chunkTicketByCompactId.put(compactChunkId, new ChunkTicket(chunk, initialChunkTicketDuration));

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
