package com.sk89q.craftbook.mechanics.pipe;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.util.BlockSyntax;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.InventoryUtil;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.ProtectionUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.VerifyUtil;
import com.sk89q.craftbook.util.events.SourcedBlockRedstoneEvent;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Jukebox;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Piston;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.*;
import org.bukkit.util.Vector;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Pipes extends AbstractCraftBookMechanic {

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

    private static ChangedSign getSignOnPiston(Block block) {
        BlockData blockData = block.getBlockData();
        BlockFace facing = BlockFace.SELF;
        if(blockData instanceof Directional directional) {
            facing = directional.getFacing();
        }

        for(BlockFace face : LocationUtil.getDirectFaces()) {
            if(face == facing || !SignUtil.isSign(block.getRelative(face)))
                continue;
            if(!SignUtil.isStandingSign(block.getRelative(face)) && (face == BlockFace.UP || face == BlockFace.DOWN))
                continue;
            else if (SignUtil.isStandingSign(block.getRelative(face)) && face != BlockFace.UP && face != BlockFace.DOWN)
                continue;
            if(!SignUtil.isStandingSign(block.getRelative(face)) && !SignUtil.getBackBlock(block.getRelative(face)).getLocation().equals(block.getLocation()))
                continue;
            ChangedSign sign = CraftBookBukkitUtil.toChangedSign(block.getRelative(face));
            if(sign != null && sign.getLine(1).equalsIgnoreCase("[Pipe]"))
                return sign;
        }

        return null;
    }

    private void locateExitNodesForItems(Block inputPistonBlock, Set<Vector> visitedBlocks, List<ItemStack> itemsInPipe) {
        enumeratePipeBlocks(inputPistonBlock, visitedBlocks, pipeBlock -> {
            if (itemsInPipe.isEmpty())
                return EnumerationHandleResult.DONE;

            if (pipeBlock.getType() != Material.PISTON)
                return EnumerationHandleResult.CONTINUE;

            ChangedSign sign = getSignOnPiston(pipeBlock);

            HashSet<ItemStack> filters = new HashSet<>();
            HashSet<ItemStack> exceptions = new HashSet<>();

            if(sign != null) {
                for(String line3 : RegexUtil.COMMA_PATTERN.split(sign.getLine(2))) {
                    filters.add(ItemSyntax.getItem(line3.trim()));
                }
                for(String line4 : RegexUtil.COMMA_PATTERN.split(sign.getLine(3))) {
                    exceptions.add(ItemSyntax.getItem(line4.trim()));
                }

                filters.removeAll(Collections.<ItemStack>singleton(null));
                exceptions.removeAll(Collections.<ItemStack>singleton(null));
            }

            List<ItemStack> filteredPipeItems = new ArrayList<>(VerifyUtil.withoutNulls(ItemUtil.filterItems(itemsInPipe, filters, exceptions)));

            PipeFilterEvent filterEvent = new PipeFilterEvent(pipeBlock, itemsInPipe, filters, exceptions, filteredPipeItems);
            Bukkit.getPluginManager().callEvent(filterEvent);

            filteredPipeItems = filterEvent.getFilteredItems();

            if(filteredPipeItems.isEmpty())
                return EnumerationHandleResult.CONTINUE;

            List<ItemStack> leftovers = new ArrayList<>();

            Piston piston = (Piston) pipeBlock.getBlockData();
            Block containerBlock = pipeBlock.getRelative(piston.getFacing());

            PipePutEvent putEvent = new PipePutEvent(pipeBlock, new ArrayList<>(filteredPipeItems), containerBlock);
            Bukkit.getPluginManager().callEvent(putEvent);

            if (putEvent.isCancelled())
                return EnumerationHandleResult.CONTINUE;

            List<ItemStack> itemsToPut = putEvent.getItems();

            if (InventoryUtil.doesBlockHaveInventory(containerBlock)) {
                InventoryHolder holder = (InventoryHolder) containerBlock.getState();
                leftovers.addAll(InventoryUtil.addItemsToInventory(holder, itemsToPut.toArray(new ItemStack[0])));
            }
            else if (containerBlock.getType() == Material.JUKEBOX) {
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

    private void enumeratePipeBlocks(Block inputPistonBlock, Set<Vector> visitedBlocks, PipeEnumerationHandler enumerationHandler) {
        Deque<Block> searchQueue = new ArrayDeque<>();
        searchQueue.addFirst(inputPistonBlock);

        while (!searchQueue.isEmpty()) {
            Block pipeBlock = searchQueue.poll();
            var handleResult = enumerationHandler.handle(pipeBlock);

            if (handleResult != EnumerationHandleResult.CONTINUE)
                return;

            Material pipeBlockType = pipeBlock.getType();

            for (int x = -1; x < 2; x++) {
                for (int y = -1; y < 2; y++) {
                    for (int z = -1; z < 2; z++) {

                        if (!pipesDiagonal) {
                            if (x != 0 && y != 0) continue;
                            if (x != 0 && z != 0) continue;
                            if (y != 0 && z != 0) continue;
                        } else {
                            boolean xIsY = Math.abs(x) == Math.abs(y);
                            boolean xIsZ = Math.abs(x) == Math.abs(z);
                            if (xIsY && xIsZ) {
                                if (pipeInsulator.equalsFuzzy(BukkitAdapter.adapt(pipeBlock.getRelative(x, 0, 0).getBlockData()))
                                        && pipeInsulator.equalsFuzzy(BukkitAdapter.adapt(pipeBlock.getRelative(0, y, 0).getBlockData()))
                                        && pipeInsulator.equalsFuzzy(BukkitAdapter.adapt(pipeBlock.getRelative(0, 0, z).getBlockData()))) {
                                    continue;
                                }
                            } else if (xIsY) {
                                if (pipeInsulator.equalsFuzzy(BukkitAdapter.adapt(pipeBlock.getRelative(x, 0, 0).getBlockData()))
                                        && pipeInsulator.equalsFuzzy(BukkitAdapter.adapt(pipeBlock.getRelative(0, y, 0).getBlockData()))) {
                                    continue;
                                }
                            } else if (xIsZ) {
                                if (pipeInsulator.equalsFuzzy(BukkitAdapter.adapt(pipeBlock.getRelative(x, 0, 0).getBlockData()))
                                        && pipeInsulator.equalsFuzzy(BukkitAdapter.adapt(pipeBlock.getRelative(0, 0, z).getBlockData()))) {
                                    continue;
                                }
                            } else {
                                if (pipeInsulator.equalsFuzzy(BukkitAdapter.adapt(pipeBlock.getRelative(0, y, 0).getBlockData()))
                                        && pipeInsulator.equalsFuzzy(BukkitAdapter.adapt(pipeBlock.getRelative(0, 0, z).getBlockData()))) {
                                    continue;
                                }
                            }
                        }

                        Block enumeratedBlock = pipeBlock.getRelative(x, y, z);
                        Material enumeratedType = enumeratedBlock.getType();

                        if (!isValidPipeBlock(enumeratedType)) continue;

                        if (visitedBlocks.contains(enumeratedBlock.getLocation().toVector())) continue;
                        visitedBlocks.add(enumeratedBlock.getLocation().toVector());

                        if(ItemUtil.isStainedGlass(pipeBlockType) && ItemUtil.isStainedGlass(enumeratedType) && pipeBlockType != enumeratedType) continue;

                        if(enumeratedType == Material.GLASS || ItemUtil.isStainedGlass(enumeratedType)) {
                            searchQueue.add(enumeratedBlock);
                        } else if (enumeratedType == Material.GLASS_PANE || ItemUtil.isStainedGlassPane(enumeratedType)) {
                            Block nextEnumeratedBlock = enumeratedBlock.getRelative(x, y, z);
                            Material nextEnumeratedType = nextEnumeratedBlock.getType();
                            if (!isValidPipeBlock(nextEnumeratedType)) continue;
                            if (visitedBlocks.contains(nextEnumeratedBlock.getLocation().toVector())) continue;
                            if(ItemUtil.isStainedGlassPane(enumeratedType)) {
                                if((ItemUtil.isStainedGlass(pipeBlockType)
                                        || ItemUtil.isStainedGlassPane(pipeBlockType)) && ItemUtil.getStainedColor(enumeratedType) != ItemUtil
                                        .getStainedColor(nextEnumeratedType)
                                        || (ItemUtil.isStainedGlass(nextEnumeratedType)
                                        || ItemUtil.isStainedGlassPane(nextEnumeratedType)) && ItemUtil.getStainedColor(enumeratedType) != ItemUtil
                                        .getStainedColor(nextEnumeratedType)) continue;
                            }
                            visitedBlocks.add(nextEnumeratedBlock.getLocation().toVector());
                            searchQueue.add(enumeratedBlock.getRelative(x, y, z));
                        } else if(enumeratedType == Material.PISTON)
                            searchQueue.addFirst(enumeratedBlock); //Pistons are treated with higher priority.
                    }
                }
            }
        }
    }

    private static boolean isValidPipeBlock(Material type) {
        return switch (type) {
            case GLASS, PISTON, STICKY_PISTON, GLASS_PANE -> true;
            default -> ItemUtil.isStainedGlass(type) || ItemUtil.isStainedGlassPane(type);
        };
    }

    private void startPipe(Block inputPistonBlock, List<ItemStack> itemsInPipe, boolean wasRequest) {
        if (inputPistonBlock.getType() != Material.STICKY_PISTON)
            return;

        // Parse sign

        Set<ItemStack> filters = new HashSet<>();
        Set<ItemStack> exceptions = new HashSet<>();

        ChangedSign sign = getSignOnPiston(inputPistonBlock);

        if(sign != null) {
            for(String line3 : RegexUtil.COMMA_PATTERN.split(sign.getLine(2))) {
                filters.add(ItemSyntax.getItem(line3.trim()));
            }
            for(String line4 : RegexUtil.COMMA_PATTERN.split(sign.getLine(3))) {
                exceptions.add(ItemSyntax.getItem(line4.trim()));
            }
        }

        filters.removeAll(Collections.<ItemStack>singleton(null));
        exceptions.removeAll(Collections.<ItemStack>singleton(null));

        // Setup auxiliaries

        Set<Vector> visitedBlocks = new HashSet<>();

        Piston piston = (Piston) inputPistonBlock.getBlockData();
        Block containerBlock = inputPistonBlock.getRelative(piston.getFacing());

        visitedBlocks.add(containerBlock.getLocation().toVector());

        // Suck items from container-block

        InventoryHolder inventoryHolder = null;
        Jukebox jukebox = null;

        if (InventoryUtil.doesBlockHaveInventory(containerBlock)) {
            inventoryHolder = ((InventoryHolder) containerBlock.getState());
            Inventory blockInventory = inventoryHolder.getInventory();

            if (blockInventory instanceof FurnaceInventory furnaceInventory) {
                ItemStack result = furnaceInventory.getResult();

                if (ItemUtil.isStackValid(result) && ItemUtil.doesItemPassFilters(result, filters, exceptions)) {
                    itemsInPipe.add(result);
                    furnaceInventory.setResult(null);
                }
            }

             else if (inventoryHolder instanceof BrewingStand brewingStand) {
                 if (brewingStand.getBrewingTime() <= 0) {
                     BrewerInventory inventory = brewingStand.getInventory();

                     for (int i = 0; i < 3; ++i) {
                         ItemStack item = inventory.getItem(i);

                         if (ItemUtil.isStackValid(item) && ItemUtil.doesItemPassFilters(item, filters, exceptions)) {
                             itemsInPipe.add(item);
                             inventory.setItem(i, null);
                         }
                     }
                 }
            }

            else {
                for (ItemStack stack : blockInventory.getContents()) {

                    if (!ItemUtil.isStackValid(stack))
                        continue;

                    if (!ItemUtil.doesItemPassFilters(stack, filters, exceptions))
                        continue;

                    itemsInPipe.add(stack);
                    blockInventory.removeItem(stack);

                    if (pipeStackPerPull)
                        break;
                }
            }
        }

        else if (containerBlock.getType() == Material.JUKEBOX) {
            jukebox = (Jukebox) containerBlock.getState();

            if (jukebox.hasRecord()) {
                itemsInPipe.add(jukebox.getRecord());
                jukebox.setRecord(null);
                jukebox.update();
            }
        }

        PipeSuckEvent suckEvent = new PipeSuckEvent(inputPistonBlock, new ArrayList<>(itemsInPipe), containerBlock);
        Bukkit.getPluginManager().callEvent(suckEvent);

        itemsInPipe.clear();

        for (ItemStack item : suckEvent.getItems()) {
            if (ItemUtil.isStackValid(item))
                itemsInPipe.add(item);
        }

        // Walk pipe to store as many items as possible

        if (!suckEvent.isCancelled() && !itemsInPipe.isEmpty())
            locateExitNodesForItems(inputPistonBlock, visitedBlocks, itemsInPipe);

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
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {
        Block pistonBlock = event.getBlock();

        if (pistonBlock.getType() == Material.STICKY_PISTON) {

            ChangedSign sign = getSignOnPiston(pistonBlock);

            if (pipeRequireSign && sign == null)
                return;

            if(!EventUtil.passesFilter(event)) return;

            startPipe(pistonBlock, new ArrayList<>(), false);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPipeRequest(PipeRequestEvent event) {
        Block pistonBlock = event.getBlock();

        if (pistonBlock.getType() == Material.STICKY_PISTON) {

            ChangedSign sign = getSignOnPiston(pistonBlock);

            if (pipeRequireSign && sign == null)
                return;

            if(!EventUtil.passesFilter(event)) return;

            startPipe(pistonBlock, event.getItems(), true);
        }
    }

    private boolean pipesDiagonal;
    private BlockStateHolder<?> pipeInsulator;
    private boolean pipeStackPerPull;
    private boolean pipeRequireSign;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "allow-diagonal", "Allow pipes to work diagonally. Required for insulators to work.");
        pipesDiagonal = config.getBoolean(path + "allow-diagonal", false);

        config.setComment(path + "insulator-block", "When pipes work diagonally, this block allows the pipe to be insulated to not work diagonally.");
        pipeInsulator = BlockSyntax.getBlock(config.getString(path + "insulator-block", BlockTypes.WHITE_WOOL.id()), true);

        config.setComment(path + "stack-per-move", "This option stops the pipes taking the entire chest on power, and makes it just take a single stack.");
        pipeStackPerPull = config.getBoolean(path + "stack-per-move", true);

        config.setComment(path + "require-sign", "Requires pipes to have a [Pipe] sign connected to them. This is the only way to require permissions to make pipes.");
        pipeRequireSign = config.getBoolean(path + "require-sign", false);
    }
}