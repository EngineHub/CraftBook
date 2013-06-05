package com.sk89q.craftbook.circuits;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Furnace;
import org.bukkit.block.Sign;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.PistonBaseMaterial;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.SourcedBlockRedstoneEvent;
import com.sk89q.craftbook.bukkit.BukkitConfiguration;
import com.sk89q.craftbook.bukkit.CircuitCore;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.ICMechanic;
import com.sk89q.craftbook.circuits.ic.PipeInputIC;
import com.sk89q.craftbook.util.InventoryUtil;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.VerifyUtil;
import com.sk89q.craftbook.util.exceptions.InvalidMechanismException;
import com.sk89q.craftbook.util.exceptions.ProcessedMechanismException;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;

public class Pipes extends AbstractMechanic {

    final CraftBookPlugin plugin = CraftBookPlugin.inst();

    public static class Factory extends AbstractMechanicFactory<Pipes> {

        /**
         * Used to construct a pipe from a mechanic as an output method.
         *
         * @param pipe The pipe start block. Doesn't need to be a pipe.
         * @param source The block that the pipe is facing.
         * @param items The items to send to the pipe.
         * @return The pipe constructed, otherwise null.
         */
        public static Pipes setupPipes(Block pipe, Block source, ItemStack ... items) {

            if (pipe.getTypeId() == BlockID.PISTON_STICKY_BASE) {

                PistonBaseMaterial p = (PistonBaseMaterial) pipe.getState().getData();
                Block fac = pipe.getRelative(p.getFacing());
                if (fac.getLocation().equals(source.getLocation()))
                    if (CircuitCore.inst().getPipeFactory() != null)
                        return CircuitCore.inst().getPipeFactory().detectWithItems(BukkitUtil.toWorldVector(pipe), Arrays.asList(items));
            }

            return null;
        }

        @Override
        public Pipes detect(BlockWorldVector pt) throws InvalidMechanismException {

            return detectWithItems(pt, null);
        }

        public Pipes detectWithItems(BlockWorldVector pt, List<ItemStack> items) {

            int type = BukkitUtil.toWorld(pt).getBlockTypeIdAt(BukkitUtil.toLocation(pt));

            if (type == BlockID.PISTON_STICKY_BASE) {

                PistonBaseMaterial piston = (PistonBaseMaterial) BukkitUtil.toBlock(pt).getState().getData();
                Sign sign = getSignOnPiston(piston, BukkitUtil.toBlock(pt));

                if (CraftBookPlugin.inst().getConfiguration().pipeRequireSign && sign == null)
                    return null;

                return new Pipes(pt, sign == null ? null : BukkitUtil.toChangedSign(sign), items);
            }

            return null;
        }

        @Override
        public Pipes detect(BlockWorldVector pos, LocalPlayer player, ChangedSign sign) throws InvalidMechanismException, ProcessedMechanismException {

            if(sign.getLine(1).equalsIgnoreCase("[Pipe]")) {
                player.checkPermission("craftbook.circuits.pipes");

                player.print("circuits.pipes.create");
                sign.setLine(1, "[Pipe]");

                throw new ProcessedMechanismException();
            }

            return null;
        }
    }

    public static Sign getSignOnPiston(PistonBaseMaterial piston, Block block) {

        for(BlockFace face : LocationUtil.getDirectFaces()) {

            Sign sign = null;

            if(face == piston.getFacing() || !(block.getRelative(face).getState() instanceof Sign))
                continue;
            sign = (Sign) block.getRelative(face).getState();
            if(sign.getBlock().getTypeId() != BlockID.SIGN_POST && (face == BlockFace.UP || face == BlockFace.DOWN))
                continue;
            if(!SignUtil.getBackBlock(sign.getBlock()).getLocation().equals(block.getLocation()))
                continue;
            if(sign != null && sign.getLine(1).equalsIgnoreCase("[Pipe]"))
                return sign;
        }

        return null;
    }

    /**
     * Construct the mechanic for a location.
     *
     * @param pt The location
     * @param sign The sign
     * @items The items to start with (Optional)
     */
    private Pipes(BlockWorldVector pt, ChangedSign sign, List<ItemStack> items) {

        super();

        scanSign(sign);

        if(items != null && !items.isEmpty()) {
            this.items.addAll(items);
            startPipe(BukkitUtil.toBlock(pt));
        }
    }

    public void scanSign(ChangedSign sign) {

        if(sign != null) {

            for(String line3 : RegexUtil.COMMA_PATTERN.split(sign.getLine(2))) {

                filters.add(ItemUtil.getItem(line3.trim()));
            }
            for(String line4 : RegexUtil.COMMA_PATTERN.split(sign.getLine(3))) {

                exceptions.add(ItemUtil.getItem(line4.trim()));
            }
        }

        while(filters.remove(null)){}
        while(exceptions.remove(null)){}
    }

    private HashSet<ItemStack> filters = new HashSet<ItemStack>();
    private HashSet<ItemStack> exceptions = new HashSet<ItemStack>();

    private List<ItemStack> items = new ArrayList<ItemStack>();
    private List<BlockVector> visitedPipes = new ArrayList<BlockVector>();

    public List<ItemStack> getItems() {

        while(items.remove(null)){}
        return items;
    }

    public void searchNearbyPipes(Block block) {

        BukkitConfiguration config = CraftBookPlugin.inst().getConfiguration();

        LinkedList<Block> searchQueue = new LinkedList<Block>();

        //Enumerate the search queue.
        for (int x = -1; x < 2; x++) {
            for (int y = -1; y < 2; y++) {
                for (int z = -1; z < 2; z++) {

                    if(items.isEmpty())
                        return;

                    if (!config.pipesDiagonal) {
                        if (x != 0 && y != 0) continue;
                        if (x != 0 && z != 0) continue;
                        if (y != 0 && z != 0) continue;
                    } else {

                        if (Math.abs(x) == Math.abs(y) && Math.abs(x) == Math.abs(z) && Math.abs(y) == Math.abs(z)) {
                            if (block.getRelative(x, 0, 0).getTypeId() == config.pipeInsulator
                                    && block.getRelative(0, y, 0).getTypeId() == config.pipeInsulator
                                    && block.getRelative(0, 0, z).getTypeId() == config.pipeInsulator) {
                                continue;
                            }
                        } else if (Math.abs(x) == Math.abs(y)) {
                            if (block.getRelative(x, 0, 0).getTypeId() == config.pipeInsulator
                                    && block.getRelative(0, y, 0).getTypeId() == config.pipeInsulator) {
                                continue;
                            }
                        } else if (Math.abs(x) == Math.abs(z)) {
                            if (block.getRelative(x, 0, 0).getTypeId() == config.pipeInsulator
                                    && block.getRelative(0, 0, z).getTypeId() == config.pipeInsulator) {
                                continue;
                            }
                        } else if (Math.abs(y) == Math.abs(z)) {
                            if (block.getRelative(0, y, 0).getTypeId() == config.pipeInsulator
                                    && block.getRelative(0, 0, z).getTypeId() == config.pipeInsulator) {
                                continue;
                            }
                        }
                    }

                    Block off = block.getRelative(x, y, z);

                    if (!isValidPipeBlock(off.getTypeId())) continue;

                    BlockVector bv = BukkitUtil.toVector(off);
                    if (visitedPipes.contains(bv)) continue;

                    visitedPipes.add(bv);

                    if(off.getTypeId() == BlockID.GLASS)
                        searchQueue.add(off);
                    else if(off.getTypeId() == BlockID.PISTON_BASE)
                        searchQueue.add(0, off); //Pistons are treated with higher priority.
                }
            }
        }

        //Use the queue to search blocks.
        for(Block bl : searchQueue) {
            if (bl.getTypeId() == BlockID.GLASS)
                searchNearbyPipes(bl);
            else if (bl.getTypeId() == BlockID.PISTON_BASE) {

                PistonBaseMaterial p = (PistonBaseMaterial) bl.getState().getData();

                Sign sign = getSignOnPiston(p, bl);

                HashSet<ItemStack> pFilters = new HashSet<ItemStack>();
                HashSet<ItemStack> pExceptions = new HashSet<ItemStack>();

                if(sign != null) {

                    for(String line3 : RegexUtil.COMMA_PATTERN.split(sign.getLine(2))) {
                        pFilters.add(ItemUtil.getItem(line3.trim()));
                    }
                    for(String line4 : RegexUtil.COMMA_PATTERN.split(sign.getLine(3))) {
                        pExceptions.add(ItemUtil.getItem(line4.trim()));
                    }

                    while(pFilters.remove(null)){}
                    while(pExceptions.remove(null)){}
                }

                List<ItemStack> filteredItems = new ArrayList<ItemStack>(VerifyUtil.<ItemStack>withoutNulls(ItemUtil.filterItems(items, pFilters, pExceptions)));

                if(filteredItems.isEmpty())
                    continue;

                List<ItemStack> newItems = new ArrayList<ItemStack>();

                Block fac = bl.getRelative(p.getFacing());
                if (fac.getState() instanceof InventoryHolder) {

                    newItems.addAll(InventoryUtil.addItemsToInventory((InventoryHolder) fac.getState(), filteredItems.toArray(new ItemStack[filteredItems.size()])));

                } else if (fac.getTypeId() == BlockID.WALL_SIGN) {

                    CircuitCore circuitCore = CircuitCore.inst();
                    if (circuitCore.getICFactory() == null) continue;

                    try {
                        ICMechanic icmech = circuitCore.getICFactory().detect(BukkitUtil.toWorldVector(fac));
                        if (icmech == null || !(icmech.getIC() instanceof PipeInputIC)) continue;
                        newItems.addAll(((PipeInputIC) icmech.getIC()).onPipeTransfer(BukkitUtil.toWorldVector(bl), filteredItems));
                    } catch (Exception e) {
                        BukkitUtil.printStacktrace(e);
                    }
                } else {

                    newItems.addAll(filteredItems);
                }

                items.removeAll(filteredItems);
                items.addAll(newItems);

                if (!items.isEmpty()) searchNearbyPipes(block);
            }
        }
    }

    private boolean isValidPipeBlock(int typeId) {

        return typeId == BlockID.GLASS || typeId == BlockID.PISTON_BASE || typeId == BlockID.PISTON_STICKY_BASE || typeId == BlockID.WALL_SIGN;
    }

    public void startPipe(Block block) {

        visitedPipes.clear();

        if (block.getTypeId() == BlockID.PISTON_STICKY_BASE) {

            List<ItemStack> leftovers = new ArrayList<ItemStack>();

            PistonBaseMaterial p = (PistonBaseMaterial) block.getState().getData();
            Block fac = block.getRelative(p.getFacing());
            if (fac.getTypeId() == BlockID.CHEST || fac.getTypeId() == BlockID.TRAPPED_CHEST || fac.getTypeId() == BlockID.DROPPER || fac.getTypeId() == BlockID.DISPENSER) {

                for (ItemStack stack : ((InventoryHolder) fac.getState()).getInventory().getContents()) {

                    if (!ItemUtil.isStackValid(stack))
                        continue;

                    if(!ItemUtil.doesItemPassFilters(stack, filters, exceptions))
                        continue;

                    items.add(stack);
                    ((InventoryHolder) fac.getState()).getInventory().removeItem(stack);
                    if (CraftBookPlugin.inst().getConfiguration().pipeStackPerPull)
                        break;
                }
                visitedPipes.add(BukkitUtil.toVector(fac));
                searchNearbyPipes(block);

                if (!items.isEmpty()) {
                    for (ItemStack item : items) {
                        if (item == null) continue;
                        leftovers.addAll(((InventoryHolder) fac.getState()).getInventory().addItem(item).values());
                    }
                }
            } else if (fac.getTypeId() == BlockID.FURNACE || fac.getTypeId() == BlockID.BURNING_FURNACE) {

                Furnace f = (Furnace) fac.getState();
                if(!ItemUtil.doesItemPassFilters(f.getInventory().getResult(), filters, exceptions))
                    return;
                items.add(f.getInventory().getResult());
                if (f.getInventory().getResult() != null) f.getInventory().setResult(null);
                visitedPipes.add(BukkitUtil.toVector(fac));
                searchNearbyPipes(block);

                if (!items.isEmpty()) {
                    for (ItemStack item : items) {
                        if (item == null) continue;
                        if(f.getInventory().getResult() == null)
                            f.getInventory().setResult(item);
                        else
                            leftovers.add(ItemUtil.addToStack(f.getInventory().getResult(), item));
                    }
                } else f.getInventory().setResult(null);
            } else if (!items.isEmpty()) {
                searchNearbyPipes(block);
                if (!items.isEmpty())
                    for (ItemStack item : items) {
                        if (!ItemUtil.isStackValid(item)) continue;
                        block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), item);
                    }
            }

            if (!leftovers.isEmpty()) {
                for (ItemStack item : leftovers) {
                    if (!ItemUtil.isStackValid(item)) continue;
                    block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), item);
                }
            }
        }
    }

    /**
     * Raised when an input redstone current changes.
     */
    @Override
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event){

        startPipe(event.getBlock());
    }
}