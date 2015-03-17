package com.sk89q.craftbook.sponge.mechanics.integratedcircuits;

import java.util.HashMap;

import org.spongepowered.api.block.BlockLoc;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.event.block.BlockUpdateEvent;
import org.spongepowered.api.service.persistence.data.DataContainer;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.event.Subscribe;

import com.sk89q.craftbook.sponge.mechanics.SpongeMechanic;
import com.sk89q.craftbook.sponge.mechanics.integratedcircuits.pinsets.SISO;
import com.sk89q.craftbook.sponge.util.LocationUtil;
import com.sk89q.craftbook.sponge.util.SpongeMechanicData;

public class ICSocket extends SpongeMechanic {

    public static final HashMap<String, PinSet> PINSETS = new HashMap<String, PinSet>();

    static {
        PINSETS.put("SISO", new SISO());
    }

    /**
     * Gets the IC that is in use by this IC Socket.
     * 
     * @return The IC
     */
    public IC getIC(BlockLoc block) {
        return ((BaseICData)this.getData(block)).ic;
    }

    @Override
    public String getName() {
        return "IC";
    }

    @Subscribe
    public void onBlockUpdate(BlockUpdateEvent event) {

        for(BlockLoc block : event.getAffectedBlocks()) {

            if(block.getType() == BlockTypes.WALL_SIGN) {
                //TODO check if sign is a valid IC.

                BaseICData data = (BaseICData) getData(block);

                if(data.ic == null) {

                    //Initialize new IC.

                    //Check for alternate PinSet types.

                    data.pins = PINSETS.get(data.ic.getType().getDefaultPinSet());
                }

                Direction facing = LocationUtil.getFacing(block, event.getBlock());

                data.pins.setInput(data.pins.getInputId(data.ic, facing), block.isFacePowered(facing), data.ic);
            }
        }
    }

    public class BaseICData implements SpongeMechanicData {

        /**
         * 
         */
        private static final long serialVersionUID = -9040614175206920563L;

        IC ic;
        PinSet pins;

        @Override
        public DataContainer toContainer () {

            return null;
        }
    }
}
