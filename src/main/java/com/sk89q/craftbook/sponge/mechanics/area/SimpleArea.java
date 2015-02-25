package com.sk89q.craftbook.sponge.mechanics.area;

import javax.annotation.Nullable;

import org.spongepowered.api.block.BlockLoc;
import org.spongepowered.api.block.data.Sign;
import org.spongepowered.api.entity.EntityInteractionType;
import org.spongepowered.api.entity.living.Human;
import org.spongepowered.api.event.block.BlockUpdateEvent;
import org.spongepowered.api.event.entity.living.human.HumanInteractBlockEvent;
import org.spongepowered.api.event.entity.living.player.PlayerInteractBlockEvent;
import org.spongepowered.api.util.event.Cancellable;
import org.spongepowered.api.util.event.Subscribe;

import com.sk89q.craftbook.sponge.mechanics.SpongeMechanic;
import com.sk89q.craftbook.sponge.util.SignUtil;

public abstract class SimpleArea extends SpongeMechanic {

    @Subscribe
    public void onPlayerInteract(HumanInteractBlockEvent event) {

        if(event instanceof PlayerInteractBlockEvent && ((PlayerInteractBlockEvent) event).getInteractionType() != EntityInteractionType.RIGHT_CLICK) return;

        if(SignUtil.isSign(event.getBlock())) {

            Sign sign = event.getBlock().getData(Sign.class).get();

            if(triggerMechanic(event.getBlock(), sign, event.getHuman(), null) && event instanceof Cancellable)
                ((Cancellable) event).setCancelled(true);
        }
    }

    @Subscribe
    public void onBlockUpdate(BlockUpdateEvent event) {

        if(SignUtil.isSign(event.getBlock())) {

            Sign sign = event.getBlock().getData(Sign.class).get();

            triggerMechanic(event.getBlock(), sign, null, event.getBlock().isPowered());
        }
    }

    /**
     * Triggers the mechanic.
     * 
     * @param block The block the mechanic is being triggered at
     * @param sign The sign of the mechanic
     * @param human The triggering human, if applicable
     * @param forceState If the mechanic should forcibly enter a specific state
     */
    public abstract boolean triggerMechanic(BlockLoc block, Sign sign, @Nullable Human human, @Nullable Boolean forceState);
}
