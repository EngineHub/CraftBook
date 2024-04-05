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

import com.google.common.collect.Lists;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.blocks.Blocks;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.ConfigurableIC;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.ICVerificationException;
import org.enginehub.craftbook.util.BlockParser;

import java.util.List;

/**
 * @author Me4502
 */
public class SetBlockChest extends SetBlock {

    public SetBlockChest(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Set Block Chest";
    }

    @Override
    public String getSignTitle() {

        return "SET BLOCK CHEST";
    }

    @Override
    protected void doSet(Block body, BlockStateHolder item, boolean force) {

        if (Blocks.containsFuzzy(((Factory) getFactory()).blockBlacklist, item))
            return;

        BlockFace toPlace = ((Factory) getFactory()).above ? BlockFace.UP : BlockFace.DOWN;
        BlockFace chest = !((Factory) getFactory()).above ? BlockFace.UP : BlockFace.DOWN;

        if (force || body.getRelative(toPlace).getType() == Material.AIR) {
            if (takeFromChest(body.getRelative(chest), item.getBlockType().getItemType())) {
                body.getRelative(toPlace).setBlockData(BukkitAdapter.adapt(item));
            }
        }
    }

    public static class Factory extends AbstractICFactory implements ConfigurableIC {

        boolean above;

        public List<BaseBlock> blockBlacklist;

        public Factory(Server server, boolean above) {

            super(server);
            this.above = above;
        }

        @Override
        public IC create(ChangedSign sign) {

            return new SetBlockChest(getServer(), sign, this);
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            String line2 = PlainTextComponentSerializer.plainText().serialize(sign.getLine(2));
            if (line2.isEmpty())
                throw new ICVerificationException("A block must be provided on line 2!");
            BlockStateHolder item = BlockParser.getBlock(line2);
            if (item == null || !item.getBlockType().hasItemType())
                throw new ICVerificationException("An invalid block was provided on line 2!");
            if (Blocks.containsFuzzy(blockBlacklist, item))
                throw new ICVerificationException("A blacklisted block was provided on line 2!");
        }

        @Override
        public String getShortDescription() {

            return "Sets " + (above ? "above" : "below") + " block from " + (!above ? "above" : "below") + " chest.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "id{:data}", "+oFORCE if it should force" };
        }

        @Override
        public void addConfiguration(YAMLProcessor config, String path) {

            config.setComment("blacklist", "Stops the IC from placing the listed blocks.");
            blockBlacklist = BlockParser.getBlocks(config.getStringList("blacklist", Lists.newArrayList(BlockTypes.BEDROCK.id())), true);
        }
    }
}
