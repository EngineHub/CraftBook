package com.sk89q.craftbook.gates.world;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.ICUtil;
import com.sk89q.craftbook.ic.ICVerificationException;
import com.sk89q.craftbook.ic.SelfTriggeredIC;

import java.util.regex.Pattern;

public class BlockSensorST extends BlockSensor implements SelfTriggeredIC {

    private static final Pattern COLON_PATTERN = Pattern.compile(":", Pattern.LITERAL);

    public BlockSensorST(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
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
        public IC create(ChangedSign sign) {

            return new BlockSensorST(getServer(), sign, this);
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            try {
                String[] split = COLON_PATTERN.split(sign.getLine(3));
                Integer.parseInt(split[0]);
            } catch (Exception ignored) {
                throw new ICVerificationException("You need to specify an block in line four.");
            }
            ICUtil.verifySignSyntax(sign);
        }
    }
}
