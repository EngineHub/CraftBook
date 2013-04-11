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
    int curTicks;

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
        mode = Integer.parseInt(data[1]);
        physics = data[2].equalsIgnoreCase("1");
        try {
            curTicks = Integer.parseInt(data[3]);
        }
        catch(Exception e){}

        mode = 0; //For now.
    }

    public boolean replaceBlocks(final boolean on, final Block block, final List<Location> traversedBlocks) {

        if(curTicks < delay) {

            curTicks++;
            getSign().setLine(3, delay + ":" + mode + ":" + (physics ? "1" : "0") + ":" + curTicks);
            getSign().update(false);
        } else {

            curTicks = 0;
            getSign().setLine(3, delay + ":" + mode + ":" + (physics ? "1" : "0") + ":" + curTicks);
            getSign().update(false);

            if(mode == 0) {
                for(final BlockFace f : BlockFace.values()) {

                    if(traversedBlocks.contains(block.getRelative(f).getLocation()))
                        continue;
                    traversedBlocks.add(block.getRelative(f).getLocation());

                    if(block.getRelative(f).getTypeId() == onId && (onData == -1 || onData == block.getRelative(f).getData())) {

                        if(!on)
                            block.setTypeIdAndData(offId, offData == -1 ? 0 : offData, physics);
                        Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new Runnable() {

                            @Override
                            public void run () {
                                replaceBlocks(on, block.getRelative(f), traversedBlocks);
                            }
                        }, delay);
                    } else if (block.getRelative(f).getTypeId() == offId && (offData == -1 || offData == block.getRelative(f).getData())) {

                        if(on)
                            block.setTypeIdAndData(onId, onData == -1 ? 0 : onData, physics);
                        Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new Runnable() {

                            @Override
                            public void run () {
                                replaceBlocks(on, block.getRelative(f), traversedBlocks);
                            }
                        }, delay);
                    }
                }
            }
        }

        return false;
    }

    public boolean replaceBlocks(boolean on) {

        Block block = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock());
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
        public String[] getLineHelp() {

            String[] lines = new String[] {"onID{:onData}-offID{:offData}}", "+odelay:mode:physics:tick"};
            return lines;
        }
    }
}