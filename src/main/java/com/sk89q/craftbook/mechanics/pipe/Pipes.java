package com.sk89q.craftbook.mechanics.pipe;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.core.LanguageManager;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.InventoryUtil;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.ProtectionUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.VerifyUtil;
import com.sk89q.craftbook.util.events.SourcedBlockRedstoneEvent;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Piston;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class Pipes extends AbstractCraftBookMechanic {

    private int currentTubeBlockCounter;
    private int currentPistonBlockCounter;

    private final BlockCache blockCache;
    private final Long2ObjectMap<PipeSign> pipeSignByPistonCompactId;

    public Pipes() {
        this.blockCache = new BlockCache(this::invalidateCache);
        this.pipeSignByPistonCompactId = new Long2ObjectOpenHashMap<>();

        Bukkit.getServer().getPluginManager().registerEvents(blockCache, CraftBookPlugin.inst());
    }

    @Override
    public void disable() {
        super.disable();

        blockCache.disable();
        pipeSignByPistonCompactId.clear();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(!event.getLine(1).equalsIgnoreCase("[pipe]")) return;

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if(!player.hasPermission("craftbook.circuits.pipes")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.create-permission");
            SignUtil.cancelSign(event);
            return;
        }

        if(ProtectionUtil.shouldUseProtection()) {
            Block pistonBlock = null;

            if (SignUtil.isWallSign(event.getBlock())) {
                pistonBlock = SignUtil.getBackBlock(event.getBlock());
            } else if (SignUtil.isStandingSign(event.getBlock())) {
                if (isPiston(event.getBlock().getRelative(BlockFace.DOWN))) {
                    pistonBlock = event.getBlock().getRelative(BlockFace.DOWN);
                } else if (isPiston(event.getBlock().getRelative(BlockFace.UP))) {
                    pistonBlock = event.getBlock().getRelative(BlockFace.UP);
                }
            }
            if(pistonBlock != null && isPiston(pistonBlock)) {
                Piston pis = (Piston) pistonBlock.getBlockData();
                Block off = pistonBlock.getRelative(pis.getFacing());
                if (InventoryUtil.doesBlockHaveInventory(off)) {
                    if (!ProtectionUtil.canAccessInventory(event.getPlayer(), off)) {
                        if (CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                            player.printError("area.use-permission");
                        SignUtil.cancelSign(event);
                        return;
                    }
                }
            } else {
                player.printError("circuits.pipes.pipe-not-found");
                SignUtil.cancelSign(event);
                return;
            }
        }

        event.setLine(1, "[Pipe]");
        player.print("circuits.pipes.create");
    }

    private static boolean isPiston(Block block) {
        Material type = block.getType();
        return type == Material.PISTON || type == Material.STICKY_PISTON;
    }

    private void invalidateSignBlock(Block signBlock, BlockFace mountingFace) {
        Block pistonBlock = signBlock.getRelative(mountingFace);

        if (pipeSignByPistonCompactId.remove(CompactId.computeWorldfulBlockId(pistonBlock)) != null)
          Bukkit.getPluginManager().callEvent(new PipeSignCacheInvalidedEvent(pistonBlock));
    }

    private void invalidateCache(Block block) {
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

    private PipeSign getSignOnPiston(Block pistonBlock, int cachedPistonBlock) throws LoadingChunkException {
        long pistonCompactId = CompactId.computeWorldfulBlockId(pistonBlock);

        PipeSign cachedSign = pipeSignByPistonCompactId.get(pistonCompactId);

        if (cachedSign != null)
            return cachedSign;

        BlockFace facing = CachedBlock.getPistonFacing(cachedPistonBlock);

        for (BlockFace face : LocationUtil.getDirectFaces()) {
            if (face == facing)
                continue;

            Block faceBlock = pistonBlock.getRelative(face);
            int cachedFaceBlock = blockCache.getCachedBlock(faceBlock);

            boolean isStandingSign = CachedBlock.isStandingSign(cachedFaceBlock);
            boolean isWallSign = CachedBlock.isWallSign(cachedFaceBlock);

            if (isWallSign && (face == BlockFace.UP || face == BlockFace.DOWN))
                continue;

            if (isStandingSign && face != BlockFace.UP && face != BlockFace.DOWN)
                continue;

            if (isWallSign && !SignUtil.getBackBlock(faceBlock).getLocation().equals(pistonBlock.getLocation()))
                continue;

            if (!(faceBlock.getState() instanceof Sign sign))
                continue;

            if (!sign.getLine(1).equalsIgnoreCase("[Pipe]"))
                continue;

            cachedSign = PipeSign.fromSign(sign);
            Bukkit.getPluginManager().callEvent(new PipeSignCacheCreatedEvent(pistonBlock, sign));
            break;
        }

        if (cachedSign == null)
            cachedSign = PipeSign.NO_SIGN;

        pipeSignByPistonCompactId.put(pistonCompactId, cachedSign);

        return cachedSign;
    }

    private EnumerationResult locateExitNodesForItems(Block inputPistonBlock, LongSet visitedBlocks, List<ItemStack> itemsInPipe) throws LoadingChunkException {
        return enumeratePipeBlocks(inputPistonBlock, visitedBlocks, (pipeBlock, cachedPipeBlock) -> {
            if (itemsInPipe.isEmpty())
                return EnumerationHandleResult.DONE;

            if (CachedBlock.isTube(cachedPipeBlock)) {
                ++currentTubeBlockCounter;

                if (maxTubeBlockCount >= 0 && currentTubeBlockCounter >= maxTubeBlockCount)
                    return EnumerationHandleResult.DONE;

                return EnumerationHandleResult.CONTINUE;
            }

            if (!CachedBlock.isMaterial(cachedPipeBlock, Material.PISTON))
                return EnumerationHandleResult.CONTINUE;

            ++currentPistonBlockCounter;

            if (maxPistonBlockCount >= 0 && currentPistonBlockCounter >= maxPistonBlockCount)
                return EnumerationHandleResult.DONE;

            PipeSign sign = getSignOnPiston(pipeBlock, cachedPipeBlock);

            List<ItemStack> filteredPipeItems = new ArrayList<>(VerifyUtil.withoutNulls(ItemUtil.filterItems(itemsInPipe, sign.includeFilters, sign.excludeFilters)));

            PipeFilterEvent filterEvent = new PipeFilterEvent(pipeBlock, itemsInPipe, sign.includeFilters, sign.excludeFilters, filteredPipeItems);
            Bukkit.getPluginManager().callEvent(filterEvent);

            filteredPipeItems = filterEvent.getFilteredItems();

            if (filteredPipeItems.isEmpty())
                return EnumerationHandleResult.CONTINUE;

            List<ItemStack> leftovers = new ArrayList<>();

            Block containerBlock = pipeBlock.getRelative(CachedBlock.getPistonFacing(cachedPipeBlock));
            int cachedContainerBlock = blockCache.getCachedBlock(containerBlock);

            PipePutEvent putEvent = new PipePutEvent(pipeBlock, new ArrayList<>(filteredPipeItems), containerBlock, cachedContainerBlock);
            Bukkit.getPluginManager().callEvent(putEvent);

            if (putEvent.isCancelled())
                return EnumerationHandleResult.CONTINUE;

            List<ItemStack> itemsToPut = putEvent.getItems();

            if (
              CachedBlock.hasHandledOutputInventory(cachedContainerBlock)
                && containerBlock.getState() instanceof InventoryHolder holder
            ) {
                leftovers.addAll(InventoryUtil.addItemsToInventory(holder, itemsToPut.toArray(new ItemStack[0])));
            }
            else if (CachedBlock.isMaterial(cachedContainerBlock, Material.JUKEBOX)) {
                Jukebox jukebox = (Jukebox) containerBlock.getState();

                for (ItemStack item : itemsToPut) {
                    if (jukebox.hasRecord() || !item.getType().isRecord()) {
                        leftovers.add(item);
                        continue;
                    }

                    jukebox.setRecord(item);
                    jukebox.update();
                }
            }
            else {
                leftovers.addAll(itemsToPut);
            }

            itemsInPipe.removeAll(itemsToPut);
            itemsInPipe.addAll(leftovers);

            return itemsInPipe.isEmpty() ? EnumerationHandleResult.DONE : EnumerationHandleResult.CONTINUE;
        });
    }

    /**
     * A publicly available, correct and efficient way to walk pipes
     * @param firstBlock The very first block of the pipe from which to start enumerating outwards.
     * @param visitedBlocks Pre-allocated set to store visited block-ids in; provide null to create it internally.
     * @param enumerationHandler Handler called at each step of the way.
     * @throws LoadingChunkException Thrown if a chunk was absent and is now loading asynchronously; try again next tick.
     */
    public EnumerationResult enumeratePipeBlocks(Block firstBlock, @Nullable LongSet visitedBlocks, PipeEnumerationHandler enumerationHandler) throws LoadingChunkException {
        if (!Bukkit.isPrimaryThread())
            throw new IllegalStateException("This method must be called on the main server thread");

        if (visitedBlocks == null)
            visitedBlocks = new LongOpenHashSet();

        currentTubeBlockCounter = currentPistonBlockCounter = 0;
        blockCache.resetCacheLoadCounter();

        Deque<Block> searchQueue = new ArrayDeque<>();
        searchQueue.addFirst(firstBlock);

        while (!searchQueue.isEmpty()) {
            Block pipeBlock = searchQueue.poll();
            int cachedPipeBlock = blockCache.getCachedBlock(pipeBlock);

            EnumerationHandleResult handleResult = enumerationHandler.handle(pipeBlock, cachedPipeBlock);

            if (handleResult != EnumerationHandleResult.CONTINUE)
                return EnumerationResult.COMPLETED;

            // While we could check for exceeding the load-counter at countless call-sites, and while there already have been
            // a few cache-lookups prior to enumerating, a hand-full blocks more don't matter in the grand scheme of things.
            if (maxCacheLoadCount >= 0 && blockCache.getCacheLoadCounter() >= maxCacheLoadCount)
                return EnumerationResult.STOPPED_EARLY;

            for (int x = -1; x < 2; x++) {
                for (int y = -1; y < 2; y++) {
                    for (int z = -1; z < 2; z++) {

                        if (!pipesDiagonal) {
                            if (x != 0 && y != 0) continue;
                            if (x != 0 && z != 0) continue;
                            if (y != 0 && z != 0) continue;
                        } else if (pipeInsulator != null) {
                            boolean xIsY = Math.abs(x) == Math.abs(y);
                            boolean xIsZ = Math.abs(x) == Math.abs(z);
                            if (xIsY && xIsZ) {
                                if (CachedBlock.isMaterial(blockCache.getCachedBlock(pipeBlock.getRelative(x, 0, 0)), pipeInsulator)
                                      && CachedBlock.isMaterial(blockCache.getCachedBlock(pipeBlock.getRelative(0, y, 0)), pipeInsulator)
                                      && CachedBlock.isMaterial(blockCache.getCachedBlock(pipeBlock.getRelative(0, 0, z)), pipeInsulator)) {
                                    continue;
                                }
                            } else if (xIsY) {
                                if (CachedBlock.isMaterial(blockCache.getCachedBlock(pipeBlock.getRelative(x, 0, 0)), pipeInsulator)
                                      && CachedBlock.isMaterial(blockCache.getCachedBlock(pipeBlock.getRelative(0, y, 0)), pipeInsulator)) {
                                    continue;
                                }
                            } else if (xIsZ) {
                                if (CachedBlock.isMaterial(blockCache.getCachedBlock(pipeBlock.getRelative(x, 0, 0)), pipeInsulator)
                                      && CachedBlock.isMaterial(blockCache.getCachedBlock(pipeBlock.getRelative(0, 0, z)), pipeInsulator)) {
                                    continue;
                                }
                            } else {
                                if (CachedBlock.isMaterial(blockCache.getCachedBlock(pipeBlock.getRelative(0, y, 0)), pipeInsulator)
                                      && CachedBlock.isMaterial(blockCache.getCachedBlock(pipeBlock.getRelative(0, 0, z)), pipeInsulator)) {
                                    continue;
                                }
                            }
                        }

                        Block enumeratedBlock = pipeBlock.getRelative(x, y, z);
                        int cachedEnumeratedBlock = blockCache.getCachedBlock(enumeratedBlock);

                        if (!CachedBlock.isValidPipeBlock(cachedEnumeratedBlock))
                            continue;

                        if (!visitedBlocks.add(CompactId.computeWorldlessBlockId(enumeratedBlock)))
                            continue;

                        // Ensure that the block we came from is of the same color as the one we're enumerating.
                        if (CachedBlock.doTubeColorsMismatch(cachedPipeBlock, cachedEnumeratedBlock))
                            continue;

                        if (!CachedBlock.isTube(cachedEnumeratedBlock)) {
                            // Pistons are treated with higher priority.
                            if (CachedBlock.isMaterial(cachedEnumeratedBlock, Material.PISTON))
                                searchQueue.addFirst(enumeratedBlock);

                            continue;
                        }

                        if (!CachedBlock.isPane(cachedEnumeratedBlock)) {
                            searchQueue.add(enumeratedBlock);
                            continue;
                        }

                        Block nextEnumeratedBlock = enumeratedBlock.getRelative(x, y, z);
                        int cachedNextEnumeratedBlock = blockCache.getCachedBlock(nextEnumeratedBlock);

                        if (!CachedBlock.isValidPipeBlock(cachedNextEnumeratedBlock))
                            continue;

                        long nextEnumeratedId = CompactId.computeWorldlessBlockId(nextEnumeratedBlock);

                        if (visitedBlocks.contains(nextEnumeratedId))
                            continue;

                        // Ensure that if the block we came from had a color, the one we jumped across to after
                        // passing a pane is also of the same color.
                        if (CachedBlock.doTubeColorsMismatch(cachedPipeBlock, cachedNextEnumeratedBlock))
                            continue;

                        visitedBlocks.add(nextEnumeratedId);
                        searchQueue.add(nextEnumeratedBlock);
                    }
                }
            }
        }

        return EnumerationResult.COMPLETED;
    }

    private EnumerationResult startPipe(Block inputPistonBlock, List<ItemStack> itemsInPipe, boolean wasRequest) {
        PipeSign sign;
        Block containerBlock;
        int cachedContainerBlock;

        try {
            int cachedInputPistonBlock = blockCache.getCachedBlock(inputPistonBlock);

            if (!CachedBlock.isMaterial(cachedInputPistonBlock, Material.STICKY_PISTON))
                return EnumerationResult.COMPLETED;

            sign = getSignOnPiston(inputPistonBlock, cachedInputPistonBlock);

            if (pipeRequireSign && sign == PipeSign.NO_SIGN)
                return EnumerationResult.COMPLETED;

            containerBlock = inputPistonBlock.getRelative(CachedBlock.getPistonFacing(cachedInputPistonBlock));
            cachedContainerBlock = blockCache.getCachedBlock(containerBlock);
        }
        // If the very beginning of the pipe already (partially) is within an unloaded chunk,
        // there's no need to start the process at all.
        catch (LoadingChunkException ignored) {
            return EnumerationResult.STOPPED_EARLY;
        }

        LongSet visitedBlocks = new LongOpenHashSet();
        visitedBlocks.add(CompactId.computeWorldlessBlockId(containerBlock));

        // Suck items from container-block

        InventoryHolder inventoryHolder = null;
        Jukebox jukebox = null;

        if (
          CachedBlock.hasHandledInputInventory(cachedContainerBlock)
            && containerBlock.getState() instanceof InventoryHolder holder
        ) {
            inventoryHolder = holder;
            Inventory blockInventory = inventoryHolder.getInventory();

            if (blockInventory instanceof FurnaceInventory furnaceInventory) {
                ItemStack result = furnaceInventory.getResult();

                if (ItemUtil.isStackValid(result) && ItemUtil.doesItemPassFilters(result, sign.includeFilters, sign.excludeFilters)) {
                    itemsInPipe.add(result);
                    furnaceInventory.setResult(null);
                }
            }

             else if (inventoryHolder instanceof BrewingStand brewingStand) {
                 if (brewingStand.getBrewingTime() <= 0) {
                     BrewerInventory inventory = brewingStand.getInventory();

                     for (int i = 0; i < 3; ++i) {
                         ItemStack item = inventory.getItem(i);

                         if (ItemUtil.isStackValid(item) && ItemUtil.doesItemPassFilters(item, sign.includeFilters, sign.excludeFilters)) {
                             itemsInPipe.add(item);
                             inventory.setItem(i, null);
                         }
                     }
                 }
            }

            else {
                for (int slot = 0; slot < blockInventory.getSize(); ++slot) {
                    ItemStack stack = blockInventory.getItem(slot);

                    if (!ItemUtil.isStackValid(stack))
                        continue;

                    if (!ItemUtil.doesItemPassFilters(stack, sign.includeFilters, sign.excludeFilters))
                        continue;

                    itemsInPipe.add(stack);
                    blockInventory.setItem(slot, null);

                    if (pipeStackPerPull)
                        break;
                }
            }
        }

        else if (CachedBlock.isMaterial(cachedContainerBlock, Material.JUKEBOX)) {
            jukebox = (Jukebox) containerBlock.getState();

            if (jukebox.hasRecord()) {
                itemsInPipe.add(jukebox.getRecord());
                jukebox.setRecord(null);
                jukebox.update();
            }
        }

        PipeSuckEvent suckEvent = new PipeSuckEvent(inputPistonBlock, new ArrayList<>(itemsInPipe), containerBlock, cachedContainerBlock);
        Bukkit.getPluginManager().callEvent(suckEvent);

        itemsInPipe.clear();

        for (ItemStack item : suckEvent.getItems()) {
            if (ItemUtil.isStackValid(item))
                itemsInPipe.add(item);
        }

        // Walk pipe to store as many items as possible

        EnumerationResult enumerationResult = EnumerationResult.COMPLETED;

        if (!suckEvent.isCancelled() && !itemsInPipe.isEmpty()) {
            try {
                enumerationResult = locateExitNodesForItems(inputPistonBlock, visitedBlocks, itemsInPipe);
            }
            // Simply terminate walking the pipe early - but do put the leftovers back
            catch (LoadingChunkException ignored) {
                enumerationResult = EnumerationResult.STOPPED_EARLY;
            }
        }

        // Try to put leftovers back into the block
        List<ItemStack> leftovers = new ArrayList<>();

        if (!itemsInPipe.isEmpty()) {
            if (inventoryHolder != null)
                leftovers.addAll(InventoryUtil.addItemsToInventory(inventoryHolder, itemsInPipe.toArray(new ItemStack[0])));
            else if (jukebox != null) {
                for (ItemStack item : itemsInPipe) {
                    if (jukebox.hasRecord() || !item.getType().isRecord()) {
                        leftovers.add(item);
                        continue;
                    }

                    jukebox.setRecord(item);
                    jukebox.update();
                }
            }
            else
                leftovers.addAll(itemsInPipe);
        }

        // Finish up the pipe and possibly drop leftovers

        PipeFinishEvent finishEvent = new PipeFinishEvent(inputPistonBlock, leftovers, containerBlock, wasRequest);
        Bukkit.getPluginManager().callEvent(finishEvent);

        leftovers = finishEvent.getItems();
        itemsInPipe.clear();

        if (!leftovers.isEmpty()) {
            for (ItemStack item : leftovers) {
                if (!ItemUtil.isStackValid(item)) continue;
                inputPistonBlock.getWorld().dropItemNaturally(inputPistonBlock.getLocation().add(0.5, 0.5, 0.5), item);
            }
        }

        return enumerationResult;
    }

    private void startPipeAndHandleNotifications(Block inputPistonBlock, List<ItemStack> itemsInPipe, boolean wasRequest) {
        EnumerationResult result = startPipe(inputPistonBlock, itemsInPipe, wasRequest);

        if (result == EnumerationResult.COMPLETED || warmupNotificationRadiusSquared <= 0)
            return;

        Location inputLocation = inputPistonBlock.getLocation();
        LanguageManager languageManager = CraftBookPlugin.inst().getLanguageManager();

        for (Player player : inputPistonBlock.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(inputLocation) > warmupNotificationRadiusSquared)
                continue;

            String message = languageManager.getString("circuits.pipes.warmup-notification", LanguageManager.getPlayersLanguage(player))
              .replace("{tubes}", String.valueOf(currentTubeBlockCounter))
              .replace("{pistons}", String.valueOf(currentPistonBlockCounter));

            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GOLD + message));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {
        if (!EventUtil.passesFilter(event))
            return;

        startPipeAndHandleNotifications(event.getBlock(), new ArrayList<>(), false);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPipeRequest(PipeRequestEvent event) {
        if (!EventUtil.passesFilter(event))
            return;

        startPipeAndHandleNotifications(event.getBlock(), event.getItems(), true);
    }

    private boolean pipesDiagonal;
    private @Nullable Material pipeInsulator;
    private boolean pipeStackPerPull;
    private boolean pipeRequireSign;
    private int maxTubeBlockCount;
    private int maxPistonBlockCount;
    private int maxCacheLoadCount;
    private int warmupNotificationRadiusSquared;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "allow-diagonal", "Allow pipes to work diagonally. Required for insulators to work.");
        pipesDiagonal = config.getBoolean(path + "allow-diagonal", false);

        config.setComment(path + "insulator-block", "When pipes work diagonally, this block allows the pipe to be insulated to not work diagonally.");
        BlockType insulatorType = BlockTypes.get(config.getString(path + "insulator-block", BlockTypes.WHITE_WOOL.id()));
        pipeInsulator = insulatorType == null ? null : BukkitAdapter.adapt(insulatorType);

        config.setComment(path + "stack-per-move", "This option stops the pipes taking the entire chest on power, and makes it just take a single stack.");
        pipeStackPerPull = config.getBoolean(path + "stack-per-move", true);

        config.setComment(path + "require-sign", "Requires pipes to have a [Pipe] sign connected to them. This is the only way to require permissions to make pipes.");
        pipeRequireSign = config.getBoolean(path + "require-sign", false);

        config.setComment(path + "max-tube-block-count", "After how many encountered tube-blocks to stop walking the pipe; -1 for no limit.");
        maxTubeBlockCount = config.getInt(path + "max-pipe-block-count", -1);

        config.setComment(path + "max-piston-block-count", "After how many encountered output-pistons to stop walking the pipe; -1 for no limit.");
        maxPistonBlockCount = config.getInt(path + "max-piston-block-count", -1);

        config.setComment(path + "max-cache-load-count", "When initially warming up caches, how many blocks to load in one go at max; -1 for no limit.");
        maxCacheLoadCount = config.getInt(path + "max-cache-load-count", 500);

        config.setComment(path + "chunk-retain-duration", "For how long, in seconds, to retain chunks in memory after having loaded them while traversing pipes");
        blockCache.setChunkTicketDuration(config.getInt(path + "chunk-retain-duration", BlockCache.DEFAULT_CHUNK_TICKET_DURATION));

        config.setComment(path + "warmup-notification-radius", "In what radius around an input-block to send warmup-notifications to player's action-bars; -1 to hide them");
        warmupNotificationRadiusSquared = config.getInt(path + "warmup-notification-radius", 5);
        warmupNotificationRadiusSquared = warmupNotificationRadiusSquared * warmupNotificationRadiusSquared;
    }
}