package com.sk89q.craftbook.mechanics.ic.gates.world.sensors;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.ConfigurableIC;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.ICVerificationException;
import com.sk89q.craftbook.util.BlockSyntax;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import org.bukkit.Server;
import org.bukkit.block.Block;

public class BlockSensor extends AbstractSelfTriggeredIC {

    private Block center;
    private BlockStateHolder item;

    public BlockSensor(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public void load() {

        center = ICUtil.parseBlockLocation(getSign());
        item = BlockSyntax.getBlock(getLine(3), true);
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
            chip.setOutput(0, ((Factory) getFactory()).invert != hasBlock());
        }
    }

    @Override
    public void think(ChipState chip) {

        chip.setOutput(0, ((Factory) getFactory()).invert != hasBlock());
    }

    /**
     * Returns true if the sign has water at the specified location.
     *
     * @return
     */
    protected boolean hasBlock() {

        return item.equalsFuzzy(BukkitAdapter.adapt(center.getBlockData()));
    }

    public static class Factory extends AbstractICFactory implements ConfigurableIC {

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
    }
}