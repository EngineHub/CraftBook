package com.sk89q.craftbook.gates.world.blocks;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.SignUtil;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

public class BlockLauncher extends AbstractIC {
    Vector velocity;
    int id;
    byte data;

    public BlockLauncher(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Block Launcher";
    }

    @Override
    public String getSignTitle() {

        return "BLOCK LAUNCH";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            launch();
        }
    }

    @Override
    public void load() {
        try {
            String[] split = ICUtil.COLON_PATTERN.split(getSign().getLine(2));
            id = Integer.parseInt(split[0]);
            data = Byte.parseByte(split[1]);
        } catch(Exception ignored){
            id = 12;
            data = 0;
        }

        try {
            String[] split2 = ICUtil.COLON_PATTERN.split(getSign().getLine(3));
            velocity = new Vector(Double.parseDouble(split2[0]), Double.parseDouble(split2[1]), Double.parseDouble(split2[2]));

        } catch (Exception ignored) {
            velocity = new Vector(0, 0.5, 0);
        }
    }

    public void launch() {

        Block above = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock()).getRelative(0, 1, 0);
        int timeout = 12;
        while (above.getTypeId() != 0 || timeout < 0 || above.getLocation().getY() >= 255) {
            above = above.getRelative(0, 1, 0);
            timeout--;
        }
        if (velocity.getY() < 0) {
            above = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock()).getRelative(0, -1, 0);
            timeout = 12;
            while (above.getTypeId() != 0 || timeout < 0 || above.getLocation().getY() <= 1) {
                above = above.getRelative(0, -1, 0);
                timeout--;
            }
        }
        double y = above.getY() - 0.99D;
        FallingBlock block = BukkitUtil.toSign(getSign()).getWorld().spawnFallingBlock(new Location(BukkitUtil.toSign(getSign()).getWorld(),
                above.getX() + 0.5D, y, above.getZ() + 0.5D), id, data);
        block.setVelocity(velocity);
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new BlockLauncher(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Launches set block with set velocity.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "id:data",
                    "velocity x:y:z"
            };
            return lines;
        }
    }
}