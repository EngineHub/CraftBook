package com.sk89q.craftbook.circuits.gates.world.sensors;

import org.bukkit.Server;
import org.bukkit.block.Block;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.ICVerificationException;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.util.yaml.YAMLProcessor;

public class BlockSensor extends AbstractSelfTriggeredIC {

    private Block center;
    private int id;
    private byte data;

    public BlockSensor(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public void load() {

        String[] ids = RegexUtil.COLON_PATTERN.split(getSign().getLine(3), 2);
        center = ICUtil.parseBlockLocation(getSign());
        try {
            id = Integer.parseInt(ids[0]);
        } catch (Exception ignored) {
            id = 1;
        }
        try {
            data = Byte.parseByte(ids[1]);
        } catch (Exception ignored) {
            data = -1;
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
            chip.setOutput(0, ((Factory) getFactory()).invert ? !hasBlock() : hasBlock());
        }
    }

    @Override
    public void think(ChipState chip) {

        chip.setOutput(0, ((Factory) getFactory()).invert ? !hasBlock() : hasBlock());
    }

    /**
     * Returns true if the sign has water at the specified location.
     *
     * @return
     */
    protected boolean hasBlock() {

        int blockID = center.getTypeId();

        if (data != (byte) -1)
            if (blockID == id) return
                    data == center.getData();
        return blockID == id;
    }

    public static class Factory extends AbstractICFactory {

        boolean invert;

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
                String[] split = RegexUtil.COLON_PATTERN.split(sign.getLine(3), 2);
                Integer.parseInt(split[0]);
            } catch (Exception ignored) {
                throw new ICVerificationException("You need to specify a block in line four.");
            }
            ICUtil.verifySignSyntax(sign);
        }

        @Override
        public String getShortDescription() {

            return "Checks for blocks at location.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"x:y:z", "id:data"};
        }

        @Override
        public void addConfiguration(YAMLProcessor config, String path) {

            invert = config.getBoolean(path + "invert-output", false);
        }

        @Override
        public boolean needsConfiguration() {

            return true;
        }
    }

    @Override
    public boolean isActive () {
        return true;
    }
}