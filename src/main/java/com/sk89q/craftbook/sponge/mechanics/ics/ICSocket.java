package com.sk89q.craftbook.sponge.mechanics.ics;

import java.util.HashMap;

import org.spongepowered.api.block.BlockLoc;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.data.Sign;
import org.spongepowered.api.event.block.BlockUpdateEvent;
import org.spongepowered.api.service.persistence.data.DataContainer;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.event.Subscribe;

import com.sk89q.craftbook.sponge.mechanics.SpongeMechanic;
import com.sk89q.craftbook.sponge.mechanics.ics.pinsets.SISO;
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
        return ((BaseICData) this.getData(block)).ic;
    }

    @Override
    public String getName() {
        return "IC";
    }

    @Subscribe
    public void onBlockUpdate(BlockUpdateEvent event) {

        for (BlockLoc block : event.getAffectedBlocks()) {

            if (block.getType() == BlockTypes.WALL_SIGN) {
                ICType<? extends IC> icType = ICManager.getICType(block.getData(Sign.class).get().getLine(1).toLegacy());

                if (icType == null) continue;

                BaseICData data = (BaseICData) getData(block);

                if (data.ic == null) {

                    // Initialize new IC.
                    data.ic = icType.buildIC(block);

                    // Check for alternate PinSet types.

                    data.pins = PINSETS.get(data.ic.getType().getDefaultPinSet());
                }

                Direction facing = LocationUtil.getFacing(block, event.getBlock());

                data.pins.setInput(data.pins.getInputId(data.ic, facing), block.isFacePowered(facing), data.ic);
                data.ic.trigger(data.pins);
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
        public DataContainer toContainer() {

            return null;
        }
    }
}
