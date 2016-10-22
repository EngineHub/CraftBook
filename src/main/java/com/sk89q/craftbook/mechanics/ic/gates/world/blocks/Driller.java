package com.sk89q.craftbook.mechanics.ic.gates.world.blocks;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.mechanics.ic.*;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.InventoryUtil;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockType;

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

    public boolean drill() {

		int signDrillSize;
		
        if (CraftBookPlugin.inst().getRandom().nextInt(100) < 60) return false;

		signDrillSize = ((Factory) getFactory()).drillSize;
		if (!getSign().getLine(2).isEmpty()) { 
			signDrillSize = Math.min(signDrillSize, Integer.parseInt(getSign().getLine(2)));
		}
		
        Block center = getBackBlock().getRelative(0, -1, 0);
        ItemStack tool = null;

        if (InventoryUtil.doesBlockHaveInventory(center.getRelative(0, 2, 0))) {
            InventoryHolder holder = (InventoryHolder) center.getRelative(0, 2, 0).getState();
            if (holder.getInventory().getItem(0) != null) {
                tool = holder.getInventory().getItem(0);
            }
        }

        boolean hasHadTrue;

		int random = CraftBookPlugin.inst().getRandom().nextInt(signDrillSize*signDrillSize);
		int x = random / signDrillSize;
		int y = random % signDrillSize;
		hasHadTrue = drillLine(tool, center.getRelative(signDrillSize/2 - x, 0, signDrillSize/2 - y));

        return hasHadTrue;
    }

    public boolean drillLine(ItemStack tool, Block blockToBreak) {

        Material brokenType = Material.AIR;
        while (brokenType == Material.AIR) {

            if (blockToBreak.getLocation().getBlockY() == 0) return false;
            blockToBreak = blockToBreak.getRelative(0, -1, 0);
            brokenType = blockToBreak.getType();
            if (brokenType == Material.BEDROCK) return false;
            if (!((Factory)getFactory()).breakNonNatural)
                if (brokenType != Material.AIR && !BlockType.isNaturalTerrainBlock(brokenType.getId())) return false;
        }

        ICUtil.collectItem(this, new Vector(0, 1, 0), BlockUtil.getBlockDrops(blockToBreak, tool));

        brokenType = blockToBreak.getType();
        blockToBreak.setType(Material.AIR);

        return !(brokenType == Material.LAVA || brokenType == Material.WATER || brokenType == Material.STATIONARY_LAVA || brokenType == Material.STATIONARY_WATER);

    }

    @Override
    public void trigger (ChipState chip) {

        if (chip.getInput(0)) chip.setOutput(0, drill());
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC, ConfigurableIC {

        boolean breakNonNatural;
		int drillSize;

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

            return new String[] {null, null};
        }

        @Override
        public void addConfiguration(YAMLProcessor config, String path) {

            breakNonNatural = config.getBoolean(path + "break-unnatural-blocks", false);
			drillSize = config.getInt(path + "drill-size", 3);
        }
		
		@Override
		public void verify(ChangedSign sign) throws ICVerificationException {
			
			if (!sign.getLine(2).isEmpty()) {
				sign.setLine(2, String.valueOf(Math.min(drillSize,Integer.parseInt(sign.getLine(2)))));
			}
		}
		
    }
}