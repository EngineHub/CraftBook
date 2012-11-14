package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ic.*;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.SignUtil;

import java.util.regex.Pattern;

/**
 * @author Silthus
 */
public class SetDoor extends AbstractIC {

    private static final Pattern MINUS_PATTERN = Pattern.compile("-", Pattern.LITERAL);
    private static final Pattern COMMA_PATTERN = Pattern.compile(",", Pattern.LITERAL);
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

    public SetDoor(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
        load();
    }

    private void load() {

        try {
            center = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock());
            faceing = SignUtil.getFacing(BukkitUtil.toSign(getSign()).getBlock());
            String line = getSign().getLine(2);
            if (!line.isEmpty()) {
                try {
                    String[] split = MINUS_PATTERN.split(line);
                    // parse the material data
                    if (split.length > 0) {
                        try {
                            // parse the data that gets set when the block is toggled off
                            String[] strings = ICUtil.COLON_PATTERN.split(split[1]);
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
                    String[] strings = ICUtil.COLON_PATTERN.split(split[0]);
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
            if (!line.isEmpty()) {
                boolean relativeOffset = !line.contains("!");
                if (!relativeOffset) {
                    line = line.replace("!", "");
                }
                String[] split = ICUtil.COLON_PATTERN.split(line);
                try {
                    // parse the offset
                    String[] offsetSplit = COMMA_PATTERN.split(split[0]);
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
                    String[] sizeSplit = COMMA_PATTERN.split(split[1]);
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
        public IC create(ChangedSign sign) {

            return new SetDoor(getServer(), sign, this);
        }
    }
}
