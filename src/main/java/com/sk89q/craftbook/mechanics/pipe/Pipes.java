package com.sk89q.craftbook.mechanics.pipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Dropper;
import org.bukkit.block.Furnace;
import org.bukkit.block.Jukebox;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Directional;
import org.bukkit.material.PistonBaseMaterial;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.InventoryUtil;
import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.ProtectionUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.VerifyUtil;
import com.sk89q.craftbook.util.events.SourcedBlockRedstoneEvent;
import com.sk89q.util.yaml.YAMLProcessor;

public class Pipes extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(!event.getLine(1).equalsIgnoreCase("[pipe]")) return;

        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if(!player.hasPermission("craftbook.circuits.pipes")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.create-permission");
            SignUtil.cancelSign(event);
            return;
        }

        if(ProtectionUtil.shouldUseProtection() && SignUtil.getBackBlock(event.getBlock()).getType() == Material.PISTON_STICKY_BASE) {

            PistonBaseMaterial pis = (PistonBaseMaterial) SignUtil.getBackBlock(event.getBlock()).getState().getData();
            Block off = SignUtil.getBackBlock(event.getBlock()).getRelative(pis.getFacing());
            if(InventoryUtil.doesBlockHaveInventory(off)) {
                if(!ProtectionUtil.canAccessInventory(event.getPlayer(), off)) {
                    if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                        player.printError("area.use-permission");
                    SignUtil.cancelSign(event);
                    return;
                }
            }
        }

        event.setLine(1, "[Pipe]");
        player.print("circuits.pipes.create");
    }

    public static ChangedSign getSignOnPiston(Block block) {

        BlockState state = block.getState();
        BlockFace facing = BlockFace.SELF;
        if(state.getData() instanceof Directional)
            facing = ((Directional) state.getData()).getFacing();

        for(BlockFace face : LocationUtil.getDirectFaces()) {

            if(face == facing || !SignUtil.isSign(block.getRelative(face)))
                continue;
            if(block.getRelative(face).getType() != Material.SIGN_POST && (face == BlockFace.UP || face == BlockFace.DOWN))
                continue;
            else if (block.getRelative(face).getType() == Material.SIGN_POST && face != BlockFace.UP && face != BlockFace.DOWN)
                continue;
            if(block.getRelative(face).getType() != Material.SIGN_POST && !SignUtil.getBackBlock(block.getRelative(face)).getLocation().equals(block.getLocation()))
                continue;
            ChangedSign sign = BukkitUtil.toChangedSign(block.getRelative(face));
            if(sign != null && sign.getLine(1).equalsIgnoreCase("[Pipe]"))
                return sign;
        }

        return null;
    }

    public void searchNearbyPipes(Block block, Set<Location> visitedPipes, List<ItemStack> items, Set<ItemStack> filters, Set<ItemStack> exceptions) {

        LinkedList<Block> searchQueue = new LinkedList<Block>();

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

                        if (Math.abs(x) == Math.abs(y) && Math.abs(x) == Math.abs(z) && Math.abs(y) == Math.abs(z)) {
                            if (pipeInsulator.isSame(block.getRelative(x, 0, 0))
                                    && pipeInsulator.isSame(block.getRelative(0, y, 0))
                                    && pipeInsulator.isSame(block.getRelative(0, 0, z))) {
                                continue;
                            }
                        } else if (Math.abs(x) == Math.abs(y)) {
                            if (pipeInsulator.isSame(block.getRelative(x, 0, 0))
                                    && pipeInsulator.isSame(block.getRelative(0, y, 0))) {
                                continue;
                            }
                        } else if (Math.abs(x) == Math.abs(z)) {
                            if (pipeInsulator.isSame(block.getRelative(x, 0, 0))
                                    && pipeInsulator.isSame(block.getRelative(0, 0, z))) {
                                continue;
                            }
                        } else if (Math.abs(y) == Math.abs(z)) {
                            if (pipeInsulator.isSame(block.getRelative(0, y, 0))
                                    && pipeInsulator.isSame(block.getRelative(0, 0, z))) {
                                continue;
                            }
                        }
                    }

                    Block off = block.getRelative(x, y, z);

                    if (!isValidPipeBlock(off.getType())) continue;

                    if (visitedPipes.contains(off.getLocation())) continue;

                    visitedPipes.add(off.getLocation());

                    if(block.getType() == Material.STAINED_GLASS && off.getType() == Material.STAINED_GLASS && block.getData() != off.getData()) continue;

                    if(off.getType() == Material.GLASS || off.getType() == Material.STAINED_GLASS)
                        searchQueue.add(off);
                    else if (off.getType() == Material.THIN_GLASS || off.getType() == Material.STAINED_GLASS_PANE) {
                        if (!isValidPipeBlock(off.getRelative(x, y, z).getType())) continue;
                        if (visitedPipes.contains(off.getRelative(x, y, z).getLocation())) continue;
                        if(off.getType() == Material.STAINED_GLASS_PANE) {
                            if((block.getType() == Material.STAINED_GLASS || block.getType() == Material.STAINED_GLASS_PANE) && off.getData() != block.getData() || (off.getRelative(x, y, z).getType() == Material.STAINED_GLASS || off.getRelative(x, y, z).getType() == Material.STAINED_GLASS_PANE) && off.getData() != off.getRelative(x, y, z).getData()) continue;
                        }
                        visitedPipes.add(off.getRelative(x, y, z).getLocation());
                        searchQueue.add(off.getRelative(x, y, z));
                    } else if(off.getType() == Material.PISTON_BASE)
                        searchQueue.add(0, off); //Pistons are treated with higher priority.
                }
            }
        }

        //Use the queue to search blocks.
        for(Block bl : searchQueue) {
            if (bl.getType() == Material.GLASS || bl.getType() == Material.STAINED_GLASS)
                searchNearbyPipes(bl, visitedPipes, items, filters, exceptions);
            else if (bl.getType() == Material.PISTON_BASE) {

                PistonBaseMaterial p = (PistonBaseMaterial) bl.getState().getData();

                ChangedSign sign = getSignOnPiston(bl);

                HashSet<ItemStack> pFilters = new HashSet<ItemStack>();
                HashSet<ItemStack> pExceptions = new HashSet<ItemStack>();

                if(sign != null) {

                    for(String line3 : RegexUtil.COMMA_PATTERN.split(sign.getLine(2))) {
                        pFilters.add(ItemSyntax.getItem(line3.trim()));
                    }
                    for(String line4 : RegexUtil.COMMA_PATTERN.split(sign.getLine(3))) {
                        pExceptions.add(ItemSyntax.getItem(line4.trim()));
                    }

                    pFilters.removeAll(Collections.singleton(null));
                    pExceptions.removeAll(Collections.singleton(null));
                }

                List<ItemStack> filteredItems = new ArrayList<ItemStack>(VerifyUtil.withoutNulls(ItemUtil.filterItems(items, pFilters, pExceptions)));

                if(filteredItems.isEmpty())
                    continue;

                List<ItemStack> newItems = new ArrayList<ItemStack>();

                Block fac = bl.getRelative(p.getFacing());
                if (InventoryUtil.doesBlockHaveInventory(fac)) {
                    newItems.addAll(InventoryUtil.addItemsToInventory((InventoryHolder) fac.getState(), filteredItems.toArray(new ItemStack[filteredItems.size()])));
                } else if(fac.getType() == Material.JUKEBOX) {
                    Jukebox juke = (Jukebox) fac.getState();
                    List<ItemStack> its = new ArrayList<ItemStack>(filteredItems);
                    if(!juke.isPlaying()) {
                        Iterator<ItemStack> iter = its.iterator();
                        while(iter.hasNext()) {
                            ItemStack st = iter.next();
                            if(!st.getType().isRecord()) continue;
                            juke.setPlaying(st.getType());
                            iter.remove();
                            break;
                        }
                    }
                    newItems.addAll(its);
                } else {
                    PipePutEvent event = new PipePutEvent(bl, new ArrayList<ItemStack>(filteredItems), fac);
                    Bukkit.getPluginManager().callEvent(event);

                    newItems.addAll(event.getItems());
                }

                items.removeAll(filteredItems);
                items.addAll(newItems);

                if (!items.isEmpty()) searchNearbyPipes(block, visitedPipes, items, filters, exceptions);
            } else if (bl.getType() == Material.DROPPER) {

                ChangedSign sign = getSignOnPiston(bl);

                HashSet<ItemStack> pFilters = new HashSet<ItemStack>();
                HashSet<ItemStack> pExceptions = new HashSet<ItemStack>();

                if(sign != null) {

                    for(String line3 : RegexUtil.COMMA_PATTERN.split(sign.getLine(2))) {
                        pFilters.add(ItemSyntax.getItem(line3.trim()));
                    }
                    for(String line4 : RegexUtil.COMMA_PATTERN.split(sign.getLine(3))) {
                        pExceptions.add(ItemSyntax.getItem(line4.trim()));
                    }

                    pFilters.removeAll(Collections.singleton(null));
                    pExceptions.removeAll(Collections.singleton(null));
                }

                List<ItemStack> filteredItems = new ArrayList<ItemStack>(VerifyUtil.withoutNulls(ItemUtil.filterItems(items, pFilters, pExceptions)));

                if(filteredItems.isEmpty())
                    continue;

                Dropper dropper = (Dropper) bl.getState();
                List<ItemStack> newItems = new ArrayList<ItemStack>();

                newItems.addAll(dropper.getInventory().addItem(filteredItems.toArray(new ItemStack[filteredItems.size()])).values());

                for(ItemStack stack : dropper.getInventory().getContents())
                    if(ItemUtil.isStackValid(stack))
                        for(int i = 0; i < stack.getAmount(); i++)
                            dropper.drop();

                items.removeAll(filteredItems);
                items.addAll(newItems);

                if (!items.isEmpty()) searchNearbyPipes(block, visitedPipes, items, filters, exceptions);
            }
        }
    }

    private boolean isValidPipeBlock(Material typeId) {

        return typeId == Material.GLASS || typeId == Material.STAINED_GLASS || typeId == Material.PISTON_BASE || typeId == Material.PISTON_STICKY_BASE || typeId == Material.WALL_SIGN || typeId == Material.DROPPER || typeId == Material.THIN_GLASS || typeId == Material.STAINED_GLASS_PANE;
    }

    public void startPipe(Block block, List<ItemStack> items, boolean request) {

        Set<ItemStack> filters = new HashSet<ItemStack>();
        Set<ItemStack> exceptions = new HashSet<ItemStack>();

        ChangedSign sign = getSignOnPiston(block);

        if(sign != null) {

            for(String line3 : RegexUtil.COMMA_PATTERN.split(sign.getLine(2))) {

                filters.add(ItemSyntax.getItem(line3.trim()));
            }
            for(String line4 : RegexUtil.COMMA_PATTERN.split(sign.getLine(3))) {

                exceptions.add(ItemSyntax.getItem(line4.trim()));
            }
        }

        filters.removeAll(Collections.singleton(null));
        exceptions.removeAll(Collections.singleton(null));

        Set<Location> visitedPipes = new HashSet<Location>();

        if (block.getType() == Material.PISTON_STICKY_BASE) {

            List<ItemStack> leftovers = new ArrayList<ItemStack>();

            PistonBaseMaterial p = (PistonBaseMaterial) block.getState().getData();
            Block fac = block.getRelative(p.getFacing());

            if (fac.getType() == Material.CHEST || fac.getType() == Material.TRAPPED_CHEST || fac.getType() == Material.DROPPER || fac.getType() == Material.DISPENSER || fac.getType() == Material.HOPPER) {

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

                PipeSuckEvent event = new PipeSuckEvent(block, new ArrayList<ItemStack>(items), fac);
                Bukkit.getPluginManager().callEvent(event);
                items.clear();
                items.addAll(event.getItems());
                if(!event.isCancelled()) {
                    visitedPipes.add(fac.getLocation());
                    searchNearbyPipes(block, visitedPipes, items, filters, exceptions);
                }

                if (!items.isEmpty()) {
                    for (ItemStack item : items) {
                        if (item == null) continue;
                        leftovers.addAll(((InventoryHolder) fac.getState()).getInventory().addItem(item).values());
                    }
                }
            } else if (fac.getType() == Material.FURNACE || fac.getType() == Material.BURNING_FURNACE) {

                Furnace f = (Furnace) fac.getState();
                if(!ItemUtil.doesItemPassFilters(f.getInventory().getResult(), filters, exceptions))
                    return;
                items.add(f.getInventory().getResult());
                if (f.getInventory().getResult() != null) f.getInventory().setResult(null);

                PipeSuckEvent event = new PipeSuckEvent(block, new ArrayList<ItemStack>(items), fac);
                Bukkit.getPluginManager().callEvent(event);
                items.clear();
                items.addAll(event.getItems());
                if(!event.isCancelled()) {
                    visitedPipes.add(fac.getLocation());
                    searchNearbyPipes(block, visitedPipes, items, filters, exceptions);
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

                items.add(new ItemStack(juke.getPlaying()));

                PipeSuckEvent event = new PipeSuckEvent(block, new ArrayList<ItemStack>(items), fac);
                Bukkit.getPluginManager().callEvent(event);
                items.clear();
                items.addAll(event.getItems());

                if(!event.isCancelled()) {
                    visitedPipes.add(fac.getLocation());
                    searchNearbyPipes(block, visitedPipes, items, filters, exceptions);
                }

                if (!items.isEmpty()) {
                    for (ItemStack item : items) {
                        if (item == null) continue;
                        block.getWorld().dropItem(BlockUtil.getBlockCentre(block), item);
                    }
                } else juke.setPlaying(null);
            } else {
                PipeSuckEvent event = new PipeSuckEvent(block, new ArrayList<ItemStack>(items), fac);
                Bukkit.getPluginManager().callEvent(event);
                items.clear();
                items.addAll(event.getItems());
                if(!event.isCancelled() && !items.isEmpty()) {
                    visitedPipes.add(fac.getLocation());
                    searchNearbyPipes(block, visitedPipes, items, filters, exceptions);
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

        if (event.getBlock().getType() == Material.PISTON_STICKY_BASE) {

            ChangedSign sign = getSignOnPiston(event.getBlock());

            if (pipeRequireSign && sign == null)
                return;

            if(!EventUtil.passesFilter(event)) return;

            startPipe(event.getBlock(), new ArrayList<ItemStack>(), false);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPipeRequest(PipeRequestEvent event) {

        if (event.getBlock().getType() == Material.PISTON_STICKY_BASE) {

            ChangedSign sign = getSignOnPiston(event.getBlock());

            if (pipeRequireSign && sign == null)
                return;

            if(!EventUtil.passesFilter(event)) return;

            startPipe(event.getBlock(), event.getItems(), true);
        }
    }

    boolean pipesDiagonal;
    ItemInfo pipeInsulator;
    boolean pipeStackPerPull;
    boolean pipeRequireSign;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "allow-diagonal", "Allow pipes to work diagonally. Required for insulators to work.");
        pipesDiagonal = config.getBoolean(path + "allow-diagonal", false);

        config.setComment(path + "insulator-block", "When pipes work diagonally, this block allows the pipe to be insulated to not work diagonally.");
        pipeInsulator = new ItemInfo(config.getString(path + "insulator-block", "WOOL"));

        config.setComment(path + "stack-per-move", "This option stops the pipes taking the entire chest on power, and makes it just take a single stack.");
        pipeStackPerPull = config.getBoolean(path + "stack-per-move", true);

        config.setComment(path + "require-sign", "Requires pipes to have a [Pipe] sign connected to them. This is the only way to require permissions to make pipes.");
        pipeRequireSign = config.getBoolean(path + "require-sign", false);
    }
}