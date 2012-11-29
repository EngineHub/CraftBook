package com.sk89q.craftbook.gates.world.blocks;

import java.util.regex.Pattern;

import org.bukkit.Server;
import org.bukkit.block.Block;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.ICUtil;
import com.sk89q.craftbook.ic.ICVerificationException;

public class BlockSensor extends AbstractIC {

    private static final Pattern COLON_PATTERN = Pattern.compile(":", Pattern.LITERAL);
    private Block center;
    private int id = 0;
    private byte data = -1;

    public BlockSensor(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public void load() {

        try {
            center = ICUtil.parseBlockLocation(getSign());
            String[] ids = COLON_PATTERN.split(getSign().getLine(3));
            id = Integer.parseInt(ids[0]);
            data = Byte.parseByte(ids[1]);
        } catch (Exception ignored) {
            // use defaults
        }
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

        int blockID = center.getTypeId();

        if (data != (byte) -1) if (blockID == id)
            return data == center.getData();
        return blockID == id;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new BlockSensor(getServer(), sign, this);
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            try {
                String[] split = COLON_PATTERN.split(sign.getLine(3), 2);
                Integer.parseInt(split[0]);
            } catch (Exception ignored) {
                throw new ICVerificationException("You need to specify an block in line four.");
            }
            ICUtil.verifySignSyntax(sign);
        }

        @Override
        public String getDescription() {

            return "Checks for blocks at location.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "x:y:z",
                    "id:data"
            };
            return lines;
        }
    }
}