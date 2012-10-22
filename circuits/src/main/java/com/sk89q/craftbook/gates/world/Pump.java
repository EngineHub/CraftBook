package com.sk89q.craftbook.gates.world;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.util.SignUtil;

/**
 * @author Me4502
 */
public class Pump extends AbstractIC {

    public Pump(Server server, Sign block, ICFactory factory) {

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

        Block pump = SignUtil.getBackBlock(getSign().getBlock());
        if (!(pump.getRelative(0, 1, 0).getType() == Material.CHEST))
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
            if (c.getInventory().addItem(new ItemStack(parse(liquid.getType()), 1)).size() == 0) {
                liquid.setTypeId(0);
                return true;
            }
        } else if (searchNear(c, liquid, depth + 1))
            return true;
        return false;
    }

    public Material parse(Material mat) {

        if (mat == Material.STATIONARY_WATER || mat == Material.WATER)
            return Material.WATER;
        if (mat == Material.STATIONARY_LAVA || mat == Material.LAVA)
            return Material.LAVA;
        return Material.AIR;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

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
    }
}