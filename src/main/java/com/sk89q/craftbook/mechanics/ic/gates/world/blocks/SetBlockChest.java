package com.sk89q.craftbook.mechanics.ic.gates.world.blocks;

import com.google.common.collect.Lists;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.ConfigurableIC;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.ICVerificationException;
import com.sk89q.craftbook.util.BlockSyntax;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.blocks.Blocks;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.List;

/**
 * @author Me4502
 */
public class SetBlockChest extends SetBlock {

    public SetBlockChest(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Set Block Chest";
    }

    @Override
    public String getSignTitle() {

        return "SET BLOCK CHEST";
    }

    @Override
    protected void doSet(Block body, BlockStateHolder item, boolean force) {

        if(Blocks.containsFuzzy(((Factory)getFactory()).blockBlacklist, item))
            return;

        BlockFace toPlace = ((Factory)getFactory()).above ? BlockFace.UP : BlockFace.DOWN;
        BlockFace chest = !((Factory)getFactory()).above ? BlockFace.UP : BlockFace.DOWN;

        if (force || body.getRelative(toPlace).getType() == Material.AIR) {
            if (takeFromChest(body.getRelative(chest), item.getBlockType().getItemType())) {
                body.getRelative(toPlace).setBlockData(BukkitAdapter.adapt(item));
            }
        }
    }

    public static class Factory extends AbstractICFactory implements ConfigurableIC {

        boolean above;

        public List<BaseBlock> blockBlacklist;

        public Factory(Server server, boolean above) {

            super(server);
            this.above = above;
        }

        @Override
        public IC create(ChangedSign sign) {

            return new SetBlockChest(getServer(), sign, this);
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            if(sign.getLine(2) == null || sign.getLine(2).isEmpty())
                throw new ICVerificationException("A block must be provided on line 2!");
            BlockStateHolder item = BlockSyntax.getBlock(sign.getLine(2));
            if(item == null || !item.getBlockType().hasItemType())
                throw new ICVerificationException("An invalid block was provided on line 2!");
            if(Blocks.containsFuzzy(blockBlacklist, item))
                throw new ICVerificationException("A blacklisted block was provided on line 2!");
        }

        @Override
        public String getShortDescription() {

            return "Sets " + (above ? "above" : "below") + " block from " + (!above ? "above" : "below") + " chest.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"id{:data}", "+oFORCE if it should force"};
        }

        @Override
        public void addConfiguration (YAMLProcessor config, String path) {

            config.setComment(path + "blacklist", "Stops the IC from placing the listed blocks.");
            blockBlacklist = BlockSyntax.getBlocks(config.getStringList(path + "blacklist", Lists.newArrayList(BlockTypes.BEDROCK.getId())), true);
        }
    }
}
