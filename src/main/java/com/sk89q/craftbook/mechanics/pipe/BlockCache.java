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
import org.bukkit.event.block.*;

import java.util.function.Consumer;
import java.util.logging.Level;

public class BlockCache {

    private static final BlockFace[] DIRECT_FACES = new BlockFace[] {
      BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST
    };

    private final BlockCacheRegistry registry;

    private final Long2ObjectMap<ChunkTicket> chunkTicketByCompactId;
    private final Long2IntMap cachedBlockByCompactId;
    private final Long2ObjectMap<PipeSign> pipeSignByPistonCompactId;

    private int cacheLoadCounter = 0;

    public BlockCache(BlockCacheRegistry registry) {
        this.registry = registry;

        this.chunkTicketByCompactId = new Long2ObjectOpenHashMap<>();
        this.cachedBlockByCompactId = new Long2IntOpenHashMap();
        this.cachedBlockByCompactId.defaultReturnValue(CachedBlock.NULL_SENTINEL);
        this.pipeSignByPistonCompactId = new Long2ObjectOpenHashMap<>();
    }

    public void removeExpiredChunkTickets(boolean all) {
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
        this.cachedBlockByCompactId.clear();
        this.pipeSignByPistonCompactId.clear();
        removeExpiredChunkTickets(true);
    }

    public void resetCacheLoadCounter() {
        cacheLoadCounter = 0;
    }

    public int getCacheLoadCounter() {
        return cacheLoadCounter;
    }

    public void invalidateCache(Block block) {
        cachedBlockByCompactId.remove(CompactId.computeWorldlessBlockId(block));

        // Signs are cached by their corresponding piston's position, so the following
        // tries to resolve that mounted-on block to then invalidate the pipe-sign.

        BlockData data = block.getBlockData();

        if (data instanceof org.bukkit.block.data.type.Sign) {
            // Since, unfortunately, pipe-signs are accepted above and below, we have to invalidate both possibilities
            invalidateSignBlock(block, BlockFace.UP);
            invalidateSignBlock(block, BlockFace.DOWN);
            return;
        }

        if (data instanceof WallSign wallSign)
            invalidateSignBlock(block, wallSign.getFacing().getOppositeFace());
    }

    private void invalidateSignBlock(Block signBlock, BlockFace mountingFace) {
        Block pistonBlock = signBlock.getRelative(mountingFace);

        if (pipeSignByPistonCompactId.remove(CompactId.computeWorldlessBlockId(pistonBlock)) != null)
            Bukkit.getPluginManager().callEvent(new PipeSignCacheInvalidedEvent(pistonBlock));
    }

    public int getCachedBlock(Block block) throws LoadingChunkException {
        var compactId = CompactId.computeWorldlessBlockId(block);
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
        long pistonCompactId = CompactId.computeWorldlessBlockId(pistonBlock);

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

        var compactChunkId = CompactId.computeWorldlessChunkId(chunkX, chunkZ);

        if (world.isChunkLoaded(chunkX, chunkZ)) {
            addOrTouchChunkTicket(block, compactChunkId);
            return;
        }

        if (registry.getChunkAtAsync != null) {
            try {
                registry.getChunkAtAsync.invoke(world, chunkX, chunkZ, true, (Consumer<Chunk>) chunk -> addOrTouchChunkTicket(block, compactChunkId));
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

        if (existingTicket != null && registry.getContinuedChunkTicketDuration() > 0) {
            existingTicket.touch(registry.getContinuedChunkTicketDuration());
            return;
        }

        if (registry.getInitialChunkTicketDuration() <= 0)
            return;

        var chunk = block.getChunk();

        chunkTicketByCompactId.put(compactChunkId, new ChunkTicket(chunk, registry.getInitialChunkTicketDuration()));

        if (!chunk.addPluginChunkTicket(CraftBookPlugin.inst()))
            CraftBookPlugin.logger().log(Level.WARNING, "Could not add plugin-ticket to chunk at " + chunk.getX() + " " + chunk.getZ());
    }
}
