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

import com.google.common.collect.Lists;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.blocks.Blocks;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.ConfigurableIC;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.ICVerificationException;
import org.enginehub.craftbook.util.BlockSyntax;
import org.enginehub.craftbook.util.BlockUtil;
import org.enginehub.craftbook.util.ICUtil;

import java.util.List;

public class BlockBreaker extends AbstractSelfTriggeredIC {

    public BlockBreaker(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Block Breaker";
    }

    @Override
    public String getSignTitle() {

        return "BLOCK BREAK";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            chip.setOutput(0, breakBlock());
        }
    }

    @Override
    public void think(ChipState state) {

        state.setOutput(0, breakBlock());
    }

    private Block broken;
    private BaseBlock item;

    @Override
    public void load() {
        item = BlockSyntax.getBlock(getLine(2), true);
    }

    public boolean breakBlock() {

        boolean above = ((Factory) getFactory()).above;
        if (broken == null) {
            Block bl = getBackBlock();

            if (above) {
                broken = bl.getRelative(0, 1, 0);
            } else {
                broken = bl.getRelative(0, -1, 0);
            }
        }

        BlockData brokenData = broken.getBlockData();

        if (broken.getType() == Material.AIR || broken.getType() == Material.MOVING_PISTON || Blocks
            .containsFuzzy(((Factory) getFactory()).blockBlacklist, BukkitAdapter.adapt(brokenData))) {
            return false;
        }

        if (item == null || item.equalsFuzzy(BukkitAdapter.adapt(brokenData))) {
            ICUtil.collectItem(this, above ? BlockVector3.at(0, -1, 0) : BlockVector3.at(0, 1, 0), BlockUtil.getBlockDrops(broken, null));
            broken.setType(Material.AIR);
        }

        return true;
    }

    public static class Factory extends AbstractICFactory implements ConfigurableIC {

        boolean above;

        List<BaseBlock> blockBlacklist;

        public Factory(Server server, boolean above) {

            super(server);
            this.above = above;
        }

        @Override
        public IC create(ChangedSign sign) {

            return new BlockBreaker(getServer(), sign, this);
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            if (!sign.getLine(2).trim().isEmpty()) {
                BaseBlock item = BlockSyntax.getBlock(sign.getLine(2), true);
                if (item == null)
                    throw new ICVerificationException("An invalid block was provided on line 2!");
                if (Blocks.containsFuzzy(blockBlacklist, item))
                    throw new ICVerificationException("A blacklisted block was provided on line 2!");
            }
        }

        @Override
        public String getShortDescription() {

            return "Breaks blocks " + (above ? "above" : "below") + " block sign is on.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "+oBlock ID:Data", null };
        }

        @Override
        public void addConfiguration(YAMLProcessor config, String path) {

            config.setComment("blacklist", "Stops the IC from breaking the listed blocks.");
            blockBlacklist = BlockSyntax.getBlocks(config.getStringList("blacklist", Lists.newArrayList(BlockTypes.BEDROCK.getId())), true);
        }
    }
}