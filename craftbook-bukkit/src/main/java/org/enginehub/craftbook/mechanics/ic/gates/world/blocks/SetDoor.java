/*
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package org.enginehub.craftbook.mechanics.ic.gates.world.blocks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.bukkit.util.CraftBookBukkitUtil;
import org.enginehub.craftbook.mechanics.ic.AbstractIC;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.RestrictedIC;
import org.enginehub.craftbook.util.BlockParser;
import org.enginehub.craftbook.util.BlockUtil;
import org.enginehub.craftbook.util.LocationUtil;
import org.enginehub.craftbook.util.RegexUtil;
import org.enginehub.craftbook.util.SignUtil;

/**
 * @author Silthus
 */
public class SetDoor extends AbstractIC {

    private BlockData onBlock;
    private BlockData offBlock;

    private int width;
    private int height;

    private int offsetX = 0;
    private int offsetY = 1;
    private int offsetZ = 0;

    private Block center;
    private BlockFace faceing;

    public SetDoor(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public void load() {

        faceing = SignUtil.getFacing(CraftBookBukkitUtil.toSign(getSign()).getBlock());
        String line = getSign().getLine(2);
        if (!line.isEmpty()) {
            String[] split = RegexUtil.MINUS_PATTERN.split(line);
            // parse the material data
            if (split.length > 1) {
                offBlock = BukkitAdapter.adapt(BlockParser.getBlock(split[1]));
            }
            // parse the material and data for toggle on
            onBlock = BukkitAdapter.adapt(BlockParser.getBlock(split[0]));
        }
        // parse the coordinates
        line = getSign().getLine(3);
        if (!line.isEmpty()) {
            boolean relativeOffset = !line.contains("!");
            if (!relativeOffset) {
                line = line.replace("!", "");
            }
            String[] split = RegexUtil.COLON_PATTERN.split(line);
            try {
                // parse the offset
                String[] offsetSplit = RegexUtil.COMMA_PATTERN.split(split[0]);
                offsetX = Integer.parseInt(offsetSplit[0]);
                offsetY = Integer.parseInt(offsetSplit[1]);
                offsetZ = Integer.parseInt(offsetSplit[2]);
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                // ignore and use defaults
            }
            try {
                // parse the size of the door
                String[] sizeSplit = RegexUtil.COMMA_PATTERN.split(split[1]);
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
                center = getBackBlock().getRelative(offsetX, offsetY, offsetZ);
            }
        } else {
            center = getBackBlock().getRelative(BlockFace.UP);
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

        chip.setOutput(0, chip.getInput(0));
    }

    private void setDoor(boolean open) {

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Block block = LocationUtil.getRelativeOffset(center, faceing, x, y, 0);
                // do not replace the block the sign is on
                boolean isSource = block.equals(getBackBlock());

                if (open) {
                    if (isSource && BlockUtil.isBlockReplacable(onBlock.getMaterial())) continue;
                    block.setBlockData(onBlock);
                } else {
                    if (isSource && BlockUtil.isBlockReplacable(offBlock.getMaterial())) continue;
                    block.setBlockData(offBlock);
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
        public String getShortDescription() {

            return "Generates a door out of the set materials with set size.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "onID{:onData-offID:offData}", "offset x,y,z:width,height" };
        }
    }
}
