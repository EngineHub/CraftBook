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

package org.enginehub.craftbook.mechanics.ic.gates.world.entity;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.util.InventoryUtil;
import org.enginehub.craftbook.util.SearchArea;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AnimalBreeder extends AbstractSelfTriggeredIC {

    public AnimalBreeder(Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    private SearchArea area;
    private Block chest;

    @Override
    public void load() {

        // if the line contains a = the offset is given
        // the given string should look something like that:
        // radius=x:y:z or radius, e.g. 1=-2:5:11
        area = SearchArea.createArea(getSign().getBlock(), getLine(2));

        chest = getBackBlock().getRelative(BlockFace.UP);
    }

    @Override
    public String getTitle() {
        return "Animal Breeder";
    }

    @Override
    public String getSignTitle() {
        return "ANIMAL BREEDER";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0))
            chip.setOutput(0, breed());
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void think(ChipState chip) {

        if (chip.getInput(0))
            chip.setOutput(0, breed());
    }

    @Override
    public void unload() {
        lastEntity.clear();
    }

    private Map<EntityType, Entity> lastEntity = new HashMap<>();

    public boolean breed() {

        lastEntity.clear();
        InventoryHolder inv = null;

        if (InventoryUtil.doesBlockHaveInventory(chest))
            inv = (InventoryHolder) chest.getState();

        if (inv == null)
            return false;

        for (Entity entity : area.getEntitiesInArea(Collections.singletonList(org.enginehub.craftbook.util.EntityType.MOB_PEACEFUL))) {
            if (entity.isValid() && entity instanceof Ageable) {
                if (!((Ageable) entity).canBreed() || !canBreed(entity))
                    continue;
                if (breedAnimal(inv, entity))
                    return true;
                else
                    lastEntity.put(entity.getType(), entity);
            }
        }

        return false;
    }

    public boolean canBreed(Entity entity) {

        return entity instanceof Cow || entity instanceof Sheep || entity instanceof Pig || entity instanceof Chicken || entity instanceof Wolf;
    }

    public boolean breedAnimal(InventoryHolder inv, Entity entity) {

        if (lastEntity.get(entity.getType()) != null) {

            if (entity instanceof Cow || entity instanceof Sheep) {

                if (InventoryUtil.doesInventoryContain(inv.getInventory(), false, new ItemStack(Material.WHEAT, 2))) {

                    if (InventoryUtil.removeItemsFromInventory(inv, new ItemStack(Material.WHEAT, 2))) {
                        Ageable animal = (Ageable) entity.getWorld().spawnEntity(entity.getLocation(), entity.getType());
                        animal.setBaby();
                        ((Ageable) entity).setBreed(false);
                        if (entity instanceof Sheep && animal instanceof Sheep)
                            ((Sheep) animal).setColor(((Sheep) entity).getColor());
                        return true;
                    }
                }
            } else if (entity instanceof Pig) {

                if (InventoryUtil.doesInventoryContain(inv.getInventory(), false, new ItemStack(Material.CARROT, 2))) {

                    if (InventoryUtil.removeItemsFromInventory(inv, new ItemStack(Material.CARROT, 2))) {
                        Ageable animal = (Ageable) entity.getWorld().spawnEntity(entity.getLocation(), entity.getType());
                        animal.setBaby();
                        ((Ageable) entity).setBreed(false);
                        return true;
                    }
                }
            } else if (entity instanceof Chicken) {
                if (InventoryUtil.doesInventoryContain(inv.getInventory(), false, new ItemStack(Material.WHEAT_SEEDS, 2))) {

                    if (InventoryUtil.removeItemsFromInventory(inv, new ItemStack(Material.WHEAT_SEEDS, 2))) {
                        Ageable animal = (Ageable) entity.getWorld().spawnEntity(entity.getLocation(), entity.getType());
                        animal.setBaby();
                        ((Ageable) entity).setBreed(false);
                        return true;
                    }
                }
            } else if (entity instanceof Wolf) {

                Material[] validItems = new Material[] { Material.CHICKEN, Material.COOKED_CHICKEN, Material.BEEF, Material.COOKED_BEEF,
                    Material.PORKCHOP, Material.COOKED_PORKCHOP, Material.ROTTEN_FLESH };

                for (Material item : validItems) {
                    if (InventoryUtil.doesInventoryContain(inv.getInventory(), false, new ItemStack(item, 2))) {
                        if (InventoryUtil.removeItemsFromInventory(inv, new ItemStack(item, 2))) {
                            Ageable animal = (Ageable) entity.getWorld().spawnEntity(entity.getLocation(), entity.getType());
                            animal.setBaby();
                            ((Ageable) entity).setBreed(false);
                            return true;
                        }
                    }
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
        public IC create(ChangedSign sign) {

            return new AnimalBreeder(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Breeds nearby animals.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "+oSearchArea", null };
        }
    }
}