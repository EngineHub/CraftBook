package com.sk89q.craftbook.mechanics.ic.gates.world.blocks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.mechanics.ic.*;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.Vector;

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

        if (broken == null) {

            Block bl = getBackBlock();

            if (((Factory)getFactory()).above) {
                broken = bl.getRelative(0, -1, 0);
            } else {
                broken = bl.getRelative(0, 1, 0);
            }
        }

        if (broken == null || broken.getType() == Material.AIR || broken.getType() == Material.PISTON_MOVING_PIECE || ((Factory)getFactory()).blockBlacklist.contains(new ItemInfo(broken)))
            return false;

        if (item.getType() != broken.getType()) return false;

        if (item.getData() > 0 && item.getData() != broken.getData()) return false;

        ICUtil.collectItem(this, new Vector(0, 1, 0), BlockUtil.getBlockDrops(broken, null));
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