package com.sk89q.craftbook.gates.world;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
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
import com.sk89q.worldedit.blocks.BlockID;

public class Spigot extends AbstractIC {

    int radius = 15;
    int yOffset = 1;

    public Spigot(Server server, Sign block, ICFactory factory) {

        super(server, block, factory);
        load();
    }

    public void load() {

        try {
            radius = Integer.parseInt(getSign().getLine(2));
        } catch (Exception ignored) {
        }

        try {
            yOffset = Integer.parseInt(getSign().getLine(3));
        } catch (Exception ignored) {
        }
    }

    @Override
    public String getTitle() {

        return "Spigot";
    }

    @Override
    public String getSignTitle() {

        return "SPIGOT";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            chip.setOutput(0, search());
        }
    }

    public boolean search() {

        Block off = SignUtil.getBackBlock(getSign().getBlock()).getRelative(0, yOffset, 0);
        ArrayList<Location> searched = new ArrayList<Location>();
        return searchAt(searched, off);
    }

    public boolean searchAt(ArrayList<Location> searched, Block off) {

        if (searched.contains(off.getLocation()))
            return false;
        searched.add(off.getLocation());
        if (off.getLocation().distanceSquared(SignUtil.getBackBlock(getSign().getBlock().getRelative(0, yOffset,
                0)).getLocation()) > radius * radius)
            return false;
        if (off.getTypeId() == 0) {

            Material m = getFromChest();
            if (m == Material.AIR)
                return false;
            off.setType(m);
            return true;
        } else if (off.isLiquid()) if (off.getData() != 0x0) { //Moving

            Material m = getFromChest(off.getType());
            if (m == Material.AIR)
                return false;
            off.setType(m);
            return true;
        } else { //Still

            if (searchAt(searched, off.getRelative(1, 0, 0)))
                return true;
            if (searchAt(searched, off.getRelative(-1, 0, 0)))
                return true;
            if (searchAt(searched, off.getRelative(0, 0, 1)))
                return true;
            if (searchAt(searched, off.getRelative(0, 0, -1)))
                return true;
            if (searchAt(searched, off.getRelative(0, 1, 0)))
                return true;
        }

        return false;
    }

    public Material getFromChest() {

        Block chest = SignUtil.getBackBlock(getSign().getBlock()).getRelative(0, -1, 0);

        if (chest.getType() == Material.CHEST) {
            Chest c = (Chest) chest.getState();
            HashMap<Integer, ItemStack> over = c.getInventory().removeItem(new ItemStack(BlockID.WATER, 1));
            if (over.size() == 0)
                return Material.WATER;
            over = c.getInventory().removeItem(new ItemStack(BlockID.LAVA, 1));
            if (over.size() == 0)
                return Material.LAVA;
        }

        return Material.AIR;
    }

    public Material getFromChest(Material m) {

        Block chest = SignUtil.getBackBlock(getSign().getBlock()).getRelative(0, -1, 0);

        if (m == Material.STATIONARY_WATER) {
            m = Material.WATER;
        }
        if (m == Material.STATIONARY_LAVA) {
            m = Material.LAVA;
        }

        if (chest.getType() == Material.CHEST) {
            Chest c = (Chest) chest.getState();
            HashMap<Integer, ItemStack> over = c.getInventory().removeItem(new ItemStack(m, 1));
            if (over.size() == 0)
                return m;
        }

        return Material.AIR;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new Spigot(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Fills areas with liquid from below chest.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "radius",
                    "y offset"
            };
            return lines;
        }
    }
}