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

import com.sk89q.worldedit.math.BlockVector3;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.enginehub.craftbook.bukkit.BukkitChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.ICVerificationException;
import org.enginehub.craftbook.util.BlockUtil;
import org.enginehub.craftbook.util.ICUtil;
import org.enginehub.craftbook.util.SearchArea;

public class CombineHarvester extends AbstractSelfTriggeredIC {

    public CombineHarvester(Server server, BukkitChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    SearchArea area;

    @Override
    public void load() {

        area = SearchArea.createArea(getLocation().getBlock(), getLine(2));
    }

    @Override
    public String getTitle() {

        return "Combine Harvester";
    }

    @Override
    public String getSignTitle() {

        return "HARVEST";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) chip.setOutput(0, harvest());
    }

    @Override
    public void think(ChipState chip) {

        if (chip.getInput(0)) return;

        for (int i = 0; i < 10; i++)
            chip.setOutput(0, harvest());
    }

    public boolean harvest() {

        Block b = area.getRandomBlockInArea();

        if (b == null) return false;

        if (harvestable(b)) {
            ICUtil.collectItem(this, BlockVector3.at(0, 1, 0), BlockUtil.getBlockDrops(b, null));
            b.setType(Material.AIR);
            return true;
        }
        return false;
    }

    public boolean harvestable(Block block) {
        Material above = block.getRelative(0, 1, 0).getType();
        Material below = block.getRelative(0, -1, 0).getType();
        switch (block.getType()) {
            case WHEAT:
            case CARROTS:
            case POTATOES:
            case BEETROOTS:
            case NETHER_WART:
            case COCOA:
                Ageable ageable = (Ageable) block.getBlockData();
                return ageable.getAge() == ageable.getMaximumAge();
            case CACTUS_FLOWER:
                return below == Material.CACTUS;
            case CACTUS:
                return below == Material.CACTUS && above != Material.CACTUS;
            case SUGAR_CANE:
                return below == Material.SUGAR_CANE && above != Material.SUGAR_CANE;
            case VINE:
                return above == Material.VINE && below != Material.VINE;
            case MELON:
            case PUMPKIN:
                return true;
            case BAMBOO:
                return below == Material.BAMBOO;
            default:
                return Tag.LOGS.isTagged(block.getType());
        }
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(BukkitChangedSign sign) {

            return new CombineHarvester(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Harvests nearby crops.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "SearchArea", null };
        }

        @Override
        public void verify(BukkitChangedSign sign) throws ICVerificationException {
            if (!SearchArea.isValidArea(sign.getBlock(), PlainTextComponentSerializer.plainText().serialize(sign.getLine(2))))
                throw new ICVerificationException("Invalid SearchArea on 3rd line!");
        }
    }
}