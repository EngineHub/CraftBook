package com.sk89q.craftbook.mechanics.pipe;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.core.LanguageManager;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.InventoryUtil;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.ProtectionUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.VerifyUtil;
import com.sk89q.craftbook.util.events.SourcedBlockRedstoneEvent;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.block.data.type.Piston;
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

public class Pipes extends AbstractCraftBookMechanic implements PipesApi {

    private int currentTubeBlockCounter;
    private int currentPistonBlockCounter;

    private final BlockCacheRegistry cacheRegistry;

    private BlockCache currentBlockCache;

    public Pipes() {
        this.cacheRegistry = new BlockCacheRegistry();
    }

    @Override
    public void disable() {
        super.disable();

        cacheRegistry.disable();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        if (!EventUtil.passesFilter(event)) return;

        if (!event.getLine(1).equalsIgnoreCase("[pipe]")) return;

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (!player.hasPermission("craftbook.circuits.pipes")) {
            if (CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.create-permission");
            SignUtil.cancelSign(event);
            return;
        }

        if (ProtectionUtil.shouldUseProtection()) {
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
            if (pistonBlock != null && isPiston(pistonBlock)) {
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

    private EnumerationResult locateExitNodesForItems(Block inputPistonBlock, LongSet visitedBlocks, List<ItemStack> itemsInPipe) {
        return enumeratePipeBlocks(inputPistonBlock, visitedBlocks, (pipeBlock, cachedPipeBlock) -> {
            if (itemsInPipe.isEmpty())
                return EnumerationDecision.STOP;

            if (!CachedBlock.isMaterial(cachedPipeBlock, Material.PISTON))
                return EnumerationDecision.CONTINUE;

            PipeSign sign = currentBlockCache.getSignOnPiston(pipeBlock, cachedPipeBlock);

            List<ItemStack> filteredPipeItems = new ArrayList<>(VerifyUtil.withoutNulls(ItemUtil.filterItems(itemsInPipe, sign.includeFilters, sign.excludeFilters)));

            PipeFilterEvent filterEvent = new PipeFilterEvent(pipeBlock, itemsInPipe, sign.includeFilters, sign.excludeFilters, filteredPipeItems);
            Bukkit.getPluginManager().callEvent(filterEvent);

            filteredPipeItems = filterEvent.getFilteredItems();

            if (filteredPipeItems.isEmpty())
                return EnumerationDecision.CONTINUE;

            List<ItemStack> leftovers = new ArrayList<>();

            Block containerBlock = pipeBlock.getRelative(CachedBlock.getFacing(cachedPipeBlock));
            int cachedContainerBlock = currentBlockCache.getCachedBlock(containerBlock);

            PipePutEvent putEvent = new PipePutEvent(pipeBlock, new ArrayList<>(filteredPipeItems), containerBlock, cachedContainerBlock);
            Bukkit.getPluginManager().callEvent(putEvent);

            if (putEvent.isCancelled())
                return EnumerationDecision.CONTINUE;

            List<ItemStack> itemsToPut = putEvent.getItems();

            if (
                CachedBlock.hasHandledOutputInventory(cachedContainerBlock)
                    && containerBlock.getState() instanceof InventoryHolder holder
            ) {
                leftovers.addAll(InventoryUtil.addItemsToInventory(holder, itemsToPut.toArray(new ItemStack[0])));
            } else if (CachedBlock.isMaterial(cachedContainerBlock, Material.JUKEBOX)) {
                Jukebox jukebox = (Jukebox) containerBlock.getState();

                for (ItemStack item : itemsToPut) {
                    if (jukebox.hasRecord() || !item.getType().isRecord()) {
                        leftovers.add(item);
                        continue;
                    }

                    jukebox.setRecord(item);
                    jukebox.update();
                }
            } else {
                leftovers.addAll(itemsToPut);
            }

            itemsInPipe.removeAll(itemsToPut);
            itemsInPipe.addAll(leftovers);

            return itemsInPipe.isEmpty() ? EnumerationDecision.STOP : EnumerationDecision.CONTINUE;
        });
    }

    @Override
    public EnumerationResult enumeratePipeBlocks(Block firstBlock, @Nullable LongSet visitedBlocks, PipeEnumerationHandler enumerationHandler) {
        if (!Bukkit.isPrimaryThread())
            throw new IllegalStateException("This method must be called on the main server thread");

        try {
            // Seeing how this is public API, assign the current block-cache again, because it
            // will only be correctly set when called through #startPipe.
            this.currentBlockCache = cacheRegistry.getBlockCache(firstBlock.getWorld());

            if (visitedBlocks == null)
                visitedBlocks = new LongOpenHashSet();

            currentTubeBlockCounter = currentPistonBlockCounter = 0;
            currentBlockCache.resetCacheLoadCounter();

            Deque<Block> searchQueue = new ArrayDeque<>();
            searchQueue.addFirst(firstBlock);
            visitedBlocks.add(CompactId.computeWorldlessBlockId(firstBlock));

            while (!searchQueue.isEmpty()) {
                Block pipeBlock = searchQueue.poll();
                int cachedPipeBlock = currentBlockCache.getCachedBlock(pipeBlock);

                if (CachedBlock.isTube(cachedPipeBlock)) {
                    ++currentTubeBlockCounter;

                    if (maxTubeBlockCount >= 0 && currentTubeBlockCounter > maxTubeBlockCount)
                        return EnumerationResult.EXCEEDED_TUBE_COUNT_LIMIT;
                }

                if (CachedBlock.isMaterial(cachedPipeBlock, Material.PISTON)) {
                    ++currentPistonBlockCounter;

                    if (maxPistonBlockCount >= 0 && currentPistonBlockCounter > maxPistonBlockCount)
                        return EnumerationResult.EXCEEDED_PISTON_COUNT_LIMIT;
                }

                EnumerationDecision handleResult = enumerationHandler.handle(pipeBlock, cachedPipeBlock);

                if (handleResult != EnumerationDecision.CONTINUE)
                    return EnumerationResult.COMPLETED;

                // While we could check for exceeding the load-counter at countless call-sites, and while there already have been
                // a few cache-lookups prior to enumerating, a hand-full blocks more don't matter in the grand scheme of things.
                if (maxCacheLoadCount >= 0 && currentBlockCache.getCacheLoadCounter() >= maxCacheLoadCount)
                    return EnumerationResult.EXCEEDED_CACHE_LOAD_LIMIT;

                for (int x = -1; x < 2; x++) {
                    for (int y = -1; y < 2; y++) {
                        for (int z = -1; z < 2; z++) {
                            if (x == 0 && y == 0 && z == 0) continue;

                            if (!pipesDiagonal) {
                                if (x != 0 && y != 0) continue;
                                if (x != 0 && z != 0) continue;
                                if (y != 0 && z != 0) continue;
                            } else if (pipeInsulator != null) {
                                boolean xIsY = Math.abs(x) == Math.abs(y);
                                boolean xIsZ = Math.abs(x) == Math.abs(z);
                                if (xIsY && xIsZ) {
                                    if (CachedBlock.isMaterial(currentBlockCache.getCachedBlock(pipeBlock.getRelative(x, 0, 0)), pipeInsulator)
                                      && CachedBlock.isMaterial(currentBlockCache.getCachedBlock(pipeBlock.getRelative(0, y, 0)), pipeInsulator)
                                      && CachedBlock.isMaterial(currentBlockCache.getCachedBlock(pipeBlock.getRelative(0, 0, z)), pipeInsulator)) {
                                        continue;
                                    }
                                } else if (xIsY) {
                                    if (CachedBlock.isMaterial(currentBlockCache.getCachedBlock(pipeBlock.getRelative(x, 0, 0)), pipeInsulator)
                                      && CachedBlock.isMaterial(currentBlockCache.getCachedBlock(pipeBlock.getRelative(0, y, 0)), pipeInsulator)) {
                                        continue;
                                    }
                                } else if (xIsZ) {
                                    if (CachedBlock.isMaterial(currentBlockCache.getCachedBlock(pipeBlock.getRelative(x, 0, 0)), pipeInsulator)
                                      && CachedBlock.isMaterial(currentBlockCache.getCachedBlock(pipeBlock.getRelative(0, 0, z)), pipeInsulator)) {
                                        continue;
                                    }
                                } else {
                                    if (CachedBlock.isMaterial(currentBlockCache.getCachedBlock(pipeBlock.getRelative(0, y, 0)), pipeInsulator)
                                      && CachedBlock.isMaterial(currentBlockCache.getCachedBlock(pipeBlock.getRelative(0, 0, z)), pipeInsulator)) {
                                        continue;
                                    }
                                }
                            }

                            Block enumeratedBlock = pipeBlock.getRelative(x, y, z);
                            int cachedEnumeratedBlock = currentBlockCache.getCachedBlock(enumeratedBlock);

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
                            int cachedNextEnumeratedBlock = currentBlockCache.getCachedBlock(nextEnumeratedBlock);

                            if (!CachedBlock.isValidPipeBlock(cachedNextEnumeratedBlock))
                                continue;

                            long nextEnumeratedId = CompactId.computeWorldlessBlockId(nextEnumeratedBlock);

                            if (visitedBlocks.contains(nextEnumeratedId))
                                continue;

                            // Ensure that the pane is allowed to link with the block we're jumping across to
                            if (CachedBlock.doTubeColorsMismatch(cachedEnumeratedBlock, cachedNextEnumeratedBlock))
                                continue;

                            visitedBlocks.add(nextEnumeratedId);
                            searchQueue.add(nextEnumeratedBlock);
                        }
                    }
                }
            }

            return EnumerationResult.COMPLETED;
        } catch (LoadingChunkException e) {
            return EnumerationResult.NEEDS_CHUNK_LOADING;
        }
    }

    @Override
    public int getMaxTubeBlockCount() {
        return maxTubeBlockCount;
    }

    @Override
    public int getMaxPistonBlockCount() {
        return maxPistonBlockCount;
    }

    @Override
    public int getMaxCacheLoadCount() {
        return maxCacheLoadCount;
    }

    private EnumerationResult startPipe(Block inputPistonBlock, List<ItemStack> itemsInPipe, boolean wasRequest) {
        this.currentBlockCache = cacheRegistry.getBlockCache(inputPistonBlock.getWorld());

        PipeSign sign;
        Block containerBlock;
        int cachedContainerBlock;

        try {
            int cachedInputPistonBlock = currentBlockCache.getCachedBlock(inputPistonBlock);

            if (!CachedBlock.isMaterial(cachedInputPistonBlock, Material.STICKY_PISTON))
                return EnumerationResult.COMPLETED;

            sign = currentBlockCache.getSignOnPiston(inputPistonBlock, cachedInputPistonBlock);

            if (pipeRequireSign && sign == PipeSign.NO_SIGN)
                return EnumerationResult.COMPLETED;

            containerBlock = inputPistonBlock.getRelative(CachedBlock.getFacing(cachedInputPistonBlock));
            cachedContainerBlock = currentBlockCache.getCachedBlock(containerBlock);
        }
        // If the very beginning of the pipe already (partially) is within an unloaded chunk,
        // there's no need to start the process at all.
        catch (LoadingChunkException ignored) {
            return EnumerationResult.NEEDS_CHUNK_LOADING;
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
            } else if (inventoryHolder instanceof BrewingStand brewingStand) {
                if (brewingStand.getBrewingTime() <= 0) {
                    BrewerInventory inventory = brewingStand.getInventory();

                    for (int i = 0; i < 3; ++i) {
                        ItemStack item = inventory.getItem(i);

                        if (ItemUtil.isStackValid(item) && ItemUtil.doesItemPassFilters(item, sign.includeFilters, sign.excludeFilters)) {
                            itemsInPipe.add(item);
                            inventory.setItem(i, null);

                            if (pipeStackPerPull)
                                break;
                        }
                    }
                }
            } else {
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
        } else if (CachedBlock.isMaterial(cachedContainerBlock, Material.JUKEBOX)) {
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

        if (!suckEvent.isCancelled() && !itemsInPipe.isEmpty())
            enumerationResult = locateExitNodesForItems(inputPistonBlock, visitedBlocks, itemsInPipe);

        // Try to put leftovers back into the block, if the limits have not been exceeded; otherwise,
        // let them be dropped at the input-container, as to avoid unending loops.

        List<ItemStack> leftovers = new ArrayList<>();

        if (enumerationResult.didExceedExtentLimits) {
            leftovers.addAll(itemsInPipe);
        } else if (!itemsInPipe.isEmpty()) {
            if (inventoryHolder != null) {
                leftovers.addAll(InventoryUtil.addItemsToInventory(inventoryHolder, itemsInPipe.toArray(new ItemStack[0])));
            } else if (jukebox != null) {
                for (ItemStack item : itemsInPipe) {
                    if (jukebox.hasRecord() || !item.getType().isRecord()) {
                        leftovers.add(item);
                        continue;
                    }

                    jukebox.setRecord(item);
                    jukebox.update();
                }
            } else {
                leftovers.addAll(itemsInPipe);
            }
        }

        // Finish up the pipe and possibly drop leftovers

        PipeFinishEvent finishEvent = new PipeFinishEvent(inputPistonBlock, leftovers, containerBlock, wasRequest);
        Bukkit.getPluginManager().callEvent(finishEvent);

        leftovers = finishEvent.getItems();
        itemsInPipe.clear();

        if (!leftovers.isEmpty()) {
            for (ItemStack item : leftovers) {
                if (!ItemUtil.isStackValid(item))
                    continue;

                inputPistonBlock.getWorld().dropItemNaturally(inputPistonBlock.getLocation().add(0.5, 0.5, 0.5), item);
            }
        }

        return enumerationResult;
    }

    private void startPipeAndHandleNotifications(Block inputPistonBlock, List<ItemStack> itemsInPipe, boolean wasRequest) {
        EnumerationResult result = startPipe(inputPistonBlock, itemsInPipe, wasRequest);

        if (result == EnumerationResult.COMPLETED)
            return;

        if (notificationRadiusSquared <= 0)
            return;

        Location inputLocation = inputPistonBlock.getLocation();
        LanguageManager languageManager = CraftBookPlugin.inst().getLanguageManager();

        for (Player player : inputPistonBlock.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(inputLocation) > notificationRadiusSquared)
                continue;

            String message;

            if (result == EnumerationResult.NEEDS_CHUNK_LOADING || result == EnumerationResult.EXCEEDED_CACHE_LOAD_LIMIT) {
                message = ChatColor.GOLD + languageManager.getString("circuits.pipes.warmup-notification", LanguageManager.getPlayersLanguage(player))
                    .replace("{tubes}", String.valueOf(currentTubeBlockCounter))
                    .replace("{pistons}", String.valueOf(currentPistonBlockCounter));
            } else if (result == EnumerationResult.EXCEEDED_TUBE_COUNT_LIMIT) {
                message = ChatColor.RED + languageManager.getString("circuits.pipes.exceeded-tube-count-notification", LanguageManager.getPlayersLanguage(player))
                    .replace("{limit}", String.valueOf(maxTubeBlockCount));
            } else if (result == EnumerationResult.EXCEEDED_PISTON_COUNT_LIMIT) {
                message = ChatColor.RED + languageManager.getString("circuits.pipes.exceeded-piston-count-notification", LanguageManager.getPlayersLanguage(player))
                    .replace("{limit}", String.valueOf(maxPistonBlockCount));
            } else {
                continue;
            }

            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
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
    private int notificationRadiusSquared;

    @Override
    public void loadConfiguration(YAMLProcessor config, String path) {

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

        config.setComment(path + "initial-chunk-retain-duration", "For how long, in seconds, to retain chunks in memory after having loaded them while traversing pipes; -1 for no retainment at all.");
        cacheRegistry.setInitialChunkTicketDuration(config.getInt(path + "initial-chunk-retain-duration", BlockCacheRegistry.DEFAULT_INITIAL_CHUNK_TICKET_DURATION) * 1000);

        config.setComment(path + "continued-chunk-retain-duration", "For how long, in seconds, to retain chunks in memory that contain regularly accessed blocks; -1 for no continued retainment.");
        cacheRegistry.setContinuedChunkTicketDuration(config.getInt(path + "continued-chunk-retain-duration", BlockCacheRegistry.DEFAULT_CONTINUED_CHUNK_TICKET_DURATION) * 1000);

        config.setComment(path + "notification-radius", "In what radius around an input-block to send notifications to player's action-bars; -1 to hide them");
        notificationRadiusSquared = config.getInt(path + "notification-radius", 5);
        notificationRadiusSquared = notificationRadiusSquared * notificationRadiusSquared;
    }
}