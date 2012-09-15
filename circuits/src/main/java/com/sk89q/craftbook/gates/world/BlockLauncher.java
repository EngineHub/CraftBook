package com.sk89q.craftbook.gates.world;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.util.SignUtil;

public class BlockLauncher extends AbstractIC {

    public BlockLauncher(Server server, Sign block) {
        super(server, block);
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

        if (chip.getInput(0)) launch();
    }

    public void launch() {
        Block above = SignUtil.getBackBlock(getSign().getBlock()).getRelative(0, 1, 0);
        int id = 12;
        byte data = 0;
        try {
            id = Integer.parseInt(getSign().getLine(2).split(":")[0]);
            data = Byte.parseByte(getSign().getLine(2).split(":")[1]);
        }
        catch(Exception e) {}
        Vector velocity = new Vector(0,0.5,0);
        try {
            velocity.setX(Double.parseDouble(getSign().getLine(3).split(":")[0]));
            velocity.setY(Double.parseDouble(getSign().getLine(3).split(":")[1]));
            velocity.setZ(Double.parseDouble(getSign().getLine(3).split(":")[2]));
        }
        catch(Exception e){}
        double y = above.getY() - 0.99D;
        if(velocity.getY() < 0)
            y = above.getY() - 2.99;
        FallingBlock block = getSign().getWorld().spawnFallingBlock(new Location(getSign().getWorld(), above.getX() + 0.5D, y, above.getZ() + 0.5D), id, data);
        block.setVelocity(velocity);
    }

    public static class Factory extends AbstractICFactory implements
    RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new BlockLauncher(getServer(), sign);
        }
    }
}
