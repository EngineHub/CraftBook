package com.sk89q.craftbook.gates.world;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.SignUtil;

/**
 * @author Silthus
 */
public class SetDoor extends AbstractIC {

    private int onMaterial = 1;
    private int onData = 0;

    private int offMaterial = 0;
    private int offData = 0;

    private int width = 1;
    private int height = 1;

    private int offsetX = 0;
    private int offsetY = 0;
    private int offsetZ = 0;

    private Block center;
    private BlockFace faceing;

    public SetDoor(Server server, Sign block, ICFactory factory) {

        super(server, block, factory);
        load();
    }

    private void load() {

        try {
            center = SignUtil.getBackBlock(getSign().getBlock());
            faceing = SignUtil.getFacing(getSign().getBlock());
            String line = getSign().getLine(2);
            if (!line.equals("")) {
                try {
                    String[] split = line.split("-");
                    // parse the material data
                    if (split.length > 0) {
                        try {
                            // parse the data that gets set when the block is toggled off
                            String[] strings = split[1].split(":");
                            offMaterial = Integer.parseInt(strings[0]);
                            if (strings.length > 0) {
                                offData = Integer.parseInt(strings[1]);
                            }
                        } catch (NumberFormatException e) {
                            // do nothing and use the defaults
                        } catch (ArrayIndexOutOfBoundsException e) {
                            // do nothing and use the defaults
                        }
                    }
                    // parse the material and data for toggle on
                    String[] strings = split[0].split(":");
                    onMaterial = Integer.parseInt(strings[0]);
                    if (strings.length > 0) {
                        onData = Integer.parseInt(strings[1]);
                    }
                } catch (NumberFormatException e) {
                    // do nothing and use the defaults
                } catch (ArrayIndexOutOfBoundsException e) {
                    // do nothing and use the defaults
                }
            }
            // parse the coordinates
            line = getSign().getLine(3);
            if (!line.equals("")) {
                boolean relativeOffset = !line.contains("!");
                if (!relativeOffset) {
                    line = line.replace("!", "");
                }
                String[] split = line.split(":");
                try {
                    // parse the offset
                    String[] offsetSplit = split[0].split(",");
                    offsetX = Integer.parseInt(offsetSplit[0]);
                    offsetY = Integer.parseInt(offsetSplit[1]);
                    offsetZ = Integer.parseInt(offsetSplit[2]);
                } catch (NumberFormatException e) {
                    // do nothing and use the defaults
                } catch (IndexOutOfBoundsException e) {
                    // do nothing and use the defaults
                }
                try {
                    // parse the size of the door
                    String[] sizeSplit = split[1].split(",");
                    width = Integer.parseInt(sizeSplit[0]);
                    height = Integer.parseInt(sizeSplit[1]);
                } catch (NumberFormatException e) {
                    // do nothing and use the defaults
                } catch (ArrayIndexOutOfBoundsException e) {
                    // do nothing and use the defaults
                }
                if (relativeOffset) {
                    center = LocationUtil.getRelativeOffset(getSign(), offsetX, offsetY, offsetZ);
                } else {
                    center = LocationUtil.getOffset(center, offsetX, offsetY, offsetZ);
                }
            } else {
                center = center.getRelative(BlockFace.UP);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public String getTitle() {

        return "Set P-Door";
    }

    @Override
    public String getSignTitle() {

        return "SET P-DOOR";
    }

    @Override
    public void trigger(ChipState chip) {

        load();
        if (chip.getInput(0)) {
            setDoor(true);
        } else {
            setDoor(false);
        }
    }

    private void setDoor(boolean open) {

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Block block = LocationUtil.getRelativeOffset(center, faceing, x, y, 0);
                if (open) {
                    block.setTypeIdAndData(onMaterial, (byte) onData, true);
                } else {
                    block.setTypeIdAndData(offMaterial, (byte) offData, true);
                }
            }
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new SetDoor(getServer(), sign, this);
        }
    }
}
