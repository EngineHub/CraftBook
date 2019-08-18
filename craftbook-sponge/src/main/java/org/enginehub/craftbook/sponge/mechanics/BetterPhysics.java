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
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.LocatableSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.FallingBlock;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

@Module(id = "betterphysics", name = "BetterPhysics", onEnable="onInitialize", onDisable="onDisable")
public class BetterPhysics extends SpongeBlockMechanic implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private ConfigValue<Boolean> fallingLadders = new ConfigValue<>("falling-ladders", "Enables the 'falling ladders' physics mechanic.", true);

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        fallingLadders.load(config);
    }

    @Override
    public String getPath() {
        return "mechanics/better_physics";
    }

    @Override
    public boolean isValid(Location<World> location) {
        return fallingLadders.getValue() && FallingLadders.isValid(location);
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue<?>[] {
                fallingLadders
        };
    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event) {
        event.getTransactions().stream()
                .map(Transaction::getOriginal)
                .map(LocatableSnapshot::getLocation)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(this::checkForPhysics);
    }

    @Listener
    public void onBlockPlace(ChangeBlockEvent.Place event) {
        event.getTransactions().stream()
                .map(Transaction::getFinal)
                .map(LocatableSnapshot::getLocation)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(this::checkForPhysics);
    }

    @Listener
    public void onBlockUpdate(NotifyNeighborBlockEvent event, @First LocatableBlock source) {
        event.getNeighbors().entrySet().stream()
                .map((directionBlockStateEntry -> source.getLocation().getRelative(directionBlockStateEntry.getKey())))
                .forEach(this::checkForPhysics);
    }

    private void checkForPhysics(Location<World> block) {
        if(fallingLadders.getValue() && FallingLadders.isValid(block)) {
            Sponge.getScheduler().createTaskBuilder()
                    .execute(new FallingLadders(this, block))
                    .submit(CraftBookPlugin.spongeInst().getContainer());
        }
    }

    private static class FallingLadders implements Runnable {
        private BetterPhysics physics;
        private Location<World> ladder;

        FallingLadders(BetterPhysics physics, Location<World> ladder) {
            this.physics = physics;
            this.ladder = ladder;
        }

        public static boolean isValid(Location<?> block) {
            return block.getBlockType() == BlockTypes.LADDER && block.getRelative(Direction.DOWN).getBlockType() == BlockTypes.AIR;
        }

        @Override
        public void run () {
            if(!isValid(ladder)) return;
            FallingBlock fallingBlock = (FallingBlock) ladder.getExtent().createEntity(EntityTypes.FALLING_BLOCK, ladder.getPosition().add(0.5, 0, 0.5));
            fallingBlock.offer(Keys.FALLING_BLOCK_STATE, ladder.getBlock());
            fallingBlock.offer(Keys.CAN_PLACE_AS_BLOCK, true);
            ladder.getExtent().spawnEntity(fallingBlock);

            physics.checkForPhysics(ladder.getRelative(Direction.UP));
        }
    }
}
