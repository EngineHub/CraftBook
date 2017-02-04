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
package com.sk89q.craftbook.sponge.mechanics.boat;

import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.TreeType;
import org.spongepowered.api.data.type.TreeTypes;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.vehicle.Boat;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.RideEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

@Module(moduleId = "boatexitremover", moduleName = "BoatExitRemover", onEnable="onInitialize", onDisable="onDisable")
public class ExitRemover extends SpongeMechanic implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private ConfigValue<Boolean> giveItem = new ConfigValue<>("give-item", "Provide the player with the boat item.", true);

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        giveItem.load(config);
    }

    @Listener
    public void onDismount(RideEntityEvent.Dismount event, @First Player player) {
        if (event.getTargetEntity() instanceof Boat) {
            Sponge.getScheduler().createTaskBuilder().delayTicks(2)
                    .execute(new BoatRemover(player, (Boat) event.getTargetEntity()))
                    .submit(CraftBookPlugin.spongeInst());
        }
    }

    @Override
    public String getName() {
        return "Boat" + super.getName();
    }

    @Override
    public String getPath() {
        return "mechanics/boat/exitremover";
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue[] {
                giveItem
        };
    }

    class BoatRemover implements Runnable {

        Player player;
        Boat boat;

        BoatRemover(Player player, Boat boat) {
            this.player = player;
            this.boat = boat;
        }

        @Override
        public void run () {
            if(boat.isRemoved()) return;

            if(giveItem.getValue()) {
                ItemType boatType = ItemTypes.BOAT;
                TreeType treeType = boat.get(Keys.TREE_TYPE).orElse(TreeTypes.OAK);
                if (treeType == TreeTypes.OAK) {
                    boatType = ItemTypes.BOAT;
                } else if (treeType == TreeTypes.ACACIA) {
                    boatType = ItemTypes.ACACIA_BOAT;
                } else if (treeType == TreeTypes.BIRCH) {
                    boatType = ItemTypes.BIRCH_BOAT;
                } else if (treeType == TreeTypes.DARK_OAK) {
                    boatType = ItemTypes.DARK_OAK_BOAT;
                } else if (treeType == TreeTypes.JUNGLE) {
                    boatType = ItemTypes.JUNGLE_BOAT;
                } else if (treeType == TreeTypes.SPRUCE) {
                    boatType = ItemTypes.SPRUCE_BOAT;
                }

                ItemStack stack = ItemStack.of(boatType, 1);

                if(!((Player) player).getInventory().offer(stack).getRejectedItems().isEmpty()) {
                    Item item = (Item) player.getLocation().getExtent().createEntity(EntityTypes.ITEM, player.getLocation().getPosition().add(0, 1, 0));
                    item.offer(Keys.REPRESENTED_ITEM, stack.createSnapshot());
                    player.getLocation().getExtent().spawnEntity(item, CraftBookPlugin.spongeInst().getCause().build());
                }
            }

            boat.remove();
        }
    }
}
