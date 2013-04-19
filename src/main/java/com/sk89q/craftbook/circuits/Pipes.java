package com.sk89q.craftbook.circuits;

import java.util.ArrayList;
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
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.exceptions.InvalidMechanismException;
import com.sk89q.craftbook.util.exceptions.ProcessedMechanismException;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;

public class Pipes extends AbstractMechanic {

    final CraftBookPlugin plugin = CraftBookPlugin.inst();

    public static class Factory extends AbstractMechanicFactory<Pipes> {

        public Factory() {

        }

        @Override
        public Pipes detect(BlockWorldVector pt) {

            int type = BukkitUtil.toWorld(pt).getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).getTypeId();

            if (type == BlockID.PISTON_STICKY_BASE || type == BlockID.PISTON_BASE) {

                PistonBaseMaterial piston = (PistonBaseMaterial) BukkitUtil.toBlock(pt).getState().getData();
                Sign sign = getSignOnPiston(piston, BukkitUtil.toBlock(pt));

                if (CraftBookPlugin.inst().getConfiguration().pipeRequireSign && sign == null)
                    return null;

                return new Pipes(pt, sign == null ? null : BukkitUtil.toChangedSign(sign));
            }

            return null;
        }

        public Pipes detect(BlockWorldVector pt, List<ItemStack> items) {

            int type = BukkitUtil.toWorld(pt).getBlockTypeIdAt(BukkitUtil.toLocation(pt));

            if (type == BlockID.PISTON_STICKY_BASE || type == BlockID.PISTON_BASE) {

                PistonBaseMaterial piston = (PistonBaseMaterial) BukkitUtil.toBlock(pt).getState().getData();
                Sign sign = getSignOnPiston(piston, BukkitUtil.toBlock(pt));

                if (CraftBookPlugin.inst().getConfiguration().pipeRequireSign && sign == null)
                    return null;

                return new Pipes(pt, items, sign == null ? null : BukkitUtil.toChangedSign(sign));
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

        for(BlockFace face : BlockFace.values()) {

            Sign sign = null;

            if(face == piston.getFacing() || !(block.getRelative(face).getState() instanceof Sign))
                continue;
            sign = (Sign) block.getRelative(face).getState();
            if(sign != null && sign.getLine(1).equalsIgnoreCase("[Pipe]"))
                return sign;
        }

        return null;
    }

    /**
     * Construct the mechanic for a location.
     *
     * @param pt
     */
    private Pipes(BlockWorldVector pt, ChangedSign sign) {

        super();
        scanSign(sign);
    }

    public void scanSign(ChangedSign sign) {

        if(sign != null) {

            for(String line3 : RegexUtil.COMMA_PATTERN.split(sign.getLine(2))) {

                filters.add(ICUtil.getItem(line3));
            }
            for(String line4 : RegexUtil.COMMA_PATTERN.split(sign.getLine(3))) {

                exceptions.add(ICUtil.getItem(line4));
            }
        }

        while(filters.remove(null)){}
        while(exceptions.remove(null)){}
    }

    private Pipes(BlockWorldVector pt, List<ItemStack> items, ChangedSign sign) {

        super();
        this.items.addAll(items);

        scanSign(sign);
        startPipe(BukkitUtil.toBlock(pt));
    }

    private List<ItemStack> filters = new ArrayList<ItemStack>();
    private List<ItemStack> exceptions = new ArrayList<ItemStack>();

    private List<ItemStack> items = new ArrayList<ItemStack>();
    private List<BlockVector> visitedPipes = new ArrayList<BlockVector>();

    public void searchNearbyPipes(Block block) {

        BukkitConfiguration config = CraftBookPlugin.inst().getConfiguration();

        for (int x = -1; x < 2; x++) {
            for (int y = -1; y < 2; y++) {
                for (int z = -1; z < 2; z++) {

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

                    if (off.getTypeId() == BlockID.GLASS) {

                        searchNearbyPipes(off);
                    } else if (off.getTypeId() == BlockID.PISTON_BASE) {

                        PistonBaseMaterial p = (PistonBaseMaterial) off.getState().getData();

                        Sign sign = getSignOnPiston(p, off);

                        List<ItemStack> pFilters = new ArrayList<ItemStack>();
                        List<ItemStack> pExceptions = new ArrayList<ItemStack>();

                        if(sign != null) {

                            for(String line3 : RegexUtil.COMMA_PATTERN.split(sign.getLine(2))) {
                                pFilters.add(ICUtil.getItem(line3));
                            }
                            for(String line4 : RegexUtil.COMMA_PATTERN.split(sign.getLine(3))) {
                                pExceptions.add(ICUtil.getItem(line4));
                            }
                        }

                        Block fac = off.getRelative(p.getFacing());
                        if (fac.getTypeId() == BlockID.CHEST || fac.getTypeId() == BlockID.DISPENSER) {
                            List<ItemStack> newItems = new ArrayList<ItemStack>();
                            List<ItemStack> filteredItems = ItemUtil.filterItems(items, pFilters, pExceptions);

                            for (ItemStack item : filteredItems) {
                                if (!ItemUtil.isStackValid(item)) continue;
                                newItems.addAll(((InventoryHolder) fac.getState()).getInventory().addItem(item).values());
                            }

                            items.removeAll(filteredItems);
                            items.addAll(newItems);

                            if (!items.isEmpty()) searchNearbyPipes(block);
                        } else if (fac.getTypeId() == BlockID.FURNACE || fac.getTypeId() == BlockID.BURNING_FURNACE) {

                            List<ItemStack> newItems = new ArrayList<ItemStack>();
                            List<ItemStack> filteredItems = ItemUtil.filterItems(items, pFilters, pExceptions);

                            newItems.addAll(filteredItems);
                            Furnace furnace = (Furnace) fac.getState();

                            for (ItemStack item : filteredItems) {
                                if (!ItemUtil.isStackValid(item)) continue;
                                if (ItemUtil.isAFuel(item)) {
                                    if (ItemUtil.isStackValid(furnace.getInventory().getFuel())) {

                                        if (ItemUtil.areItemsIdentical(item, furnace.getInventory().getFuel())) {

                                            newItems.remove(item);
                                            ItemStack newStack = ItemUtil.addToStack(furnace.getInventory().getFuel(),
                                                    item);
                                            if (newStack != null) newItems.add(newStack);
                                        }
                                    } else {

                                        furnace.getInventory().setFuel(item);
                                        newItems.remove(item);
                                    }
                                } else if (ItemUtil.isSmeltable(item)) {

                                    if (ItemUtil.isStackValid(furnace.getInventory().getSmelting())) {

                                        if (ItemUtil.areItemsIdentical(item, furnace.getInventory().getSmelting())) {

                                            newItems.remove(item);
                                            ItemStack newStack = ItemUtil.addToStack(furnace.getInventory()
                                                    .getSmelting(), item);
                                            if (newStack != null) newItems.add(newStack);
                                        }
                                    } else {

                                        furnace.getInventory().setSmelting(item);
                                        newItems.remove(item);
                                    }
                                }
                            }

                            items.removeAll(filteredItems);
                            items.addAll(newItems);

                            if (!items.isEmpty()) searchNearbyPipes(block);

                        } else if (fac.getTypeId() == BlockID.WALL_SIGN) {

                            CircuitCore circuitCore = CircuitCore.inst();
                            if (circuitCore.getICFactory() == null) continue;

                            try {
                                ICMechanic icmech = circuitCore.getICFactory().detect(BukkitUtil.toWorldVector(fac));
                                if (icmech == null) continue;
                                if (!(icmech.getIC() instanceof PipeInputIC)) continue;
                                List<ItemStack> filteredItems = ItemUtil.filterItems(items, pFilters, pExceptions);
                                List<ItemStack> newItems = ((PipeInputIC) icmech.getIC()).onPipeTransfer(BukkitUtil.toWorldVector(off), filteredItems);

                                items.removeAll(filteredItems);
                                items.addAll(newItems);

                                if (!items.isEmpty()) searchNearbyPipes(block);
                            } catch (Exception e) {
                                BukkitUtil.printStacktrace(e);
                            }
                        }
                    }
                }
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
            if (fac.getTypeId() == BlockID.CHEST || fac.getTypeId() == BlockID.DISPENSER) {

                for (ItemStack stack : ((InventoryHolder) fac.getState()).getInventory().getContents()) {

                    if (!ItemUtil.isStackValid(stack))
                        continue;

                    if(!doesItemPassFilters(stack))
                        continue;

                    items.add(stack.clone());
                    ((InventoryHolder) fac.getState()).getInventory().remove(stack);
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
                if(!doesItemPassFilters(f.getInventory().getResult()))
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

    public boolean doesItemPassFilters(ItemStack stack) {

        if(!ItemUtil.isStackValid(stack))
            return false;
        boolean passesFilters = true;
        if(filters.size() > 0) {
            for (ItemStack fil : filters) {

                if(!ItemUtil.isStackValid(fil))
                    continue;
                passesFilters = false;
                if(ItemUtil.areItemsIdentical(fil, stack)) {
                    passesFilters = true;
                    break;
                }
            }
            if(!passesFilters)
                return false;
        }
        if(exceptions.size() > 0) {
            for (ItemStack fil : exceptions) {

                if(!ItemUtil.isStackValid(fil))
                    continue;
                if(ItemUtil.areItemsIdentical(fil, stack)) {
                    passesFilters = false;
                    break;
                }
            }
            if(!passesFilters)
                return false;
        }

        return passesFilters;
    }

    /**
     * Raised when an input redstone current changes.
     */
    @Override
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {

        startPipe(event.getBlock());
    }
}