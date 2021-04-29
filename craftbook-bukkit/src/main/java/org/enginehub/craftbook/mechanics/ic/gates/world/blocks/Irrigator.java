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

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.bukkit.util.CraftBookBukkitUtil;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.ICVerificationException;
import org.enginehub.craftbook.util.InventoryUtil;
import org.enginehub.craftbook.util.SearchArea;

import java.util.HashMap;

public class Irrigator extends AbstractSelfTriggeredIC {

    public Irrigator(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    private SearchArea area;

    @Override
    public void load() {

        area = SearchArea.createArea(getLocation().getBlock(), getLine(2));
    }

    @Override
    public String getTitle() {

        return "Irrigator";
    }

    @Override
    public String getSignTitle() {

        return "IRRIGATOR";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) chip.setOutput(0, irrigate());
    }

    @Override
    public void think(ChipState chip) {

        if (chip.getInput(0)) return;

        for (int i = 0; i < 10; i++)
            chip.setOutput(0, irrigate());
    }

    public boolean irrigate() {

        Block b = area.getRandomBlockInArea();

        if (b == null) return false;

        if (b.getType() == Material.FARMLAND) {
            Farmland farmland = (Farmland) b.getBlockData();
            if (farmland.getMoisture() < 1 && consumeWater()) {
                farmland.setMoisture(farmland.getMaximumMoisture());
                b.setBlockData(farmland);
                return true;
            }
        }
        return false;
    }

    public boolean consumeWater() {
        Block chest = getBackBlock().getRelative(0, 1, 0);
        if (InventoryUtil.doesBlockHaveInventory(chest)) {
            InventoryHolder c = (InventoryHolder) chest.getState();
            HashMap<Integer, ItemStack> over = c.getInventory().removeItem(new ItemStack(Material.WATER_BUCKET, 1));
            if (over.isEmpty()) {
                c.getInventory().addItem(new ItemStack(Material.BUCKET, 1));
                return true;
            }
        } else if (chest.getType() == Material.WATER) {
            chest.setType(Material.AIR);
            return true;
        }

        return false;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new Irrigator(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Irrigates nearby farmland using water in above chest.";
        }

        @Override
        public String[] getLongDescription() {

            return new String[] {
                "The '''MC1238''' irrigates soil in the selected search area with water found in the chest above the IC.",
                "This IC is part of the Farming IC family, and can be used to make a fully automated farm."
            };
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "+oSearchArea", null };
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {
            if (!SearchArea.isValidArea(CraftBookBukkitUtil.toSign(sign).getBlock(), sign.getLine(2)))
                throw new ICVerificationException("Invalid SearchArea on 3rd line!");
        }
    }
}