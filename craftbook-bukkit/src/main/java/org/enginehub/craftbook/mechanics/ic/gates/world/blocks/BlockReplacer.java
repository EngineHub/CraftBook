/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
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
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanics.ic.AbstractIC;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.ICVerificationException;
import org.enginehub.craftbook.mechanics.ic.RestrictedIC;
import org.enginehub.craftbook.util.BlockParser;
import org.enginehub.craftbook.util.LocationUtil;
import org.enginehub.craftbook.util.RegexUtil;

import java.util.HashSet;
import java.util.Set;

public class BlockReplacer extends AbstractIC {

    public BlockReplacer(Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    @Override
    public void trigger(ChipState chip) {

        chip.setOutput(0, replaceBlocks(chip.getInput(0)));
    }

    private BlockStateHolder onBlock;
    private BlockStateHolder offBlock;

    int delay;
    int mode;
    boolean physics;

    @Override
    public void load() {

        String[] ids = RegexUtil.MINUS_PATTERN.split(getLine(2));

        onBlock = BlockParser.getBlock(ids[0], true);
        offBlock = BlockParser.getBlock(ids[1], true);

        String[] data = RegexUtil.COLON_PATTERN.split(getLine(3));
        delay = Integer.parseInt(data[0]);
        if (data.length > 1)
            mode = Integer.parseInt(data[1]);
        else
            mode = 0;
        physics = data.length <= 2 || data[2].equalsIgnoreCase("1");
    }

    public boolean replaceBlocks(final boolean on, final Block block, final Set<Location> traversedBlocks) {

        if (traversedBlocks.size() > 15000)
            return true;

        if (mode == 0) {
            for (BlockFace f : LocationUtil.getDirectFaces()) {

                final Block b = block.getRelative(f);

                if (traversedBlocks.contains(b.getLocation()))
                    continue;
                traversedBlocks.add(b.getLocation());

                BlockState bState = BukkitAdapter.adapt(b.getBlockData());

                if (onBlock.equalsFuzzy(bState)) {
                    if (!on) {
                        b.setBlockData(BukkitAdapter.adapt(offBlock), physics);
                    }
                    Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), () -> replaceBlocks(on, b, traversedBlocks), delay);
                } else if (offBlock.equalsFuzzy(bState)) {
                    if (on) {
                        b.setBlockData(BukkitAdapter.adapt(onBlock), physics);
                    }
                    Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), () -> replaceBlocks(on, b, traversedBlocks), delay);
                }
            }
        }

        return traversedBlocks.size() > 0;
    }

    public boolean replaceBlocks(boolean on) {
        Block block = getBackBlock();
        BlockState blockState = BukkitAdapter.adapt(block.getBlockData());

        if (onBlock.equalsFuzzy(blockState)) {
            if (!on) {
                block.setBlockData(BukkitAdapter.adapt(offBlock), physics);
            }
        } else if (offBlock.equalsFuzzy(blockState))
            if (on) {
                block.setBlockData(BukkitAdapter.adapt(onBlock), physics);
            }
        Set<Location> traversedBlocks = new HashSet<>();
        traversedBlocks.add(block.getLocation());
        return replaceBlocks(on, block, traversedBlocks);
    }

    @Override
    public String getTitle() {
        return "Block Replacer";
    }

    @Override
    public String getSignTitle() {
        return "BLOCK REPLACER";
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new BlockReplacer(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Searches a nearby area and replaces blocks accordingly.";
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            String[] ids = RegexUtil.MINUS_PATTERN.split(sign.getLine(2));

            String[] onIds = RegexUtil.COLON_PATTERN.split(ids[0]);
            try {
                Integer.parseInt(onIds[0]);
            } catch (Exception e) {
                throw new ICVerificationException("Must provide an on ID!");
            }
            try {
                if (onIds.length > 1)
                    Byte.parseByte(onIds[1]);
            } catch (Exception e) {
                throw new ICVerificationException("Invalid on Data!");
            }

            String[] offIds = RegexUtil.COLON_PATTERN.split(ids[1]);
            try {
                Integer.parseInt(offIds[0]);
            } catch (Exception e) {
                throw new ICVerificationException("Must provide an off ID!");
            }
            try {
                if (offIds.length > 1)
                    Byte.parseByte(offIds[1]);
            } catch (Exception e) {
                throw new ICVerificationException("Invalid off Data!");
            }

            String[] data = RegexUtil.COLON_PATTERN.split(sign.getLine(3));
            try {
                Integer.parseInt(data[0]);
            } catch (Exception e) {
                throw new ICVerificationException("Must provide a delay!");
            }
            try {
                if (data.length > 1)
                    Integer.parseInt(data[1]);
            } catch (Exception e) {
                throw new ICVerificationException("Invalid mode!");
            }
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "onID{:onData}-offID{:offData}}", "delay{:mode:physics}" };
        }
    }
}