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
package org.enginehub.craftbook.sponge.mechanics.minecart;

import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import org.enginehub.craftbook.core.util.ConfigValue;
import org.enginehub.craftbook.core.util.CraftBookException;
import org.enginehub.craftbook.core.util.documentation.DocumentationProvider;
import org.enginehub.craftbook.sponge.CraftBookPlugin;
import org.enginehub.craftbook.sponge.mechanics.types.SpongeMechanic;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.vehicle.minecart.Minecart;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.RideEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

@Module(id = "minecartexitremover", name = "MinecartExitRemover", onEnable="onInitialize", onDisable="onDisable")
public class ExitRemover extends SpongeMechanic implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private ConfigValue<Boolean> giveItem = new ConfigValue<>("give-item", "Provide the player with the minecart item.", true);

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        giveItem.load(config);
    }

    @Listener
    public void onDismount(RideEntityEvent.Dismount event, @First Player player) {
        if (event.getTargetEntity() instanceof Minecart) {
            Sponge.getScheduler().createTaskBuilder().delayTicks(2)
                    .execute(new CartRemover(player, (Minecart) event.getTargetEntity()))
                    .submit(CraftBookPlugin.spongeInst());
        }
    }

    @Override
    public String getName() {
        return "Minecart" + super.getName();
    }

    @Override
    public String getPath() {
        return "mechanics/minecart/exitremover";
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue[] {
                giveItem
        };
    }

    class CartRemover implements Runnable {

        Player player;
        Minecart minecart;

        CartRemover(Player player, Minecart minecart) {
            this.player = player;
            this.minecart = minecart;
        }

        @Override
        public void run () {
            if(minecart.isRemoved()) return;

            if(giveItem.getValue() && player.gameMode().get() != GameModes.CREATIVE) {
                ItemStack stack = ItemStack.of(ItemTypes.MINECART, 1);

                if(!((Player) player).getInventory().offer(stack).getRejectedItems().isEmpty()) {
                    Item item = (Item) player.getLocation().getExtent().createEntity(EntityTypes.ITEM, player.getLocation().getPosition().add(0, 1, 0));
                    item.offer(Keys.REPRESENTED_ITEM, stack.createSnapshot());
                    player.getLocation().getExtent().spawnEntity(item);
                }
            }

            minecart.remove();
        }
    }
}
