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

package org.enginehub.craftbook.mechanics.ic.gates.world.sensors;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.enginehub.craftbook.bukkit.BukkitChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.ConfigurableIC;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.ICVerificationException;
import org.enginehub.craftbook.util.BlockParser;
import org.enginehub.craftbook.util.ICUtil;
import org.enginehub.craftbook.util.RegexUtil;

public class BlockSensor extends AbstractSelfTriggeredIC {

    private Block center;
    private BlockStateHolder item;

    public BlockSensor(Server server, BukkitChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public void load() {

        center = ICUtil.parseBlockLocation(getSign());
        item = BlockParser.getBlock(getLine(3), true);
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
        public IC create(BukkitChangedSign sign) {

            return new BlockSensor(getServer(), sign, this);
        }

        @Override
        public void verify(BukkitChangedSign sign) throws ICVerificationException {

            try {
                String[] split = RegexUtil.COLON_PATTERN.split(PlainTextComponentSerializer.plainText().serialize(sign.getLine(3)), 2);
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

            return new String[] { "x:y:z", "id:data" };
        }

        @Override
        public void addConfiguration(YAMLProcessor config, String path) {

            invert = config.getBoolean("invert-output", false);
        }
    }
}