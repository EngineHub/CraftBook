package com.sk89q.craftbook.gates.world.blocks;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.*;
import com.sk89q.worldedit.blocks.BlockID;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;

public class LiquidFlood extends AbstractIC {

    int radius;
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

        try {
            String[] splitEquals = ICUtil.EQUALS_PATTERN.split(getSign().getLine(2), 2);
            radius = Integer.parseInt(splitEquals[0]);
            if (getSign().getLine(2).contains("=")) {
                String[] splitCoords = ICUtil.COLON_PATTERN.split(splitEquals[1]);
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
            radius = 10;
        }

        liquid = getSign().getLine(2).equalsIgnoreCase("lava") ? "lava" : "water";
    }

    public void doStuff(ChipState chip) {

        if (chip.getInput(0)) {
            for (int x = -radius + 1; x < radius; x++) {
                for (int y = -radius + 1; y < radius; y++) {
                    for (int z = -radius + 1; z < radius; z++) {
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
            for (int x = -radius + 1; x < radius; x++) {
                for (int y = -radius + 1; y < radius; y++) {
                    for (int z = -radius + 1; z < radius; z++) {
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
        public String getDescription() {

            return "Floods an area with a liquid.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {"water/lava", "radius=x:y:z offset"};
            return lines;
        }
    }
}