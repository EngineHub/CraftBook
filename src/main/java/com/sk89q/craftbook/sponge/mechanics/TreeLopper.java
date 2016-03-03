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

import com.me4502.modularframework.module.Module;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import com.sk89q.craftbook.sponge.util.BlockUtil;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.TreeType;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Module(moduleName = "TreeLopper", onEnable="onInitialize", onDisable="onDisable")
public class TreeLopper extends SpongeMechanic {

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event) {

        Humanoid human;
        if(event.getCause().first(Humanoid.class).isPresent())
            human = event.getCause().first(Humanoid.class).get();
        else
            return;

        event.getTransactions().forEach((transaction) -> {
            if(transaction.getOriginal().getState().getType() == BlockTypes.LOG || transaction.getOriginal().getState().getType() == BlockTypes.LOG2) {
                checkBlocks(transaction.getOriginal().getLocation().get(), human, transaction.getOriginal().get(Keys.TREE_TYPE).get(), new ArrayList<>());
            }
        });
    }

    private void checkBlocks(Location<World> block, Humanoid player, TreeType type, List<Location> traversed) {
        if(traversed.contains(block)) return;

        traversed.add(block);

        Optional<TreeType> data = block.getBlock().get(Keys.TREE_TYPE);
        if(!data.isPresent()) return;

        if(data.get().equals(type)) { //Same tree type.
            //TODO FIX THIS block.digBlockWith(player.getItemInHand().get());
            for(Direction dir : BlockUtil.getDirectFaces()) {
                checkBlocks(block.getRelative(dir), player, type, traversed);
            }
        }
    }
}
