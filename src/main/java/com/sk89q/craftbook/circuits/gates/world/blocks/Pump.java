package com.sk89q.craftbook.circuits.gates.world.blocks;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.ConfigurableIC;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.util.yaml.YAMLProcessor;

/**
 * @author Me4502
 */
public class Pump extends AbstractSelfTriggeredIC {

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

    @Override
    public void think(ChipState state) {

        if (state.getInput(0)) {
            state.setOutput(0, scan());
        }
    }

    /**
     * @return water found
     */
    public boolean scan() {

        Block pump = getBackBlock();
        if (!(pump.getRelative(0, 1, 0).getType() == Material.CHEST)) return false;
        Chest c = (Chest) pump.getRelative(0, 1, 0).getState();
        for (int y = -1; y > -11; y--) {
            Block liquid = pump.getRelative(0, y, 0);
            if (check(c, liquid, 0)) return true;
        }
        return false;
    }

    public boolean searchNear(Chest c, Block block, int depth) {

        return depth <= 5 && (check(c, block.getRelative(0, 0, 1), depth) || check(c, block.getRelative(0, 0, -1), depth) || check(c, block.getRelative(1, 0, 0), depth) || check(c, block.getRelative(-1, 0, 0), depth));
    }

    public boolean check(Chest c, Block liquid, int depth) {

        if (!liquid.isLiquid()) return false;
        if (liquid.getData() == 0x0) {
            if (addToChest(c, liquid)) {
                liquid.setType(Material.AIR);
                return true;
            }
        } else if (searchNear(c, liquid, depth + 1)) return true;
        return false;
    }

    public boolean addToChest(Chest c, Block liquid) {

        if (((Factory) getFactory()).buckets) {
            if (c.getInventory().contains(Material.BUCKET)) {
                c.getInventory().remove(Material.BUCKET);
                if (c.getInventory().addItem(new ItemStack(parse(liquid.getType()) == Material.LAVA ? Material.LAVA_BUCKET : Material.WATER_BUCKET, 1)).isEmpty()) {
                    return true;
                } else {
                    c.getInventory().addItem(new ItemStack(Material.BUCKET));
                    return false;
                }
            } else return false;
        } else
            if (c.getInventory().addItem(new ItemStack(parse(liquid.getType()))).isEmpty()) return true;

        return false;
    }

    public Material parse(Material mat) {

        if (mat == Material.STATIONARY_WATER || mat == Material.WATER) return Material.WATER;
        if (mat == Material.STATIONARY_LAVA || mat == Material.LAVA) return Material.LAVA;
        return Material.AIR;
    }

    public static class Factory extends AbstractICFactory implements ConfigurableIC {

        public boolean buckets;

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new Pump(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Pumps liquids into above chest.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {null, null}; //TODO allow offsets.
        }

        @Override
        public void addConfiguration(YAMLProcessor config, String path) {

            buckets = config.getBoolean(path + "requires-buckets", false);
        }
    }
}