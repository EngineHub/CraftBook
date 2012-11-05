package com.sk89q.craftbook.gates.world;

import org.bukkit.Server;
import org.bukkit.block.Block;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.worldedit.blocks.BlockID;

public class LiquidFlood extends AbstractIC {

    int radius;
    String liquid;

    public LiquidFlood(Server server, ChangedSign block, ICFactory factory) {

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
                getSign().update(false);
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
                        int rx = getSign().getSignLocation().getPosition().getBlockX() - x;
                        int ry = getSign().getSignLocation().getPosition().getBlockY() - y;
                        int rz = getSign().getSignLocation().getPosition().getBlockZ() - z;
                        Block b = BukkitUtil.toSign(getSign()).getWorld().getBlockAt(rx, ry, rz);
                        if (b.getTypeId() == 0 || b.getTypeId() == (liquid.equalsIgnoreCase("water") ? BlockID.WATER :
                            BlockID.LAVA)) {
                            b.setTypeId(liquid.equalsIgnoreCase("water") ? BlockID.STATIONARY_WATER :
                                BlockID.STATIONARY_LAVA);
                        }
                    }
                }
            }
        } else if (!chip.getInput(0)) {
            for (int x = -radius + 1; x < radius; x++) {
                for (int y = -radius + 1; y < radius; y++) {
                    for (int z = -radius + 1; z < radius; z++) {
                        int rx = getSign().getSignLocation().getPosition().getBlockX() - x;
                        int ry = getSign().getSignLocation().getPosition().getBlockY() - y;
                        int rz = getSign().getSignLocation().getPosition().getBlockZ() - z;
                        Block b = BukkitUtil.toSign(getSign()).getWorld().getBlockAt(rx, ry, rz);
                        if (b.getTypeId() == (liquid.equalsIgnoreCase("water") ? BlockID.WATER : BlockID.LAVA) || b
                                .getTypeId() == (liquid.equalsIgnoreCase("water") ? BlockID.STATIONARY_WATER :
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

    public static class Factory extends AbstractICFactory implements
    RestrictedIC {

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

            String[] lines = new String[] {
                    "water/lava",
                    "radius"
            };
            return lines;
        }
    }
}