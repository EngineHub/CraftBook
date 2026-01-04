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

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.enginehub.craftbook.bukkit.BukkitChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.ICVerificationException;
import org.enginehub.craftbook.util.ItemUtil;
import org.enginehub.craftbook.util.SearchArea;

import java.util.Set;

public class Cultivator extends AbstractSelfTriggeredIC {

    public Cultivator(Server server, BukkitChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Cultivator";
    }

    @Override
    public String getSignTitle() {

        return "CULTIVATOR";
    }

    private SearchArea area;

    @Override
    public void load() {

        area = SearchArea.createArea(getLocation().getBlock(), getLine(2));
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) chip.setOutput(0, cultivate());
    }

    @Override
    public void think(ChipState state) {

        if (state.getInput(0)) return;

        for (int i = 0; i < 10; i++)
            state.setOutput(0, cultivate());
    }

    public boolean cultivate() {

        Block b = area.getRandomBlockInArea();

        if (b == null) return false;

        if (b.getType() == Material.DIRT || b.getType() == Material.GRASS_BLOCK) {
            if (b.getRelative(BlockFace.UP).getType() == Material.AIR && damageHoe()) {
                b.setType(Material.FARMLAND);
                return true;
            }
        }

        return false;
    }

    private static final Set<Material> hoes = Set.of(Material.WOODEN_HOE, Material.STONE_HOE, Material.IRON_HOE, Material.GOLDEN_HOE,
        Material.DIAMOND_HOE);

    public boolean damageHoe() {

        if (getBackBlock().getRelative(0, 1, 0).getType() == Material.CHEST) {
            Chest c = (Chest) getBackBlock().getRelative(0, 1, 0).getState();
            for (int slot = 0; slot < c.getInventory().getSize(); slot++) {
                if (c.getInventory().getItem(slot) == null || !hoes.contains(c.getInventory().getItem(slot).getType()))
                    continue;
                if (ItemUtil.isStackValid(c.getInventory().getItem(slot))) {
                    ItemStack item = c.getInventory().getItem(slot);
                    item.setDurability((short) (item.getDurability() + 1));
                    if (item.getDurability() > ItemUtil.getMaxDurability(item.getType()))
                        item = null;
                    c.getInventory().setItem(slot, item);
                    return true;
                }
            }
        }

        return false;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(BukkitChangedSign sign) {

            return new Cultivator(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Cultivates an area using a hoe.";
        }

        @Override
        public String[] getLongDescription() {

            return new String[] {
                "The '''MC1235''' tills farmland in the alloted radius using a hoe placed inside the above chest.",
                "This IC is part of the Farming IC family, and can be used to make a fully automated farm.",
                "",
                "== Video example ==",
                "",
                "<div style=\"text-align: center\">{{#ev:youtube|GnMfQtTAZZc|480}}</div>"
            };
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "+oSearchArea", null };
        }

        @Override
        public void verify(BukkitChangedSign sign) throws ICVerificationException {
            if (!SearchArea.isValidArea(sign.getBlock(), PlainTextComponentSerializer.plainText().serialize(sign.getLine(2))))
                throw new ICVerificationException("Invalid SearchArea on 3rd line!");
        }
    }
}