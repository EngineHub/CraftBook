package com.sk89q.craftbook.gates.world;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.core.AbstractIC;
import com.sk89q.craftbook.ic.core.AbstractICFactory;
import com.sk89q.craftbook.ic.core.ChipState;
import com.sk89q.craftbook.ic.core.IC;
import com.sk89q.craftbook.util.SignUtil;

public class BlockSensor extends AbstractIC {

    public BlockSensor(Server server, Sign sign) {
        super(server, sign);
    }

    @Override
    public String getTitle() {
        return "Block Sensor";
    }

    @Override
    public String getSignTitle() {
        return "BLOCK SENSOR";
    }

    @Override
    public void trigger(ChipState chip) {
        if (chip.getInput(0)) {
            chip.setOutput(0, hasBlock());
        }
    }

    /**
     * Returns true if the sign has water at the specified location.
     * 
     * @return
     */
    protected boolean hasBlock() {

        Block b = SignUtil.getBackBlock(getSign().getBlock());

        int x = b.getX();
        int yOffset = b.getY();
        int z = b.getZ();
        String ids = "";
        int id = 0;
        try {
            String yOffsetLine = getSign().getLine(2);
            ids = getSign().getLine(3);
            if (yOffsetLine.length() > 0) {
                yOffset += Integer.parseInt(yOffsetLine);
            } else {
                yOffset -= 1;
            }
            id = Integer.parseInt(ids);
        } catch (NumberFormatException e) {
            yOffset -= 1;
        }
        int blockID = getSign().getBlock().getWorld()
                .getBlockTypeIdAt(x, yOffset, z);

        return blockID == id;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {
            super(server);
        }

        @Override
        public IC create(Sign sign) {
            return new BlockSensor(getServer(), sign);
        }
    }

}
