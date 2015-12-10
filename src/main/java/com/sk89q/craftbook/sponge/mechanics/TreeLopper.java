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

    public void checkBlocks(Location<World> block, Humanoid player, TreeType type, List<Location> traversed) {
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
