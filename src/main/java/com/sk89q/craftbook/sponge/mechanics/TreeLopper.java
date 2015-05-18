package com.sk89q.craftbook.sponge.mechanics;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.manipulator.block.TreeData;
import org.spongepowered.api.data.type.TreeType;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.entity.player.PlayerBreakBlockEvent;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;

import com.google.common.base.Optional;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import com.sk89q.craftbook.sponge.util.LocationUtil;

public class TreeLopper extends SpongeMechanic {

    @Subscribe
    public void onBlockBreak(PlayerBreakBlockEvent event) {

        if(event.getBlock().getType() == BlockTypes.LOG || event.getBlock().getType() == BlockTypes.LOG2) {
            checkBlocks(event.getBlock(), event.getEntity(), event.getBlock().getState().getManipulator(TreeData.class).get().getValue(), new ArrayList<Location>());
        }
    }

    public void checkBlocks(Location block, Player player, TreeType type, List<Location> traversed) {

        if(traversed.contains(block)) return;

        traversed.add(block);

        Optional<TreeData> data = block.getData(TreeData.class);
        if(!data.isPresent()) return;

        if(data.get().getValue().equals(type)) { //Same tree type.
            block.digWith(player.getItemInHand().get());
            for(Direction dir : LocationUtil.getDirectFaces()) {
                checkBlocks(block.getRelative(dir), player, type, traversed);
            }
        }
    }
}
