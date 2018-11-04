package com.sk89q.craftbook.mechanics.ic.gates.world.blocks;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.ConfigurableIC;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.ICVerificationException;
import com.sk89q.craftbook.mechanics.ic.RestrictedIC;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.InventoryUtil;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class Driller extends AbstractSelfTriggeredIC {

    public Driller (Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    @Override
    public void think (ChipState chip) {

        if (!chip.getInput(0)) chip.setOutput(0, drill());
    }

    @Override
    public String getTitle () {

        return "Driller";
    }

    @Override
    public String getSignTitle () {

        return "DRILLER";
    }

    private int signDrillSize;
    private int signMaxDepth;

    @Override
    public void load() {
        super.load();

        signDrillSize = ((Factory) getFactory()).drillSize;

        if (!getSign().getLine(2).isEmpty()) {
            signDrillSize = Math.min(signDrillSize, Integer.parseInt(getSign().getLine(2)));
        }

        signMaxDepth = ((Factory) getFactory()).maxDrillDepth;

        if (!getSign().getLine(3).isEmpty()) {
            signMaxDepth = Math.min(signMaxDepth, Integer.parseInt(getSign().getLine(3)));
        }
    }

    public boolean drill() {

        if (CraftBookPlugin.inst().getRandom().nextInt(100) < 60) return false;

        Block center = getBackBlock().getRelative(0, -1, 0);
        ItemStack tool = null;

        if (InventoryUtil.doesBlockHaveInventory(center.getRelative(0, 2, 0))) {
            InventoryHolder holder = (InventoryHolder) center.getRelative(0, 2, 0).getState();
            if (holder.getInventory().getItem(0) != null) {
                tool = holder.getInventory().getItem(0);
            }
        }

        int random = CraftBookPlugin.inst().getRandom().nextInt(signDrillSize*signDrillSize);
        int x = random / signDrillSize;
        int y = random % signDrillSize;

        return drillLine(tool, center.getRelative(signDrillSize/2 - x, 0, signDrillSize/2 - y));
    }

    public boolean drillLine(ItemStack tool, Block blockToBreak) {

        Material brokenType = Material.AIR;
        int depth = 0;
        while (brokenType == Material.AIR) {

            if (blockToBreak.getLocation().getBlockY() == 0 || depth > signMaxDepth) return false;
            blockToBreak = blockToBreak.getRelative(0, -1, 0);
            depth += 1;
            brokenType = blockToBreak.getType();
            if (brokenType == Material.BEDROCK) return false;
        }

        ICUtil.collectItem(this, BlockVector3.at(0, 1, 0), BlockUtil.getBlockDrops(blockToBreak, tool));

        brokenType = blockToBreak.getType();
        blockToBreak.setType(Material.AIR);

        return !(brokenType == Material.LAVA || brokenType == Material.WATER);

    }

    @Override
    public void trigger (ChipState chip) {

        if (chip.getInput(0)) chip.setOutput(0, drill());
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC, ConfigurableIC {

        int drillSize;
        int maxDrillDepth;

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new Driller(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Breaks a line of blocks from the IC block.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"+odrill size", "+omax depth"};
        }

        @Override
        public void addConfiguration(YAMLProcessor config, String path) {

            drillSize = config.getInt(path + "drill-size", 3);
            maxDrillDepth = config.getInt(path + "max-drill-depth", 256);
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            try {
                if (!sign.getLine(2).isEmpty()) {
                    sign.setLine(2, String.valueOf(Math.min(drillSize, Integer.parseInt(sign.getLine(2)))));
                }
                if (!sign.getLine(3).isEmpty()) {
                    sign.setLine(3, String.valueOf(Math.min(maxDrillDepth, Integer.parseInt(sign.getLine(3)))));
                }
            } catch (Exception e) {
                throw new ICVerificationException("Failed to parse numbers.");
            }
        }

    }
}