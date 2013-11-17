package com.sk89q.craftbook.circuits.gates.world.blocks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.Pipes;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.ConfigurableIC;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.RestrictedIC;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.util.yaml.YAMLProcessor;
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

        if (CraftBookPlugin.inst().getRandom().nextInt(100) < 60) return false;

        Block center = getBackBlock().getRelative(0, -1, 0);
        InventoryHolder holder = null;

        if (center.getRelative(0, 2, 0).getState() instanceof InventoryHolder) {
            holder = (InventoryHolder) center.getRelative(0, 2, 0).getState();
        }

        boolean hasHadTrue;

        switch (CraftBookPlugin.inst().getRandom().nextInt(9)) {
            case 0:
                hasHadTrue = drillLine(holder, center.getRelative(-1, 0, -1));
                break;
            case 1:
                hasHadTrue = drillLine(holder, center.getRelative(-1, 0, 0));
                break;
            case 2:
                hasHadTrue = drillLine(holder, center.getRelative(-1, 0, 1));
                break;
            case 3:
                hasHadTrue = drillLine(holder, center.getRelative(0, 0, -1));
                break;
            case 4:
                hasHadTrue = drillLine(holder, center.getRelative(0, 0, 0));
                break;
            case 5:
                hasHadTrue = drillLine(holder, center.getRelative(0, 0, 1));
                break;
            case 6:
                hasHadTrue = drillLine(holder, center.getRelative(1, 0, -1));
                break;
            case 7:
                hasHadTrue = drillLine(holder, center.getRelative(1, 0, 0));
                break;
            case 8:
                hasHadTrue = drillLine(holder, center.getRelative(1, 0, 1));
                break;
            default:
                hasHadTrue = drillLine(holder, center.getRelative(0, 0, 0));
                break;
        }

        return hasHadTrue;
    }

    public boolean drillLine(InventoryHolder chest, Block blockToBreak) {

        boolean hasChest = chest != null;

        Material brokenType = Material.AIR;
        while (brokenType == Material.AIR) {

            if (blockToBreak.getLocation().getBlockY() == 0) return false;
            blockToBreak = blockToBreak.getRelative(0, -1, 0);
            brokenType = blockToBreak.getType();
            if (brokenType == Material.BEDROCK) return false;
            if (!((Factory)getFactory()).breakNonNatural)
                if (brokenType != Material.AIR && !BlockType.isNaturalTerrainBlock(brokenType.getId())) return false;
        }

        ItemStack tool = null;
        if(hasChest && chest.getInventory().getItem(0) != null)
            tool = chest.getInventory().getItem(0);

        for (ItemStack stack : BlockUtil.getBlockDrops(blockToBreak, tool)) {

            List<ItemStack> toDrop = new ArrayList<ItemStack>();
            toDrop.add(stack);

            if (hasChest) {
                toDrop = new ArrayList<ItemStack>(chest.getInventory().addItem(toDrop.toArray(new ItemStack[1])).values());
            }

            BlockFace back = SignUtil.getBack(BukkitUtil.toSign(getSign()).getBlock());
            Block pipe = getBackBlock().getRelative(back);

            if(Pipes.Factory.setupPipes(pipe, getBackBlock(), toDrop.toArray(new ItemStack[toDrop.size()])) != null)
                continue;

            if (!toDrop.isEmpty()) {
                for (ItemStack d : toDrop) {
                    BukkitUtil.toSign(getSign()).getBlock().getWorld().dropItemNaturally(BukkitUtil.toSign(getSign()).getBlock().getLocation().add(0.5, 0.5, 0.5), d);
                }
            }
        }

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
        }
    }
}