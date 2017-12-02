/*
 * CraftBook Copyright (C) 2010-2017 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2017 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
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
package com.sk89q.craftbook.sponge.mechanics.dispenser;

import com.flowpowered.math.vector.Vector3d;
import com.me4502.modularframework.module.Module;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.carrier.Dispenser;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnType;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.filter.cause.ContextValue;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;

@Module(id = "dispenserrecipes", name = "DispenserRecipes", onEnable = "onInitialize", onDisable = "onDisable")
public class DispenserRecipes extends SpongeBlockMechanic implements DocumentationProvider {

    private List<DispenserRecipe> recipes = new ArrayList<>();

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        recipes.add(new Cannon());
        recipes.add(new Fan());
        recipes.add(new Vacuum());
    }

    @Override
    public String getPath() {
        return "mechanics/dispenser_recipes";
    }

    @Override
    public boolean isValid(Location<World> location) {
        return location.getBlockType() == BlockTypes.DISPENSER;
    }

    @Listener
    public void onDispense(DropItemEvent.Dispense event, @First LocatableBlock dispenser) {
        if (dispenser.getBlockState().getType() != BlockTypes.DISPENSER) {
            return;
        }

        Dispenser dispenserTile = (Dispenser) dispenser.getLocation().getTileEntity().get();
        if (handleDispenserAction(dispenserTile, event.getEntities().get(0).getVelocity())) {
            event.setCancelled(true);
        }
    }

    @Listener
    public void onCreateEntity(SpawnEntityEvent event, @First LocatableBlock dispenser) {
        event.getContext().get(EventContextKeys.SPAWN_TYPE).ifPresent(spawnType -> {
            if (spawnType != SpawnTypes.DISPENSE || dispenser.getBlockState().getType() != BlockTypes.DISPENSER) {
                return;
            }

            Dispenser dispenserTile = (Dispenser) dispenser.getLocation().getTileEntity().get();
            if (handleDispenserAction(dispenserTile, event.getEntities().get(0).getVelocity())) {
                event.setCancelled(true);
            }
        });
    }

    @Listener
    public void onChangeBlock(ChangeBlockEvent event, @First LocatableBlock dispenser) {
        if (dispenser.getBlockState().getType() != BlockTypes.DISPENSER) {
            return;
        }

        Dispenser dispenserTile = (Dispenser) dispenser.getLocation().getTileEntity().get();
        if (handleDispenserAction(dispenserTile, null)) {
            event.setCancelled(true);
        }
    }

    public boolean handleDispenserAction(Dispenser dispenser, @Nullable Vector3d velocity) {
        for (DispenserRecipe recipe : recipes) {
            ItemStack[] items = StreamSupport.stream(dispenser.getInventory().slots().spliterator(), false)
                    .map(Inventory::peek).filter(Optional::isPresent)
                    .map(Optional::get).toArray(ItemStack[]::new);
            if (recipe.doesPass(items)) {
                if (recipe.doAction(dispenser, items, velocity)) {
                    dispenser.getInventory().slots().forEach(inv -> inv.poll(1)); // Take one of every stack.
                    return true;
                }

                break;
            }
        }

        return false;
    }
}
