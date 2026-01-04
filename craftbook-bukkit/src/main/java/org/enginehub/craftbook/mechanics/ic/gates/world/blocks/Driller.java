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

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.math.BlockVector3;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.enginehub.craftbook.bukkit.BukkitChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.ConfigurableIC;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.ICVerificationException;
import org.enginehub.craftbook.mechanics.ic.RestrictedIC;
import org.enginehub.craftbook.util.BlockUtil;
import org.enginehub.craftbook.util.ICUtil;
import org.enginehub.craftbook.util.InventoryUtil;

import java.util.concurrent.ThreadLocalRandom;

public class Driller extends AbstractSelfTriggeredIC {

    public Driller(Server server, BukkitChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    @Override
    public void think(ChipState chip) {

        if (!chip.getInput(0)) chip.setOutput(0, drill());
    }

    @Override
    public String getTitle() {

        return "Driller";
    }

    @Override
    public String getSignTitle() {

        return "DRILLER";
    }

    private int signDrillSize;
    private int signMaxDepth;

    @Override
    public void load() {
        super.load();

        signDrillSize = ((Factory) getFactory()).drillSize;

        if (!getLine(2).isEmpty()) {
            signDrillSize = Math.min(signDrillSize, Integer.parseInt(getLine(2)));
        }

        signMaxDepth = ((Factory) getFactory()).maxDrillDepth;

        if (!getLine(3).isEmpty()) {
            signMaxDepth = Math.min(signMaxDepth, Integer.parseInt(getLine(3)));
        }
    }

    public boolean drill() {

        if (ThreadLocalRandom.current().nextInt(100) < 60) return false;

        Block center = getBackBlock().getRelative(0, -1, 0);
        ItemStack tool = null;

        if (InventoryUtil.doesBlockHaveInventory(center.getRelative(0, 2, 0))) {
            InventoryHolder holder = (InventoryHolder) center.getRelative(0, 2, 0).getState();
            if (holder.getInventory().getItem(0) != null) {
                tool = holder.getInventory().getItem(0);
            }
        }

        int random = ThreadLocalRandom.current().nextInt(signDrillSize * signDrillSize);
        int x = random / signDrillSize;
        int y = random % signDrillSize;

        return drillLine(tool, center.getRelative(signDrillSize / 2 - x, 0, signDrillSize / 2 - y));
    }

    public boolean drillLine(ItemStack tool, Block blockToBreak) {

        Material brokenType = Material.AIR;
        int depth = 0;
        while (brokenType == Material.AIR) {

            if (blockToBreak.getLocation().getBlockY() == 0 || depth > signMaxDepth) return false;
            blockToBreak = blockToBreak.getRelative(0, -1, 0);
            depth += 1;
            brokenType = blockToBreak.getType();
            if (brokenType == Material.BEDROCK) return false;
        }

        ICUtil.collectItem(this, BlockVector3.at(0, 1, 0), BlockUtil.getBlockDrops(blockToBreak, tool));

        brokenType = blockToBreak.getType();
        blockToBreak.setType(Material.AIR);

        return !(brokenType == Material.LAVA || brokenType == Material.WATER);

    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) chip.setOutput(0, drill());
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC, ConfigurableIC {

        int drillSize;
        int maxDrillDepth;

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(BukkitChangedSign sign) {

            return new Driller(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Breaks a line of blocks from the IC block.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "+odrill size", "+omax depth" };
        }

        @Override
        public void addConfiguration(YAMLProcessor config, String path) {

            drillSize = config.getInt("drill-size", 3);
            maxDrillDepth = config.getInt("max-drill-depth", 256);
        }

        @Override
        public void verify(BukkitChangedSign sign) throws ICVerificationException {

            try {
                String line2 = PlainTextComponentSerializer.plainText().serialize(sign.getLine(2));
                if (!line2.isEmpty()) {
                    sign.setLine(2, Component.text(Math.min(drillSize, Integer.parseInt(line2))));
                }
                String line3 = PlainTextComponentSerializer.plainText().serialize(sign.getLine(3));
                if (!line3.isEmpty()) {
                    sign.setLine(3, Component.text(Math.min(maxDrillDepth, Integer.parseInt(line3))));
                }
            } catch (Exception e) {
                throw new ICVerificationException("Failed to parse numbers.");
            }
        }

    }
}