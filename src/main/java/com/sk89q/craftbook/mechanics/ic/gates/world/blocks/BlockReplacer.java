package com.sk89q.craftbook.mechanics.ic.gates.world.blocks;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.mechanics.ic.AbstractIC;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.ICVerificationException;
import com.sk89q.craftbook.mechanics.ic.RestrictedIC;
import com.sk89q.craftbook.util.BlockSyntax;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.HashSet;
import java.util.Set;

public class BlockReplacer extends AbstractIC {

    public BlockReplacer (Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    @Override
    public void trigger (ChipState chip) {

        chip.setOutput(0, replaceBlocks(chip.getInput(0)));
    }

    private BlockStateHolder onBlock;
    private BlockStateHolder offBlock;

    int delay;
    int mode;
    boolean physics;

    @Override
    public void load() {

        String[] ids = RegexUtil.MINUS_PATTERN.split(getLine(2));

        onBlock = BlockSyntax.getBlock(ids[0], true);
        offBlock = BlockSyntax.getBlock(ids[1], true);

        String[] data = RegexUtil.COLON_PATTERN.split(getLine(3));
        delay = Integer.parseInt(data[0]);
        if(data.length > 1)
            mode = Integer.parseInt(data[1]);
        else
            mode = 0;
        physics = data.length <= 2 || data[2].equalsIgnoreCase("1");
    }

    public boolean replaceBlocks(final boolean on, final Block block, final Set<Location> traversedBlocks) {

        if(traversedBlocks.size() > 15000)
            return true;

        if(mode == 0) {
            for(BlockFace f : LocationUtil.getDirectFaces()) {

                final Block b = block.getRelative(f);

                if(traversedBlocks.contains(b.getLocation()))
                    continue;
                traversedBlocks.add(b.getLocation());

                BlockState bState = BukkitAdapter.adapt(b.getBlockData());

                if(onBlock.equalsFuzzy(bState)) {
                    if(!on) {
                        b.setBlockData(BukkitAdapter.adapt(offBlock), physics);
                    }
                    Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), () -> replaceBlocks(on, b, traversedBlocks), delay);
                } else if (offBlock.equalsFuzzy(bState)) {
                    if(on) {
                        b.setBlockData(BukkitAdapter.adapt(onBlock), physics);
                    }
                    Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), () -> replaceBlocks(on, b, traversedBlocks), delay);
                }
            }
        }

        return traversedBlocks.size() > 0;
    }

    public boolean replaceBlocks(boolean on) {
        Block block = getBackBlock();
        BlockState blockState = BukkitAdapter.adapt(block.getBlockData());

        if(onBlock.equalsFuzzy(blockState)) {
            if(!on) {
                block.setBlockData(BukkitAdapter.adapt(offBlock), physics);
            }
        }
        else if (offBlock.equalsFuzzy(blockState))
            if(on) {
                block.setBlockData(BukkitAdapter.adapt(onBlock), physics);
            }
        Set<Location> traversedBlocks = new HashSet<>();
        traversedBlocks.add(block.getLocation());
        return replaceBlocks(on, block, traversedBlocks);
    }

    @Override
    public String getTitle () {
        return "Block Replacer";
    }

    @Override
    public String getSignTitle () {
        return "BLOCK REPLACER";
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new BlockReplacer(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Searches a nearby area and replaces blocks accordingly.";
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            String[] ids = RegexUtil.MINUS_PATTERN.split(sign.getLine(2));

            String[] onIds = RegexUtil.COLON_PATTERN.split(ids[0]);
            try {
                Integer.parseInt(onIds[0]);
            }
            catch(Exception e){
                throw new ICVerificationException("Must provide an on ID!");
            }
            try {
                if(onIds.length > 1)
                    Byte.parseByte(onIds[1]);
            }
            catch(Exception e){
                throw new ICVerificationException("Invalid on Data!");
            }

            String[] offIds = RegexUtil.COLON_PATTERN.split(ids[1]);
            try {
                Integer.parseInt(offIds[0]);
            }
            catch(Exception e){
                throw new ICVerificationException("Must provide an off ID!");
            }
            try {
                if(offIds.length > 1)
                    Byte.parseByte(offIds[1]);
            }
            catch(Exception e){
                throw new ICVerificationException("Invalid off Data!");
            }

            String[] data = RegexUtil.COLON_PATTERN.split(sign.getLine(3));
            try {
                Integer.parseInt(data[0]);
            }
            catch(Exception e){
                throw new ICVerificationException("Must provide a delay!");
            }
            try {
                if(data.length > 1)
                    Integer.parseInt(data[1]);
            }
            catch(Exception e) {
                throw new ICVerificationException("Invalid mode!");
            }
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"onID{:onData}-offID{:offData}}", "delay{:mode:physics}"};
        }
    }
}