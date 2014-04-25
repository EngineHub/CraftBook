package com.sk89q.craftbook.circuits.gates.world.blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.ConfigurableIC;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.ICVerificationException;
import com.sk89q.craftbook.circuits.pipe.PipeRequestEvent;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.util.yaml.YAMLProcessor;

public class BlockBreaker extends AbstractSelfTriggeredIC {

    public BlockBreaker(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Block Breaker";
    }

    @Override
    public String getSignTitle() {

        return "BLOCK BREAK";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            chip.setOutput(0, breakBlock());
        }
    }

    @Override
    public void think(ChipState state) {

        state.setOutput(0, breakBlock());
    }

    Block broken, chest;

    ItemInfo item;

    @Override
    public void load() {

        item = new ItemInfo(getLine(2));
    }

    public boolean breakBlock() {

        if (chest == null || broken == null) {

            Block bl = getBackBlock();

            if (((Factory)getFactory()).above) {
                chest = bl.getRelative(0, 1, 0);
                broken = bl.getRelative(0, -1, 0);
            } else {
                chest = bl.getRelative(0, -1, 0);
                broken = bl.getRelative(0, 1, 0);
            }
        }

        boolean hasChest = false;
        if (chest != null && chest.getState() instanceof InventoryHolder)
            hasChest = true;

        if (broken == null || broken.getType() == Material.AIR || broken.getType() == Material.PISTON_MOVING_PIECE || ((Factory)getFactory()).blockBlacklist.contains(new ItemInfo(broken)))
            return false;

        if (item.getType() != broken.getType()) return false;

        if (item.getData() > 0 && item.getData() != broken.getData()) return false;

        for (ItemStack stack : BlockUtil.getBlockDrops(broken, null)) {

            BlockFace back = SignUtil.getBack(BukkitUtil.toSign(getSign()).getBlock());
            Block pipe = getBackBlock().getRelative(back);

            PipeRequestEvent event = new PipeRequestEvent(pipe, new ArrayList<ItemStack>(Arrays.asList(stack)), getBackBlock());
            Bukkit.getPluginManager().callEvent(event);

            if(!event.isValid())
                continue;

            for(ItemStack blockstack : event.getItems()) {
                if (hasChest) {
                    InventoryHolder c = (InventoryHolder) chest.getState();
                    HashMap<Integer, ItemStack> overflow = c.getInventory().addItem(blockstack);
                    ((BlockState) c).update();
                    if (overflow.isEmpty()) continue;
                    else {
                        for (Map.Entry<Integer, ItemStack> bit : overflow.entrySet()) {
                            dropItem(bit.getValue());
                        }
                        continue;
                    }
                }

                dropItem(blockstack);
            }
        }
        broken.setType(Material.AIR);

        return true;
    }

    public void dropItem(ItemStack item) {

        BukkitUtil.toSign(getSign()).getWorld().dropItem(BlockUtil.getBlockCentre(BukkitUtil.toSign(getSign()).getBlock()), item);
    }

    public static class Factory extends AbstractICFactory implements ConfigurableIC {

        boolean above;

        @SuppressWarnings("serial")
        public List<ItemInfo> blockBlacklist = new ArrayList<ItemInfo>(){{
            add(new ItemInfo(Material.BEDROCK, -1));
        }};

        public Factory(Server server, boolean above) {

            super(server);
            this.above = above;
        }

        @Override
        public IC create(ChangedSign sign) {

            return new BlockBreaker(getServer(), sign, this);
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            if(!sign.getLine(2).trim().isEmpty()) {
                ItemInfo item = new ItemInfo(sign.getLine(2));
                if(item.getType() == null)
                    throw new ICVerificationException("An invalid block was provided on line 2!");
                if(blockBlacklist.contains(item))
                    throw new ICVerificationException("A blacklisted block was provided on line 2!");
            }
        }

        @Override
        public String getShortDescription() {

            return "Breaks blocks " + (above ? "above" : "below") + " block sign is on.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"+oBlock ID:Data", null};
        }

        @Override
        public void addConfiguration (YAMLProcessor config, String path) {

            config.setComment(path + "blacklist", "Stops the IC from breaking the listed blocks.");
            blockBlacklist.addAll(ItemInfo.parseListFromString(config.getStringList(path + "blacklist", ItemInfo.toStringList(blockBlacklist))));
        }
    }
}