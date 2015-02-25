package com.sk89q.craftbook.sponge.mechanics.integratedcircuits;

import org.spongepowered.api.event.block.BlockUpdateEvent;
import org.spongepowered.api.util.event.Subscribe;

import com.sk89q.craftbook.sponge.mechanics.SpongeMechanic;

public class ICSocket extends SpongeMechanic {

    IC ic;

    /**
     * Gets the IC that is in use by this IC Socket.
     * 
     * @return The IC
     */
    public IC getIC() {
        return ic;
    }

    @Override
    public String getName() {
        return "IC";
    }

    @Subscribe
    public void onBlockUpdate(BlockUpdateEvent event) {

    }

    /*public class BaseICData implements SpongeMechanicData {

        boolean[] inputStates;

        @Override
        public DataContainer toContainer () {

            DataContainer container = CraftBookPlugin.<CraftBookPlugin>inst().game.getServiceManager().

            return null;
        }

        @Override
        public void serialize (DataSource source) {

            source.serialize(this);
        }

    }*/
}
