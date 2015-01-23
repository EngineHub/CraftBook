package com.sk89q.craftbook.sponge.mechanics.area;

import org.spongepowered.api.block.BlockLoc;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.EntityInteractionType;
import org.spongepowered.api.event.entity.living.human.HumanInteractBlockEvent;
import org.spongepowered.api.event.entity.living.player.PlayerInteractBlockEvent;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.event.Subscribe;

import com.sk89q.craftbook.core.util.CachePolicy;
import com.sk89q.craftbook.sponge.mechanics.SpongeMechanic;

public class Bridge extends SpongeMechanic {

    @Override
    public String getName () {
        return "Bridge";
    }

    @Subscribe
    public void onPlayerInteract(HumanInteractBlockEvent event) {

        if(event instanceof PlayerInteractBlockEvent && ((PlayerInteractBlockEvent) event).getInteractionType() != EntityInteractionType.RIGHT_CLICK) return;

        if(event.getBlock().getType() == BlockTypes.WALL_SIGN || event.getBlock().getType() == BlockTypes.STANDING_SIGN) {

            BlockLoc baseBlock = event.getBlock().getRelative(Direction.DOWN);


        }
    }

    @Override
    public CachePolicy getCachePolicy () {
        return null;
    }

}