package com.sk89q.craftbook.gates.world.blocks;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.SignUtil;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.regex.Pattern;

/**
 * @author Silthus
 */
public class SetDoor extends AbstractIC {

    private static final Pattern MINUS_PATTERN = Pattern.compile("-", Pattern.LITERAL);
    private static final Pattern COMMA_PATTERN = Pattern.compile(",", Pattern.LITERAL);
    private int onMaterial;
    private int onData;

    private int offMaterial;
    private int offData;

    private int width;
    private int height;

    private int offsetX;
    private int offsetY;
    private int offsetZ;

    private Block center;
    private BlockFace faceing;

    public SetDoor(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public void load() {

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
                        offMaterial = 0;
                        offData = 0;
                    } catch (ArrayIndexOutOfBoundsException e) {
                        offData = 0;
                    }
                }
                // parse the material and data for toggle on
                String[] strings = ICUtil.COLON_PATTERN.split(split[0]);
                onMaterial = Integer.parseInt(strings[0]);
                if (strings.length > 0) {
                    onData = Integer.parseInt(strings[1]);
                }
            } catch (NumberFormatException e) {
                onMaterial = 1;
                onData = 0;
            } catch (ArrayIndexOutOfBoundsException e) {
                onData = 0;
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
                offsetX = 0;
                offsetY = 0;
                offsetZ = 0;
            } catch (IndexOutOfBoundsException e) {
                offsetX = 0;
                offsetY = 0;
                offsetZ = 0;
            }
            try {
                // parse the size of the door
                String[] sizeSplit = COMMA_PATTERN.split(split[1]);
                width = Integer.parseInt(sizeSplit[0]);
                height = Integer.parseInt(sizeSplit[1]);
            } catch (NumberFormatException e) {
                width = 1;
                height = 1;
            } catch (ArrayIndexOutOfBoundsException e) {
                height = 1;
            }
            if (relativeOffset) {
                center = LocationUtil.getRelativeOffset(getSign(), offsetX, offsetY, offsetZ);
            } else {
                center = LocationUtil.getOffset(center, offsetX, offsetY, offsetZ);
            }
        } else {
            center = center.getRelative(BlockFace.UP);
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

        @Override
        public String getDescription () {

            return "Generates a door out of the set materials with set size.";
        }

        @Override
        public String[] getLineHelp () {

            String[] lines = new String[] { "onID:onData-offID:offData", "offset x,y,z:width,height" };
            return lines;
        }
    }
}
