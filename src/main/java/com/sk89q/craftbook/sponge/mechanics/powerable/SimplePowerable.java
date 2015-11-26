package com.sk89q.craftbook.sponge.mechanics.powerable;

import com.sk89q.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.property.block.PoweredProperty;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.world.Location;

public abstract class SimplePowerable extends SpongeBlockMechanic {

    public abstract void updateState(Location<?> location, boolean powered);

    @Listener
    public void onBlockUpdate(NotifyNeighborBlockEvent event) {

        BlockSnapshot source;
        if(event.getCause().first(BlockSnapshot.class).isPresent())
            source = event.getCause().first(BlockSnapshot.class).get();
        else
            return;

        if(isValid(source.getLocation().get())) {
            PoweredProperty poweredProperty = source.getLocation().get().getProperty(PoweredProperty.class).orElse(null);
            updateState(source.getLocation().get(), poweredProperty == null ? false : poweredProperty.getValue());
        }
    }
}
