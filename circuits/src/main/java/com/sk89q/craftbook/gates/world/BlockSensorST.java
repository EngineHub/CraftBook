package com.sk89q.craftbook.gates.world;

import org.bukkit.Server;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICUtil;
import com.sk89q.craftbook.ic.ICVerificationException;
import com.sk89q.craftbook.ic.SelfTriggeredIC;

public class BlockSensorST extends BlockSensor implements SelfTriggeredIC {

    public BlockSensorST(Server server, Sign sign) {

        super(server, sign);
    }

    @Override
    public String getTitle() {

        return "Self-triggered Block Sensor";
    }

    @Override
    public String getSignTitle() {

        return "ST BLOCK SENSOR";
    }

    @Override
    public void think(ChipState chip) {

        chip.setOutput(0, hasBlock());
    }

    @Override
    public boolean isActive() {

        return true;
    }

    public static class Factory extends BlockSensor.Factory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new BlockSensorST(getServer(), sign);
        }

        @Override
        public void verify(Sign sign) throws ICVerificationException {

            try {
                String[] split = sign.getLine(3).split(":");
                Integer.parseInt(split[0]);
            } catch (Exception ignored) {
                throw new ICVerificationException("You need to specify an block in line four.");
            }
            ICUtil.verifySignSyntax(sign);
        }
    }
}
