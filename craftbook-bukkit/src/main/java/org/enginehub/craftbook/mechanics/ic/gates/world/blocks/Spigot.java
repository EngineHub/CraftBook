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

package com.sk89q.craftbook.mechanics.ic.gates.world.blocks;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractIC;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.RestrictedIC;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.InventoryUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.worldedit.math.Vector3;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;

public class Spigot extends AbstractIC {

    private Vector3 radius;
    private Location offset;

    public Spigot(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public void load() {
        radius = ICUtil.parseRadius(getSign());
        offset = ICUtil.parseBlockLocation(getSign()).getLocation();
    }

    @Override
    public String getTitle() {

        return "Spigot";
    }

    @Override
    public String getSignTitle() {

        return "SPIGOT";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            chip.setOutput(0, search());
        }
    }

    public boolean search() {
        Block chest = getBackBlock().getRelative(0, -1, 0);

        if (InventoryUtil.doesBlockHaveInventory(chest)) {
            InventoryHolder c = (InventoryHolder) chest.getState();
            Block off = offset.getBlock();
            ArrayList<Location> searched = new ArrayList<>();
            return searchAt(c, searched, off);
        } else {
            return false;
        }
    }

    public boolean searchAt(InventoryHolder chest, ArrayList<Location> searched, Block off) {

        if (searched.contains(off.getLocation())) return false;
        searched.add(off.getLocation());
        if (!LocationUtil.isWithinRadius(off.getLocation(), offset, radius)) return false;
        if (off.getType() == Material.AIR) {
            Material m = getFromChest(chest);
            if (m == Material.AIR) return false;
            off.setType(unparse(m));
            return true;
        } else if (off.isLiquid()) {
            if (off.getData() != 0x0) { // Moving
                Material m = getFromChest(chest, off.getType());
                if (m == Material.AIR) return false;
                off.setType(unparse(m));
                return true;
            } else { // Still
                return searchAt(chest, searched, off.getRelative(1, 0, 0))
                        || searchAt(chest, searched, off.getRelative(-1, 0, 0))
                        || searchAt(chest, searched, off.getRelative(0, 0, 1))
                        || searchAt(chest, searched, off.getRelative(0, 0, -1))
                        || searchAt(chest, searched, off.getRelative(0, 1, 0))
                        || searchAt(chest, searched, off.getRelative(0, -1, 0));
            }
        }

        return false;
    }

    public Material getFromChest(InventoryHolder holder) {
        HashMap<Integer, ItemStack> over = holder.getInventory().removeItem(new ItemStack(Material.WATER_BUCKET, 1));
        if (over.isEmpty()) {
            holder.getInventory().addItem(new ItemStack(Material.BUCKET));
            return Material.WATER;
        }
        over = holder.getInventory().removeItem(new ItemStack(Material.LAVA_BUCKET, 1));
        if (over.isEmpty()) {
            holder.getInventory().addItem(new ItemStack(Material.BUCKET));
            return Material.LAVA;
        }

        return Material.AIR;
    }

    public Material getFromChest(InventoryHolder holder, Material m) {
        m = parse(m);

        HashMap<Integer, ItemStack> over = holder.getInventory().removeItem(new ItemStack(m, 1));
        if (over.isEmpty()) {
            holder.getInventory().addItem(new ItemStack(Material.BUCKET));
            return unparse(m);
        }

        return Material.AIR;
    }

    public static Material parse(Material mat) {
        if (mat == Material.WATER || mat == Material.WATER_BUCKET) return Material.WATER_BUCKET;
        if (mat == Material.LAVA || mat == Material.LAVA_BUCKET) return Material.LAVA_BUCKET;
        return Material.AIR;
    }

    public static Material unparse(Material mat) {
        if (mat == Material.WATER_BUCKET || mat == Material.WATER) return Material.WATER;
        if (mat == Material.LAVA_BUCKET || mat == Material.LAVA) return Material.LAVA;
        return Material.AIR;
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new Spigot(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Fills areas with liquid from below chest.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"+oradius=x:y:z offset", null};
        }
    }
}