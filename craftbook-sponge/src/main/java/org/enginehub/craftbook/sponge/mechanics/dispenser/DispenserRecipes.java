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
package org.enginehub.craftbook.sponge.mechanics.dispenser;

import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import org.enginehub.craftbook.util.ConfigValue;
import org.enginehub.craftbook.util.CraftBookException;
import org.enginehub.craftbook.util.documentation.DocumentationProvider;
import org.enginehub.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.carrier.Dispenser;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

@Module(id = "dispenserrecipes", name = "DispenserRecipes", onEnable = "onInitialize", onDisable = "onDisable")
public class DispenserRecipes extends SpongeBlockMechanic implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private List<DispenserRecipe> recipes = new ArrayList<>();

    private ConfigValue<Boolean> cannon = new ConfigValue<>("cannon", "Enables the 'Cannon' dispenser recipe.", true);
    private ConfigValue<Boolean> fan = new ConfigValue<>("fan", "Enables the 'Fan' dispenser recipe.", true);
    private ConfigValue<Boolean> vacuum = new ConfigValue<>("vacuum", "Enables the 'Vacuum' dispenser recipe.", true);
    private ConfigValue<Boolean> fireArrows = new ConfigValue<>("fire-arrows", "Enables the 'Fire Arrows' dispenser recipe.", true);
    private ConfigValue<Boolean> snowShooter = new ConfigValue<>("snow-shooter", "Enables the 'Snow Shooter' dispenser recipe.", true);
    private ConfigValue<Boolean> xpShooter = new ConfigValue<>("xp-shooter", "Enables the 'XP Shooter' dispenser recipe.", true);

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        cannon.load(config);
        fan.load(config);
        vacuum.load(config);
        fireArrows.load(config);
        snowShooter.load(config);
        xpShooter.load(config);

        if (cannon.getValue()) {
            recipes.add(new Cannon());
        }
        if (fan.getValue()) {
            recipes.add(new Fan());
        }
        if (vacuum.getValue()) {
            recipes.add(new Vacuum());
        }
        if (fireArrows.getValue()) {
            recipes.add(new FireArrows());
        }
        if (snowShooter.getValue()) {
            recipes.add(new SnowShooter());
        }
        if (xpShooter.getValue()) {
            recipes.add(new XPShooter());
        }
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
        if (handleDispenserAction(dispenserTile)) {
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
            if (handleDispenserAction(dispenserTile)) {
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
        if (handleDispenserAction(dispenserTile)) {
            event.setCancelled(true);
        }
    }

    public boolean handleDispenserAction(Dispenser dispenser) {
        for (DispenserRecipe recipe : recipes) {
            ItemStack[] items = StreamSupport.stream(dispenser.getInventory().slots().spliterator(), false)
                    .map(Inventory::peek)
                    .map(opt -> opt.orElse(ItemStack.empty()))
                    .toArray(ItemStack[]::new);
            if (recipe.doesPass(items)) {
                if (recipe.doAction(dispenser, items)) {
                    dispenser.getInventory().slots().forEach(inv -> inv.poll(1)); // Take one of every stack.
                    return true;
                }

                break;
            }
        }

        return false;
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue[] {
                cannon,
                fan,
                vacuum,
                fireArrows,
                snowShooter,
                xpShooter
        };
    }
}
