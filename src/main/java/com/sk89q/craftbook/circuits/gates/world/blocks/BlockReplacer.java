package com.sk89q.craftbook.circuits.gates.world.blocks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractIC;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.ICVerificationException;
import com.sk89q.craftbook.circuits.ic.RestrictedIC;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SignUtil;

public class BlockReplacer extends AbstractIC {

    public BlockReplacer (Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    @Override
    public void trigger (ChipState chip) {

        chip.setOutput(0, replaceBlocks(chip.getInput(0)));
    }

    int onId;
    byte onData;

    int offId;
    byte offData;

    int delay;
    int mode;
    boolean physics;

    @Override
    public void load() {

        String[] ids = RegexUtil.MINUS_PATTERN.split(getLine(2));

        String[] onIds = RegexUtil.COLON_PATTERN.split(ids[0]);
        onId = Integer.parseInt(onIds[0]);
        if(onIds.length > 1)
            onData = Byte.parseByte(onIds[1]);
        else
            onData = -1;

        String[] offIds = RegexUtil.COLON_PATTERN.split(ids[1]);
        offId = Integer.parseInt(offIds[0]);
        if(offIds.length > 1)
            offData = Byte.parseByte(offIds[1]);
        else
            offData = -1;

        String[] data = RegexUtil.COLON_PATTERN.split(getLine(3));
        delay = Integer.parseInt(data[0]);
        if(data.length > 1)
            mode = Integer.parseInt(data[1]);
        else
            mode = 0;
        if(data.length > 2)
            physics = data[2].equalsIgnoreCase("1");
        else
            physics = true;
    }

    public boolean replaceBlocks(final boolean on, final Block block, final List<Location> traversedBlocks) {

        if(traversedBlocks.size() > 15000)
            return true;

        if(mode == 0) {
            for(BlockFace f : BlockFace.values()) {

                final Block b = block.getRelative(f);

                if(traversedBlocks.contains(b.getLocation()))
                    continue;
                traversedBlocks.add(b.getLocation());

                if(b.getTypeId() == onId && (onData == -1 || onData == b.getData())) {

                    if(!on)
                        b.setTypeIdAndData(offId, offData == -1 ? 0 : offData, physics);
                    Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new Runnable() {

                        @Override
                        public void run () {
                            replaceBlocks(on, b, traversedBlocks);
                        }
                    }, delay);
                } else if (b.getTypeId() == offId && (offData == -1 || offData == b.getData())) {

                    if(on)
                        b.setTypeIdAndData(onId, onData == -1 ? 0 : onData, physics);
                    Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new Runnable() {

                        @Override
                        public void run () {
                            replaceBlocks(on, b, traversedBlocks);
                        }
                    }, delay);
                }
            }
        }

        return traversedBlocks.size() > 0;
    }

    public boolean replaceBlocks(boolean on) {

        Block block = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock());
        if(block.getTypeId() == onId && (onData == -1 || onData == block.getData())) {
            if(!on)
                block.setTypeIdAndData(offId, offData == -1 ? 0 : offData, physics);
        }
        else if (block.getTypeId() == offId && (offData == -1 || offData == block.getData()))
            if(on)
                block.setTypeIdAndData(onId, onData == -1 ? 0 : onData, physics);
        List<Location> traversedBlocks = new ArrayList<Location>();
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

            String[] lines = new String[] {"onID{:onData}-offID{:offData}}", "delay{:mode:physics}"};
            return lines;
        }
    }
}