/*
 * CraftBook Copyright (C) 2010-2016 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2016 me4502 <http://www.me4502.com>
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
package com.sk89q.craftbook.sponge.mechanics;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import com.sk89q.craftbook.sponge.util.BlockFilter;
import com.sk89q.craftbook.sponge.util.BlockUtil;
import com.sk89q.craftbook.sponge.util.type.BlockFilterListTypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.TreeType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.filter.cause.Named;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Module(moduleName = "TreeLopper", onEnable="onInitialize", onDisable="onDisable")
public class TreeLopper extends SpongeMechanic {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private ConfigValue<List<BlockFilter>> allowedBlocks = new ConfigValue<>("allowed-blocks", "A list of blocks that are logs.", getDefaultBlocks(), new BlockFilterListTypeToken());

    @Override
    public void onInitialize() {
        allowedBlocks.load(config);
    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event, @Named(NamedCause.SOURCE) Player player) {
        event.getTransactions().stream().filter((t) -> BlockUtil.doesStatePassFilters(allowedBlocks.getValue(), t.getOriginal().getState())).forEach((transaction) -> {
            checkBlocks(transaction.getOriginal().getLocation().get(), player, new ArrayList<>());
        });
    }

    private static void checkBlocks(Location<World> block, Player player, List<Location> traversed) {
        if(traversed.contains(block)) return;

        traversed.add(block);

        block.setBlockType(BlockTypes.AIR);
        for(Direction dir : BlockUtil.getDirectFaces()) {
            checkBlocks(block.getRelative(dir), player, traversed);
        }
    }

    private static List<BlockFilter> getDefaultBlocks() {
        List<BlockFilter> states = Lists.newArrayList();
        states.add(new BlockFilter("LOG"));
        states.add(new BlockFilter("LOG2"));
        return states;
    }
}
