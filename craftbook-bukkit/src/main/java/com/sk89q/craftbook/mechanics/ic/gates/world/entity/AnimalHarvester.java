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

package com.sk89q.craftbook.mechanics.ic.gates.world.entity;

import java.util.Collections;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.mechanics.ic.*;
import com.sk89q.craftbook.util.EntityType;
import com.sk89q.craftbook.util.SearchArea;

public class AnimalHarvester extends AbstractSelfTriggeredIC {

    public AnimalHarvester (Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    private SearchArea area;
    private Block chest;

    @Override
    public void load() {

        // if the line contains a = the offset is given
        // the given string should look something like that:
        // radius=x:y:z or radius, e.g. 1=-2:5:11
        area = SearchArea.createArea(CraftBookBukkitUtil.toSign(getSign()).getBlock(), getLine(2));

        chest = getBackBlock().getRelative(BlockFace.UP);
    }

    @Override
    public String getTitle () {
        return "Animal Harvester";
    }

    @Override
    public String getSignTitle () {
        return "ANIMAL HARVEST";
    }

    @Override
    public void trigger (ChipState chip) {

        if(chip.getInput(0))
            chip.setOutput(0, harvest());
    }

    @Override
    public boolean isActive () {
        return true;
    }

    @Override
    public void think (ChipState chip) {

        if(chip.getInput(0))
            chip.setOutput(0, harvest());
    }

    public boolean harvest() {

        for (Entity entity : area.getEntitiesInArea(Collections.singletonList(EntityType.MOB_PEACEFUL))) {
            if (entity.isValid() && (entity instanceof Cow || entity instanceof Sheep)) {
                if(!((Animals) entity).isAdult())
                    continue;
                if(canHarvest(entity))
                    return harvestAnimal(entity);
            }
        }

        return false;
    }

    public boolean canHarvest(Entity entity) {

        if (entity instanceof Cow) {

            if(doesChestContain(Material.BUCKET)) {

                return true;
            }
        }

        if (entity instanceof Sheep) {

            if(doesChestContain(Material.SHEARS)) {

                Sheep sh = (Sheep) entity;
                return !sh.isSheared();

            }
        }

        return false;
    }

    public boolean harvestAnimal(Entity entity) {

        if (entity instanceof Cow) {
            if(doesChestContain(Material.BUCKET)) {
                removeFromChest(Material.BUCKET);
                if(!addToChest(new ItemStack(Material.MILK_BUCKET, 1))) {
                    addToChest(new ItemStack(Material.BUCKET, 1));
                    return false;
                }

                return true;
            }
        }

        if (entity instanceof Sheep) {
            if(doesChestContain(Material.SHEARS)) {
                Sheep sh = (Sheep) entity;
                if(sh.isSheared())
                    return false;
                if (addToChest(new ItemStack(ItemUtil.getWoolFromColour(sh.getColor()), CraftBookPlugin.inst().getRandom().nextInt(2) + 1))) {
                    sh.setSheared(true);
                    return true;
                }
            }
        }

        return false;
    }

    public boolean doesChestContain(Material item) {

        if (chest.getType() == Material.CHEST) {

            Chest c = (Chest) chest.getState();
            return c.getInventory().contains(item);
        }

        return false;
    }

    public boolean addToChest(ItemStack item) {

        if (chest.getType() == Material.CHEST) {

            Chest c = (Chest) chest.getState();
            return c.getInventory().addItem(item).isEmpty();
        }

        return false;
    }

    public boolean removeFromChest(Material item) {

        if (chest.getType() == Material.CHEST) {

            Chest c = (Chest) chest.getState();
            return c.getInventory().removeItem(new ItemStack(item, 1)).isEmpty();
        }

        return false;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new AnimalHarvester(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Harvests nearby cows and sheep.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"+oSearchArea", null};
        }
    }
}
