package com.sk89q.craftbook.circuits.gates.world.blocks;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.ICVerificationException;
import com.sk89q.craftbook.util.SearchArea;

public class Irrigator extends AbstractSelfTriggeredIC {

    public Irrigator(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    SearchArea area;

    @Override
    public void load() {

        area = SearchArea.createArea(getLocation().getBlock(), getLine(2));
    }

    @Override
    public String getTitle() {

        return "Irrigator";
    }

    @Override
    public String getSignTitle() {

        return "IRRIGATOR";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) chip.setOutput(0, irrigate());
    }

    @Override
    public void think(ChipState chip) {

        if(chip.getInput(0)) return;

        for(int i = 0; i < 10; i++)
            chip.setOutput(0, irrigate());
    }

    public boolean irrigate() {

        Block b = area.getRandomBlockInArea();

        if(b == null) return false;

        if (b.getType() == Material.SOIL && b.getData() < 0x1) {
            if (consumeWater()) {
                b.setData((byte) 0x8, false);
                return true;
            }
        }
        return false;
    }

    public boolean consumeWater() {

        Block chest = getBackBlock().getRelative(0, 1, 0);
        if (chest.getType() == Material.CHEST) {
            Chest c = (Chest) chest.getState();
            HashMap<Integer, ItemStack> over = c.getInventory().removeItem(new ItemStack(Material.WATER, 1));
            if (over.isEmpty()) return true;
            over = c.getInventory().removeItem(new ItemStack(Material.STATIONARY_WATER, 1));
            if (over.isEmpty()) return true;
            over = c.getInventory().removeItem(new ItemStack(Material.WATER_BUCKET, 1));
            if (over.isEmpty()) {
                c.getInventory().addItem(new ItemStack(Material.BUCKET, 1));
                return true;
            }
        } else if (chest.getType() == Material.WATER || chest.getType() == Material.STATIONARY_WATER) {
            chest.setType(Material.AIR);
            return true;
        }

        return false;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new Irrigator(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Irrigates nearby farmland using water in above chest.";
        }

        @Override
        public String[] getLongDescription() {

            return new String[]{
                    "The '''MC1238''' irrigates soil in the selected search area with water found in the chest above the IC.",
                    "This IC is part of the Farming IC family, and can be used to make a fully automated farm."
            };
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"+oSearchArea", null};
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {
            if(!SearchArea.isValidArea(BukkitUtil.toSign(sign).getBlock(), sign.getLine(2)))
                throw new ICVerificationException("Invalid SearchArea on 3rd line!");
        }
    }
}