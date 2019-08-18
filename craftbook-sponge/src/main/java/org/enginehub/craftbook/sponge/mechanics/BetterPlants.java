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
package org.enginehub.craftbook.sponge.mechanics;

import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import org.enginehub.craftbook.core.util.ConfigValue;
import org.enginehub.craftbook.core.util.CraftBookException;
import org.enginehub.craftbook.core.util.documentation.DocumentationProvider;
import org.enginehub.craftbook.sponge.CraftBookPlugin;
import org.enginehub.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DoublePlantTypes;
import org.spongepowered.api.data.type.PortionTypes;
import org.spongepowered.api.data.type.ShrubTypes;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.TickBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.concurrent.ThreadLocalRandom;

@Module(id = "betterplants", name = "BetterPlants", onEnable="onInitialize", onDisable="onDisable")
public class BetterPlants extends SpongeBlockMechanic implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private ConfigValue<Boolean> fernFarming = new ConfigValue<>("fern-farming", "Enables the 'fern farming' plants mechanic.", true);

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        fernFarming.load(config);

        if (fernFarming.getValue()) {
            BlockTypes.TALLGRASS.setTickRandomly(true);
        }
    }

    @Listener
    public void onTick(TickBlockEvent.Random event) {
        event.getTargetBlock().getLocation().ifPresent(worldLocation -> {
            if (isValidFernFarming(worldLocation) && ThreadLocalRandom.current().nextInt(10) == 0) {
                worldLocation.setBlock(BlockState.builder().blockType(BlockTypes.DOUBLE_PLANT).add(Keys.DOUBLE_PLANT_TYPE, DoublePlantTypes.FERN).add(Keys.PORTION_TYPE, PortionTypes.BOTTOM).build());
                worldLocation.getRelative(Direction.UP).setBlock(BlockState.builder().blockType(BlockTypes.DOUBLE_PLANT).add(Keys.DOUBLE_PLANT_TYPE, DoublePlantTypes.FERN).add(Keys.PORTION_TYPE, PortionTypes.TOP).build());
            }
        });
    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event, @First Player player) {
        event.getTransactions().stream()
                .map(Transaction::getOriginal)
                .filter(snapshot -> snapshot.getState().getType() == BlockTypes.DOUBLE_PLANT)
                .filter(snapshot -> snapshot.getState().get(Keys.DOUBLE_PLANT_TYPE).orElse(DoublePlantTypes.GRASS).equals(DoublePlantTypes.FERN))
                .filter(snapshot -> snapshot.getState().get(Keys.PORTION_TYPE).orElse(PortionTypes.BOTTOM).equals(PortionTypes.TOP))
                .forEach(snapshot -> Sponge.getScheduler().createTaskBuilder().execute(task -> {
                    snapshot.getLocation().get().getRelative(Direction.DOWN)
                            .setBlock(BlockState.builder().blockType(BlockTypes.TALLGRASS).add(Keys.SHRUB_TYPE, ShrubTypes.FERN).build());
                    if (player.get(Keys.GAME_MODE).orElse(GameModes.SURVIVAL) != GameModes.CREATIVE) {
                        Item item = (Item) snapshot.getLocation().get().getExtent().createEntity(EntityTypes.ITEM, snapshot.getPosition());
                        item.offer(Keys.REPRESENTED_ITEM, ItemStack.builder().itemType(ItemTypes.TALLGRASS).add(Keys.SHRUB_TYPE, ShrubTypes.FERN).build().createSnapshot());
                        snapshot.getLocation().get().spawnEntity(item);
                    }
                }).submit(CraftBookPlugin.spongeInst().getContainer()));
    }

    @Override
    public String getPath() {
        return "mechanics/better_plants";
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue<?>[] {
                fernFarming
        };
    }

    private boolean isValidFernFarming(Location<?> location) {
        return fernFarming.getValue()
                && location.getBlockType().equals(BlockTypes.TALLGRASS)
                && ShrubTypes.FERN.equals(location.get(Keys.SHRUB_TYPE).orElse(null))
                && location.getRelative(Direction.UP).getBlockType() == BlockTypes.AIR;
    }

    @Override
    public boolean isValid(Location<World> location) {
        return isValidFernFarming(location);
    }
}
