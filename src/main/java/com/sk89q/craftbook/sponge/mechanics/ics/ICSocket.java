package com.sk89q.craftbook.sponge.mechanics.ics;

import java.util.HashMap;

import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tile.Sign;
import org.spongepowered.api.event.block.BlockUpdateEvent;
import org.spongepowered.api.service.persistence.data.DataContainer;
import org.spongepowered.api.service.persistence.data.DataQuery;
import org.spongepowered.api.service.persistence.data.MemoryDataContainer;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.event.Subscribe;
import org.spongepowered.api.world.Location;

import com.sk89q.craftbook.sponge.mechanics.SpongeMechanic;
import com.sk89q.craftbook.sponge.mechanics.ics.pinsets.SISO;
import com.sk89q.craftbook.sponge.util.LocationUtil;
import com.sk89q.craftbook.sponge.util.SignUtil;
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
    public IC getIC(Location block) {
        return this.getData(BaseICData.class, block).ic;
    }

    @Override
    public String getName() {
        return "IC";
    }

    @Subscribe
    public void onBlockUpdate(BlockUpdateEvent event) {

        for (Location block : event.getAffectedBlocks()) {

            if (block.getType() == BlockTypes.WALL_SIGN) {

                ICType<? extends IC> icType = ICManager.getICType(SignUtil.getTextRaw(block.getData(Sign.class).get(), 1));

                if (icType == null) continue;

                BaseICData data = getData(BaseICData.class, block);

                if (data.ic == null) {

                    // Initialize new IC.
                    data.ic = icType.buildIC(block);
                }

                Direction facing = LocationUtil.getFacing(block, event.getBlock());

                boolean powered = block.isPowered();// block.isFacePowered(facing);

                if (powered != data.ic.getPinSet().getInput(data.ic.getPinSet().getInputId(data.ic, facing), data.ic)) {
                    data.ic.getPinSet().setInput(data.ic.getPinSet().getInputId(data.ic, facing), powered, data.ic);
                    data.ic.trigger();
                }
            }
        }
    }

    public static class BaseICData extends SpongeMechanicData {

        IC ic;

        public BaseICData() {
        }

        @Override
        public DataContainer toContainer() {

            DataContainer container = new MemoryDataContainer();

            container.set(new DataQuery("icState"), ic);

            return container;
        }
    }
}
