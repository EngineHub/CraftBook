/*
 * CraftBook Copyright (C) 2010-2018 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2018 me4502 <http://www.me4502.com>
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
package com.sk89q.craftbook.sponge.mechanics.pipe;

import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.PermissionNode;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.pipe.parts.ColorConditionalPipePart;
import com.sk89q.craftbook.sponge.mechanics.pipe.parts.InputPipePart;
import com.sk89q.craftbook.sponge.mechanics.pipe.parts.OutputPipePart;
import com.sk89q.craftbook.sponge.mechanics.pipe.parts.PassthroughPipePart;
import com.sk89q.craftbook.sponge.mechanics.pipe.parts.PipePart;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import com.sk89q.craftbook.sponge.util.BlockUtil;
import com.sk89q.craftbook.sponge.util.LocationUtil;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.property.block.PoweredProperty;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Module(id = "pipes", name = "Pipes", onEnable="onInitialize", onDisable="onDisable")
public class Pipes extends SpongeBlockMechanic implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private ConfigValue<Boolean> stackPerPull = new ConfigValue<>("stack-per-pull", "Only pull a single stack from the source per usage.",true);

    private PipePart[] pipeParts;

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        stackPerPull.load(config);

        List<PipePart> pipePartList = new ArrayList<>();
        pipePartList.add(new PassthroughPipePart());
        pipePartList.add(new ColorConditionalPipePart());
        pipePartList.add(new InputPipePart());
        pipePartList.add(new OutputPipePart());

        pipeParts = pipePartList.toArray(new PipePart[pipePartList.size()]);
    }

    @Listener
    public void onBlockUpdate(NotifyNeighborBlockEvent event, @First LocatableBlock source) {
        if(isValid(source.getLocation())) {
            PoweredProperty poweredProperty = source.getLocation().getProperty(PoweredProperty.class).orElse(null);
            if(poweredProperty.getValue() != null && poweredProperty.getValue())
                performPipeAction(source.getLocation());
        }
    }

    private void performPipeAction(Location<World> location) {
        Direction direction = location.get(Keys.DIRECTION).get();
        Location<World> inventorySource = location.getRelative(direction);

        //Let's try and find a source of items!
        LocationUtil.getInventoryForLocation(inventorySource).ifPresent(inv -> {
            while (inv.peek(1).isPresent()) {
                Optional<ItemStack> itemStackOptional = inv.poll(1);
                itemStackOptional.ifPresent(itemStack -> {
                    Set<Location<World>> traversed = new HashSet<>();

                    try {
                        itemStack = doPipeIteration(location, itemStack, direction, traversed);
                    } catch (StackOverflowError e) {
                        CraftBookPlugin.spongeInst().getLogger()
                                .error("Pipe overflow. Please report this issue to the developers with images of setup.", e);
                    }

                    if (itemStack.getQuantity() > 0) {
                        for (ItemStackSnapshot snapshot : inv.offer(itemStack).getRejectedItems()) {
                            Item item = (Item) location.getExtent().createEntity(EntityTypes.ITEM, location.getPosition());
                            item.offer(Keys.REPRESENTED_ITEM, snapshot);
                            location.getExtent().spawnEntity(item);
                        }
                    }
                });
                if (stackPerPull.getValue()) {
                    break;
                }
            }
        });
    }

    private ItemStack doPipeIteration(Location<World> location, ItemStack itemStack, Direction fromDirection, Set<Location<World>> traversed) {
        if(traversed.contains(location))
            return itemStack;

        traversed.add(location);

        if(itemStack.getQuantity() == 0)
            return itemStack;

        PipePart pipePart = getPipePart(location);
        if(pipePart == null)
            return itemStack;

        for (Location<World> location1 : pipePart.findPotentialOutputs(location, itemStack, fromDirection)) {
            if (pipePart.validateOutput(location, location1, itemStack)) {
                if (pipePart instanceof OutputPipePart) {
                    Optional<Inventory> inventory = LocationUtil.getInventoryForLocation(location1);

                    if (inventory.isPresent()) {
                        InventoryTransactionResult result = inventory.get().offer(itemStack);
                        if (!result.getRejectedItems().isEmpty()) {
                            for (ItemStackSnapshot snapshot : result.getRejectedItems()) {
                                itemStack = snapshot.createStack();
                            }
                        } else {
                            itemStack.setQuantity(0);
                        }
                    }
                } else
                    itemStack = doPipeIteration(location1, itemStack, BlockUtil.getFacing(location1, location), traversed);
            }
        }

        return itemStack;
    }

    private PipePart getPipePart(Location<World> location) {
        for(PipePart pipePart : pipeParts)
            if(pipePart.isValid(location.getBlock()))
                return pipePart;
        return null;
    }

    @Override
    public boolean isValid(Location<World> location) {
        return location.getBlockType() == BlockTypes.STICKY_PISTON;
    }

    @Override
    public String getPath() {
        return "mechanics/pipes";
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue<?>[] {
            stackPerPull
        };
    }

    @Override
    public PermissionNode[] getPermissionNodes() {
        return new PermissionNode[] {

        };
    }
}
