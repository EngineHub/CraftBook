package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ic.*;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class BlockSensor extends AbstractIC {

    private Block center;
    private int id = 0;
    private byte data = -1;

    public BlockSensor(Server server, Sign sign, ICFactory factory) {

        super(server, sign, factory);
        load();
    }

    private void load() {

        try {
            center = ICUtil.parseBlockLocation(getSign());
            String ids = getSign().getLine(3);
            id = Integer.parseInt(ids.split(":")[0]);
            data = Byte.parseByte(ids.split(":")[1]);
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
        public IC create(Sign sign) {

            return new BlockSensor(getServer(), sign, this);
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