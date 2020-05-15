package com.sk89q.craftbook.mechanics.pipe;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.util.BlockSyntax;
import com.sk89q.craftbook.util.BlockUtil;
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
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dropper;
import org.bukkit.block.Furnace;
import org.bukkit.block.Jukebox;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Piston;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
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
        return block.getType() == Material.PISTON || block.getType() == Material.STICKY_PISTON;
    }

    private static ChangedSign getSignOnPiston(Block block) {
        BlockData blockData = block.getBlockData();
        BlockFace facing = BlockFace.SELF;
        if(blockData instanceof Directional) {
            facing = ((Directional) blockData).getFacing();
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

    private void searchNearbyPipes(Block block, Set<Vector> visitedPipes, List<ItemStack> items) {
        Deque<Block> searchQueue = new ArrayDeque<>();
        searchQueue.addFirst(block);

        //Use the queue to search blocks.
        while (!searchQueue.isEmpty()) {
            Block bl = searchQueue.poll();
            if (bl.getType() == Material.PISTON) {
                Piston p = (Piston) bl.getBlockData();

                ChangedSign sign = getSignOnPiston(bl);

                HashSet<ItemStack> pFilters = new HashSet<>();
                HashSet<ItemStack> pExceptions = new HashSet<>();

                if(sign != null) {
                    for(String line3 : RegexUtil.COMMA_PATTERN.split(sign.getLine(2))) {
                        pFilters.add(ItemSyntax.getItem(line3.trim()));
                    }
                    for(String line4 : RegexUtil.COMMA_PATTERN.split(sign.getLine(3))) {
                        pExceptions.add(ItemSyntax.getItem(line4.trim()));
                    }

                    pFilters.removeAll(Collections.<ItemStack>singleton(null));
                    pExceptions.removeAll(Collections.<ItemStack>singleton(null));
                }

                List<ItemStack> filteredItems = new ArrayList<>(VerifyUtil.withoutNulls(ItemUtil.filterItems(items, pFilters, pExceptions)));

                if(filteredItems.isEmpty())
                    continue;

                List<ItemStack> newItems = new ArrayList<>();

                Block fac = bl.getRelative(p.getFacing());

                PipePutEvent event = new PipePutEvent(bl, new ArrayList<>(filteredItems), fac);
                Bukkit.getPluginManager().callEvent(event);

                if (!event.isCancelled()) {
                    if (InventoryUtil.doesBlockHaveInventory(fac)) {
                        InventoryHolder holder = (InventoryHolder) fac.getState();
                        newItems.addAll(InventoryUtil.addItemsToInventory(holder, event.getItems().toArray(new ItemStack[event.getItems().size()])));
                    } else if (fac.getType() == Material.JUKEBOX) {
                        Jukebox juke = (Jukebox) fac.getState();
                        List<ItemStack> its = new ArrayList<>(event.getItems());
                        if (juke.getPlaying() != Material.AIR) {
                            Iterator<ItemStack> iter = its.iterator();
                            while (iter.hasNext()) {
                                ItemStack st = iter.next();
                                if (!st.getType().isRecord()) continue;
                                juke.setPlaying(st.getType());
                                juke.update();
                                iter.remove();
                                break;
                            }
                        }
                        newItems.addAll(its);
                    } else {
                        newItems.addAll(event.getItems());
                    }

                    items.removeAll(filteredItems);
                    items.addAll(newItems);
                }
            } else if (bl.getType() == Material.DROPPER) {
                ChangedSign sign = getSignOnPiston(bl);

                HashSet<ItemStack> pFilters = new HashSet<>();
                HashSet<ItemStack> pExceptions = new HashSet<>();

                if(sign != null) {
                    for(String line3 : RegexUtil.COMMA_PATTERN.split(sign.getLine(2))) {
                        pFilters.add(ItemSyntax.getItem(line3.trim()));
                    }
                    for(String line4 : RegexUtil.COMMA_PATTERN.split(sign.getLine(3))) {
                        pExceptions.add(ItemSyntax.getItem(line4.trim()));
                    }

                    pFilters.removeAll(Collections.<ItemStack>singleton(null));
                    pExceptions.removeAll(Collections.<ItemStack>singleton(null));
                }

                List<ItemStack> filteredItems = new ArrayList<>(VerifyUtil.withoutNulls(ItemUtil.filterItems(items, pFilters, pExceptions)));

                if(filteredItems.isEmpty())
                    continue;

                Dropper dropper = (Dropper) bl.getState();
                List<ItemStack> newItems =
                        new ArrayList<>(dropper.getInventory().addItem(filteredItems.toArray(new ItemStack[filteredItems.size()])).values());

                for(ItemStack stack : dropper.getInventory().getContents())
                    if(ItemUtil.isStackValid(stack))
                        for(int i = 0; i < stack.getAmount(); i++)
                            dropper.drop();

                items.removeAll(filteredItems);
                items.addAll(newItems);
            }

            if (!items.isEmpty()) {
                //Enumerate the search queue.
                for (int x = -1; x < 2; x++) {
                    for (int y = -1; y < 2; y++) {
                        for (int z = -1; z < 2; z++) {

                            if(items.isEmpty())
                                return;

                            if (!pipesDiagonal) {
                                if (x != 0 && y != 0) continue;
                                if (x != 0 && z != 0) continue;
                                if (y != 0 && z != 0) continue;
                            } else {
                                boolean xIsY = Math.abs(x) == Math.abs(y);
                                boolean xIsZ = Math.abs(x) == Math.abs(z);
                                if (xIsY && xIsZ) {
                                    if (pipeInsulator.equalsFuzzy(BukkitAdapter.adapt(bl.getRelative(x, 0, 0).getBlockData()))
                                            && pipeInsulator.equalsFuzzy(BukkitAdapter.adapt(bl.getRelative(0, y, 0).getBlockData()))
                                            && pipeInsulator.equalsFuzzy(BukkitAdapter.adapt(bl.getRelative(0, 0, z).getBlockData()))) {
                                        continue;
                                    }
                                } else if (xIsY) {
                                    if (pipeInsulator.equalsFuzzy(BukkitAdapter.adapt(bl.getRelative(x, 0, 0).getBlockData()))
                                            && pipeInsulator.equalsFuzzy(BukkitAdapter.adapt(bl.getRelative(0, y, 0).getBlockData()))) {
                                        continue;
                                    }
                                } else if (xIsZ) {
                                    if (pipeInsulator.equalsFuzzy(BukkitAdapter.adapt(bl.getRelative(x, 0, 0).getBlockData()))
                                            && pipeInsulator.equalsFuzzy(BukkitAdapter.adapt(bl.getRelative(0, 0, z).getBlockData()))) {
                                        continue;
                                    }
                                } else {
                                    if (pipeInsulator.equalsFuzzy(BukkitAdapter.adapt(bl.getRelative(0, y, 0).getBlockData()))
                                            && pipeInsulator.equalsFuzzy(BukkitAdapter.adapt(bl.getRelative(0, 0, z).getBlockData()))) {
                                        continue;
                                    }
                                }
                            }

                            Block off = bl.getRelative(x, y, z);

                            if (!isValidPipeBlock(off)) continue;

                            if (visitedPipes.contains(off.getLocation().toVector())) continue;
                            visitedPipes.add(off.getLocation().toVector());

                            if(ItemUtil.isStainedGlass(bl.getType()) && ItemUtil.isStainedGlass(off.getType()) && bl.getType() != off.getType()) continue;

                            if(off.getType() == Material.GLASS || ItemUtil.isStainedGlass(off.getType())) {
                                searchQueue.add(off);
                            } else if (off.getType() == Material.GLASS_PANE || ItemUtil.isStainedGlassPane(off.getType())) {
                                Block offsetBlock = off.getRelative(x, y, z);
                                if (!isValidPipeBlock(offsetBlock)) continue;
                                if (visitedPipes.contains(offsetBlock.getLocation().toVector())) continue;
                                if(ItemUtil.isStainedGlassPane(off.getType())) {
                                    if((ItemUtil.isStainedGlass(bl.getType())
                                            || ItemUtil.isStainedGlassPane(bl.getType())) && ItemUtil.getStainedColor(off.getType()) != ItemUtil
                                            .getStainedColor(offsetBlock.getType())
                                            || (ItemUtil.isStainedGlass(offsetBlock.getType())
                                            || ItemUtil.isStainedGlassPane(offsetBlock.getType())) && ItemUtil.getStainedColor(off.getType()) != ItemUtil
                                            .getStainedColor(offsetBlock.getType())) continue;
                                }
                                visitedPipes.add(offsetBlock.getLocation().toVector());
                                searchQueue.add(off.getRelative(x, y, z));
                            } else if(off.getType() == Material.PISTON)
                                searchQueue.addFirst(off); //Pistons are treated with higher priority.
                        }
                    }
                }
            }
        }
    }

    private static boolean isValidPipeBlock(Block block) {
        switch (block.getType()) {
            case GLASS:
            case PISTON:
            case STICKY_PISTON:
            case DROPPER:
            case GLASS_PANE:
                return true;
            default:
                return ItemUtil.isStainedGlass(block.getType())
                        || ItemUtil.isStainedGlassPane(block.getType())
                        || SignUtil.isWallSign(block);
        }
    }

    private void startPipe(Block block, List<ItemStack> items, boolean request) {

        Set<ItemStack> filters = new HashSet<>();
        Set<ItemStack> exceptions = new HashSet<>();

        ChangedSign sign = getSignOnPiston(block);

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

        Set<Vector> visitedPipes = new HashSet<>();

        if (block.getType() == Material.STICKY_PISTON) {

            List<ItemStack> leftovers = new ArrayList<>();

            Piston p = (Piston) block.getBlockData();
            Block fac = block.getRelative(p.getFacing());

            if (fac.getType() == Material.CHEST
                    || fac.getType() == Material.TRAPPED_CHEST
                    || fac.getType() == Material.DROPPER
                    || fac.getType() == Material.DISPENSER
                    || fac.getType() == Material.HOPPER
                    || fac.getType() == Material.BARREL
                    || Tag.SHULKER_BOXES.isTagged(fac.getType())) {
                for (ItemStack stack : ((InventoryHolder) fac.getState()).getInventory().getContents()) {

                    if (!ItemUtil.isStackValid(stack))
                        continue;

                    if(!ItemUtil.doesItemPassFilters(stack, filters, exceptions))
                        continue;

                    items.add(stack);
                    ((InventoryHolder) fac.getState()).getInventory().removeItem(stack);
                    if (pipeStackPerPull)
                        break;
                }

                PipeSuckEvent event = new PipeSuckEvent(block, new ArrayList<>(items), fac);
                Bukkit.getPluginManager().callEvent(event);
                items.clear();
                items.addAll(event.getItems());
                if(!event.isCancelled()) {
                    visitedPipes.add(fac.getLocation().toVector());
                    searchNearbyPipes(block, visitedPipes, items);
                }

                if (!items.isEmpty()) {
                    for (ItemStack item : items) {
                        if (item == null) continue;
                        leftovers.addAll(((InventoryHolder) fac.getState()).getInventory().addItem(item).values());
                    }
                }
            } else if (fac.getType() == Material.FURNACE || fac.getType() == Material.BLAST_FURNACE || fac.getType() == Material.SMOKER) {

                Furnace f = (Furnace) fac.getState();
                if(!ItemUtil.doesItemPassFilters(f.getInventory().getResult(), filters, exceptions))
                    return;
                items.add(f.getInventory().getResult());
                if (f.getInventory().getResult() != null) f.getInventory().setResult(null);

                PipeSuckEvent event = new PipeSuckEvent(block, new ArrayList<>(items), fac);
                Bukkit.getPluginManager().callEvent(event);
                items.clear();
                items.addAll(event.getItems());
                if(!event.isCancelled()) {
                    visitedPipes.add(fac.getLocation().toVector());
                    searchNearbyPipes(block, visitedPipes, items);
                }

                if (!items.isEmpty()) {
                    for (ItemStack item : items) {
                        if (item == null) continue;
                        if(f.getInventory().getResult() == null)
                            f.getInventory().setResult(item);
                        else
                            leftovers.add(ItemUtil.addToStack(f.getInventory().getResult(), item));
                    }
                } else f.getInventory().setResult(null);
            } else if (fac.getType() == Material.JUKEBOX) {

                Jukebox juke = (Jukebox) fac.getState();

                if (juke.getPlaying() != Material.AIR) {
                    items.add(new ItemStack(juke.getPlaying()));

                    PipeSuckEvent event = new PipeSuckEvent(block, new ArrayList<>(items), fac);
                    Bukkit.getPluginManager().callEvent(event);
                    items.clear();
                    items.addAll(event.getItems());

                    if (!event.isCancelled()) {
                        visitedPipes.add(fac.getLocation().toVector());
                        searchNearbyPipes(block, visitedPipes, items);
                    }

                    if (!items.isEmpty()) {
                        for (ItemStack item : items) {
                            if (!ItemUtil.isStackValid(item)) continue;
                            block.getWorld().dropItem(BlockUtil.getBlockCentre(block), item);
                        }
                    } else {
                        juke.setPlaying(Material.AIR);
                        juke.update();
                    }
                }
            } else {
                PipeSuckEvent event = new PipeSuckEvent(block, new ArrayList<>(items), fac);
                Bukkit.getPluginManager().callEvent(event);
                items.clear();
                items.addAll(event.getItems());
                if(!event.isCancelled() && !items.isEmpty()) {
                    visitedPipes.add(fac.getLocation().toVector());
                    searchNearbyPipes(block, visitedPipes, items);
                }
                leftovers.addAll(items);
            }

            PipeFinishEvent fEvent = new PipeFinishEvent(block, leftovers, fac, request);
            Bukkit.getPluginManager().callEvent(fEvent);

            leftovers = fEvent.getItems();
            items.clear();

            if (!leftovers.isEmpty()) {
                for (ItemStack item : leftovers) {
                    if (!ItemUtil.isStackValid(item)) continue;
                    block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), item);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event){

        if (event.getBlock().getType() == Material.STICKY_PISTON) {

            ChangedSign sign = getSignOnPiston(event.getBlock());

            if (pipeRequireSign && sign == null)
                return;

            if(!EventUtil.passesFilter(event)) return;

            startPipe(event.getBlock(), new ArrayList<>(), false);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPipeRequest(PipeRequestEvent event) {

        if (event.getBlock().getType() == Material.STICKY_PISTON) {

            ChangedSign sign = getSignOnPiston(event.getBlock());

            if (pipeRequireSign && sign == null)
                return;

            if(!EventUtil.passesFilter(event)) return;

            startPipe(event.getBlock(), event.getItems(), true);
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
        pipeInsulator = BlockSyntax.getBlock(config.getString(path + "insulator-block", BlockTypes.WHITE_WOOL.getId()), true);

        config.setComment(path + "stack-per-move", "This option stops the pipes taking the entire chest on power, and makes it just take a single stack.");
        pipeStackPerPull = config.getBoolean(path + "stack-per-move", true);

        config.setComment(path + "require-sign", "Requires pipes to have a [Pipe] sign connected to them. This is the only way to require permissions to make pipes.");
        pipeRequireSign = config.getBoolean(path + "require-sign", false);
    }
}