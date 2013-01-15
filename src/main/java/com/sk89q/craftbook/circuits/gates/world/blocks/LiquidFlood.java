package com.sk89q.craftbook.circuits.gates.world.blocks;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractIC;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.ICUtil;
import com.sk89q.craftbook.circuits.ic.RestrictedIC;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockID;

public class LiquidFlood extends AbstractIC {

    Vector radius;
    String liquid;
    Location centre;

    public LiquidFlood(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Liquid Flooder";
    }

    @Override
    public String getSignTitle() {

        return "LIQUID FLOOD";
    }

    @Override
    public void load() {

        centre = BukkitUtil.toSign(getSign()).getLocation();

        radius = ICUtil.parseRadius(getSign());
        try {

            if (getSign().getLine(2).contains("=")) {
                String[] splitEquals = RegexUtil.EQUALS_PATTERN.split(getSign().getLine(2), 2);
                String[] splitCoords = RegexUtil.COLON_PATTERN.split(splitEquals[1]);
                int x = Integer.parseInt(splitCoords[0]);
                int y = Integer.parseInt(splitCoords[1]);
                int z = Integer.parseInt(splitCoords[2]);
                if (x > 16) x = 16;
                if (x < -16) x = -16;
                if (y > 16) y = 16;
                if (y < -16) y = -16;
                if (z > 16) z = 16;
                if (z < -16) z = -16;
                centre.add(x, y, z);
            }
        } catch (Exception ignored) {
        }

        liquid = getSign().getLine(2).equalsIgnoreCase("lava") ? "lava" : "water";
    }

    public void doStuff(ChipState chip) {

        if (chip.getInput(0)) {
            for (int x = -radius.getBlockX() + 1; x < radius.getBlockX(); x++) {
                for (int y = -radius.getBlockY() + 1; y < radius.getBlockY(); y++) {
                    for (int z = -radius.getBlockZ() + 1; z < radius.getBlockZ(); z++) {
                        int rx = centre.getBlockX() - x;
                        int ry = centre.getBlockY() - y;
                        int rz = centre.getBlockZ() - z;
                        Block b = BukkitUtil.toSign(getSign()).getWorld().getBlockAt(rx, ry, rz);
                        if (b.getTypeId() == 0 || b.getTypeId() == (liquid.equalsIgnoreCase("water") ? BlockID.WATER
                                : BlockID.LAVA)) {
                            b.setTypeId(liquid.equalsIgnoreCase("water") ? BlockID.STATIONARY_WATER : BlockID
                                    .STATIONARY_LAVA);
                        }
                    }
                }
            }
        } else if (!chip.getInput(0)) {
            for (int x = -radius.getBlockX() + 1; x < radius.getBlockX(); x++) {
                for (int y = -radius.getBlockY() + 1; y < radius.getBlockY(); y++) {
                    for (int z = -radius.getBlockZ() + 1; z < radius.getBlockZ(); z++) {
                        int rx = centre.getBlockX() - x;
                        int ry = centre.getBlockY() - y;
                        int rz = centre.getBlockZ() - z;
                        Block b = BukkitUtil.toSign(getSign()).getWorld().getBlockAt(rx, ry, rz);
                        if (b.getTypeId() == (liquid.equalsIgnoreCase("water") ? BlockID.WATER : BlockID.LAVA)
                                || b.getTypeId() == (liquid.equalsIgnoreCase("water") ? BlockID.STATIONARY_WATER :
                                    BlockID.STATIONARY_LAVA)) {
                            b.setTypeId(BlockID.AIR);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void trigger(ChipState chip) {

        doStuff(chip);
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new LiquidFlood(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Floods an area with a liquid.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {"water/lava", "radius=x:y:z offset"};
            return lines;
        }
    }
}