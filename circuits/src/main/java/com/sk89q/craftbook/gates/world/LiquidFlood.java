package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ic.*;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class LiquidFlood extends AbstractIC {

    int radius;
    String liquid;

    public LiquidFlood(Server server, Sign block, ICFactory factory) {

        super(server, block, factory);
        load();
    }

    @Override
    public String getTitle() {

        return "Liquid Flooder";
    }

    @Override
    public String getSignTitle() {

        return "LIQUID FLOOD";
    }

    private void load() {

        try {
            radius = Integer.parseInt(getSign().getLine(3));
            if (radius > 15) {
                radius = 15;
                getSign().setLine(3, "15");
                getSign().update();
            }
        } catch (Exception e) {
            radius = 10;
        }

        try {
            liquid = getSign().getLine(2).equalsIgnoreCase("lava") ? "lava" : "water";
        } catch (Exception ignored) {
        }
    }

    public void doStuff(ChipState chip) {

        if (chip.getInput(0)) {
            for (int x = -radius + 1; x < radius; x++) {
                for (int y = -radius + 1; y < radius; y++) {
                    for (int z = -radius + 1; z < radius; z++) {
                        int rx = getSign().getLocation().getBlockX() - x;
                        int ry = getSign().getLocation().getBlockY() - y;
                        int rz = getSign().getLocation().getBlockZ() - z;
                        Block b = getSign().getWorld().getBlockAt(rx, ry, rz);
                        if (b.getTypeId() == 0 || b.getType() == (liquid.equalsIgnoreCase("water") ? Material.WATER :
                            Material.LAVA)) {
                            b.setType(liquid.equalsIgnoreCase("water") ? Material.STATIONARY_WATER : Material
                                    .STATIONARY_LAVA);
                        }
                    }
                }
            }
        } else if (!chip.getInput(0)) {
            for (int x = -radius + 1; x < radius; x++) {
                for (int y = -radius + 1; y < radius; y++) {
                    for (int z = -radius + 1; z < radius; z++) {
                        int rx = getSign().getLocation().getBlockX() - x;
                        int ry = getSign().getLocation().getBlockY() - y;
                        int rz = getSign().getLocation().getBlockZ() - z;
                        Block b = getSign().getWorld().getBlockAt(rx, ry, rz);
                        if (b.getType() == (liquid.equalsIgnoreCase("water") ? Material.WATER : Material.LAVA) || b
                                .getType() == (liquid.equalsIgnoreCase("water") ? Material.STATIONARY_WATER :
                                    Material.STATIONARY_LAVA)) {
                            b.setType(Material.AIR);
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

    public static class Factory extends AbstractICFactory implements
    RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new LiquidFlood(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Floods an area with a liquid.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "water/lava",
                    "radius"
            };
            return lines;
        }
    }
}