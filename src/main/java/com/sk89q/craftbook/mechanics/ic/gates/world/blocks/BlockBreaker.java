package com.sk89q.craftbook.mechanics.ic.gates.world.blocks;

import com.google.common.collect.Lists;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.ConfigurableIC;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.ICVerificationException;
import com.sk89q.craftbook.util.BlockSyntax;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.blocks.Blocks;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.List;

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

    private Block broken;
    private BaseBlock item;

    @Override
    public void load() {
        item = BlockSyntax.getBlock(getLine(2), true);
    }

    public boolean breakBlock() {

        boolean above = ((Factory) getFactory()).above;
        if (broken == null) {
            Block bl = getBackBlock();

            if (above) {
                broken = bl.getRelative(0, 1, 0);
            } else {
                broken = bl.getRelative(0, -1, 0);
            }
        }

        BlockData brokenData = broken.getBlockData();

        if (broken.getType() == Material.AIR || broken.getType() == Material.MOVING_PISTON || Blocks
                .containsFuzzy(((Factory) getFactory()).blockBlacklist, BukkitAdapter.adapt(brokenData))) {
            return false;
        }

        if (item == null || item.equalsFuzzy(BukkitAdapter.adapt(brokenData))) {
            ICUtil.collectItem(this, above ? BlockVector3.at(0, -1, 0) : BlockVector3.at(0, 1, 0), BlockUtil.getBlockDrops(broken, null));
            broken.setType(Material.AIR);
        }

        return true;
    }

    public static class Factory extends AbstractICFactory implements ConfigurableIC {

        boolean above;

        List<BaseBlock> blockBlacklist;

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
                BaseBlock item = BlockSyntax.getBlock(sign.getLine(2), true);
                if(item == null)
                    throw new ICVerificationException("An invalid block was provided on line 2!");
                if(Blocks.containsFuzzy(blockBlacklist, item))
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
            blockBlacklist = BlockSyntax.getBlocks(config.getStringList(path + "blacklist", Lists.newArrayList(BlockTypes.BEDROCK.getId())), true);
        }
    }
}