package com.sk89q.craftbook.gates.world;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.BaseConfiguration;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;

/**
 * @author Me4502
 */
public class Pump extends AbstractIC {

    public Pump(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Pump";
    }

    @Override
    public String getSignTitle() {

        return "PUMP";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            chip.setOutput(0, scan());
        }
    }

    /**
     * @return water found
     */
    public boolean scan() {

        Block pump = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock());
        if (!(pump.getRelative(0, 1, 0).getTypeId() == BlockID.CHEST))
            return false;
        Chest c = (Chest) pump.getRelative(0, 1, 0).getState();
        for (int y = 0; y > -10; y--) {
            Block liquid = pump.getRelative(0, y, 0);
            if (check(c, liquid, 0))
                return true;
        }
        return false;
    }

    public boolean searchNear(Chest c, Block block, int depth) {

        return depth <= 5 && (check(c, block.getRelative(0, 0, 1), depth) || check(c, block.getRelative(0, 0, -1),
                depth) || check(c, block.getRelative(1, 0, 0), depth) || check(c, block.getRelative(-1, 0, 0), depth));
    }

    public boolean check(Chest c, Block liquid, int depth) {

        if (!liquid.isLiquid())
            return false;
        if (liquid.getData() == 0x0) {
            if (addToChest(c,liquid)) {
                liquid.setTypeId(0);
                return true;
            }
        } else if (searchNear(c, liquid, depth + 1))
            return true;
        return false;
    }

    public boolean addToChest(Chest c, Block liquid) {
        if(((Factory)getFactory()).buckets) {
            if(c.getInventory().contains(ItemID.BUCKET)) {
                c.getInventory().remove(ItemID.BUCKET);
                if(c.getInventory().addItem(new ItemStack(parse(liquid.getTypeId()) == BlockID.LAVA ?
                        ItemID.LAVA_BUCKET : ItemID.WATER_BUCKET, 1)).size() < 1) {
                    return true;
                }
                else {
                    c.getInventory().addItem(new ItemStack(ItemID.BUCKET));
                    return false;
                }
            }
            else
                return false;
        }
        else {
            if(c.getInventory().addItem(new ItemStack(parse(liquid.getTypeId()))).size() < 1)
                return true;
        }

        return false;
    }

    public int parse(int mat) {

        if (mat == BlockID.STATIONARY_WATER || mat == BlockID.WATER)
            return BlockID.WATER;
        if (mat == BlockID.STATIONARY_LAVA || mat == BlockID.LAVA)
            return BlockID.LAVA;
        return BlockID.AIR;
    }

    public static class Factory extends AbstractICFactory {

        public boolean buckets;

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new Pump(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Pumps liquids into above chest.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    null,
                    null
            };
            return lines;
        }

        @Override
        public void addConfiguration(BaseConfiguration.BaseConfigurationSection section) {

            buckets = section.getBoolean("requires-buckets", false);
        }

        @Override
        public boolean needsConfiguration() {
            return true;
        }
    }
}