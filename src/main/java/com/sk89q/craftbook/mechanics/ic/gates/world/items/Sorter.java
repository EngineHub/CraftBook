package com.sk89q.craftbook.mechanics.ic.gates.world.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.PipeInputIC;
import com.sk89q.craftbook.mechanics.pipe.PipePutEvent;
import com.sk89q.craftbook.mechanics.pipe.PipeRequestEvent;
import com.sk89q.craftbook.util.InventoryUtil;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SignUtil;

public class Sorter extends AbstractSelfTriggeredIC implements PipeInputIC {

    public Sorter(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    Block chestBlock;
    boolean inverted;
    boolean ignoreDurability;
    boolean ignoreEnchants;
    boolean ignoreMeta;

    @Override
    public void load() {

        chestBlock = getBackBlock().getRelative(0, 1, 0);
        inverted = getSign().getLine(2).equalsIgnoreCase("invert");

        for (String line4 : RegexUtil.PIPE_PATTERN.split(getSign().getLine(3))) {
            if (line4.equalsIgnoreCase("!D"))
                ignoreDurability = true;
            if (line4.equalsIgnoreCase("!E"))
                ignoreEnchants = true;
            if (line4.equalsIgnoreCase("!M"))
                ignoreMeta = true;
        }
    }

    @Override
    public String getTitle() {

        return "Sorter";
    }

    @Override
    public String getSignTitle() {

        return "SORTER";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) chip.setOutput(0, sort());
    }

    @Override
    public void think(ChipState state) {

        state.setOutput(0, sort());
    }

    public boolean sort() {

        boolean returnValue = false;

        for (Item item : ItemUtil.getItemsAtBlock(CraftBookBukkitUtil.toSign(getSign()).getBlock())) {
            if(sortItemStack(item.getItemStack())) {
                item.remove();
                returnValue = true;
            }
        }
        return returnValue;
    }

    public boolean sortItemStack(final ItemStack item) {

        BlockFace back = SignUtil.getBack(CraftBookBukkitUtil.toSign(getSign()).getBlock());
        Block b;

        if (isInAboveContainer(item) ^ inverted)
            b = SignUtil.getRightBlock(CraftBookBukkitUtil.toSign(getSign()).getBlock()).getRelative(back);
        else
            b = SignUtil.getLeftBlock(CraftBookBukkitUtil.toSign(getSign()).getBlock()).getRelative(back);

        PipeRequestEvent event = new PipeRequestEvent(b, new ArrayList<>(Collections.singletonList(item)), getBackBlock());
        Bukkit.getPluginManager().callEvent(event);

        for(ItemStack it : event.getItems())
            b.getWorld().dropItemNaturally(b.getLocation().add(0.5, 0.5, 0.5), it);

        return true;
    }

    public boolean isInAboveContainer(ItemStack item) {
        ItemStack itemClone = item.clone();
        itemClone.setAmount(1);
        return InventoryUtil.doesBlockHaveInventory(chestBlock) && InventoryUtil.doesInventoryContain(((InventoryHolder) chestBlock.getState()).getInventory(), true, ignoreDurability, ignoreMeta, ignoreEnchants, itemClone);
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new Sorter(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Sorts items and spits out left/right depending on above chest.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"invert - to invert output sides", null};
        }
    }

    @Override
    public void onPipeTransfer(PipePutEvent event) {

        List<ItemStack> leftovers = new ArrayList<>();

        for (ItemStack item : event.getItems())
            if (ItemUtil.isStackValid(item))
                if(!sortItemStack(item))
                    leftovers.add(item);

        event.setItems(leftovers);
    }
}