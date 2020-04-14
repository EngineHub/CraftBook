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
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.ICVerificationException;
import com.sk89q.craftbook.mechanics.ic.PipeInputIC;
import com.sk89q.craftbook.mechanics.pipe.PipePutEvent;
import com.sk89q.craftbook.mechanics.pipe.PipeRequestEvent;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SignUtil;

public class Distributer extends AbstractSelfTriggeredIC implements PipeInputIC {

    public Distributer(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    Block chestBlock;
    int right, left;
    int currentIndex;

    @Override
    public void load() {

        try {

            currentIndex = Integer.parseInt(getLine(3));
        }
        catch(Exception e) {
            currentIndex = -1;
        }
        left = Integer.parseInt(RegexUtil.COLON_PATTERN.split(getLine(2))[0]);
        right = Integer.parseInt(RegexUtil.COLON_PATTERN.split(getLine(2))[1]);
        chestBlock = getBackBlock().getRelative(0, 1, 0);
    }

    @Override
    public String getTitle() {

        return "Distributer";
    }

    @Override
    public String getSignTitle() {

        return "DISTRIBUTER";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) chip.setOutput(0, distribute());
    }

    @Override
    public void think(ChipState state) {

        state.setOutput(0, distribute());
    }

    public boolean distribute() {

        boolean returnValue = false;

        for (Item item : ItemUtil.getItemsAtBlock(CraftBookBukkitUtil.toSign(getSign()).getBlock())) {
            if(distributeItemStack(item.getItemStack())) {
                item.remove();
                returnValue = true;
            }
        }
        return returnValue;
    }

    public boolean distributeItemStack(ItemStack item) {

        BlockFace back = SignUtil.getBack(CraftBookBukkitUtil.toSign(getSign()).getBlock());
        Block b;

        if (goRight())
            b = SignUtil.getRightBlock(CraftBookBukkitUtil.toSign(getSign()).getBlock()).getRelative(back);
        else
            b = SignUtil.getLeftBlock(CraftBookBukkitUtil.toSign(getSign()).getBlock()).getRelative(back);

        PipeRequestEvent event = new PipeRequestEvent(b, new ArrayList<>(Collections.singletonList(item)), getBackBlock());
        Bukkit.getPluginManager().callEvent(event);

        for(ItemStack it : event.getItems())
            b.getWorld().dropItemNaturally(b.getLocation().add(0.5, 0.5, 0.5), it);

        return true;
    }

    public boolean goRight() {

        currentIndex++;
        getSign().setLine(3, String.valueOf(currentIndex));
        if (currentIndex >= left && currentIndex < left+right)
            return true;
        else if (currentIndex < left)
            return false;
        else {
            currentIndex = 0;
            getSign().setLine(3, String.valueOf(currentIndex));
        }
        return false;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new Distributer(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Distributes items to right and left based on sign.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"left quantity:right quantity", "Current distribution status"};
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {
            try {
                Integer.parseInt(RegexUtil.COLON_PATTERN.split(sign.getLine(2))[0]);
                Integer.parseInt(RegexUtil.COLON_PATTERN.split(sign.getLine(2))[1]);
            } catch(ArrayIndexOutOfBoundsException e) {
                throw new ICVerificationException("You need to specify both left and right quantities!");
            } catch(NumberFormatException e) {
                throw new ICVerificationException("Invalid quantities!");
            }
        }
    }

    @Override
    public void onPipeTransfer(PipePutEvent event) {

        List<ItemStack> leftovers = new ArrayList<>();

        for (ItemStack item : event.getItems())
            if (ItemUtil.isStackValid(item))
                if(!distributeItemStack(item))
                    leftovers.add(item);

        event.setItems(leftovers);
    }
}
