package com.sk89q.craftbook.sponge.mechanics.ics;

import java.util.HashMap;

import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.block.BlockUpdateEvent;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;

import com.sk89q.craftbook.sponge.mechanics.ics.pinsets.SISO;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import com.sk89q.craftbook.sponge.st.SelfTriggerManager;
import com.sk89q.craftbook.sponge.st.SelfTriggeringMechanic;
import com.sk89q.craftbook.sponge.util.LocationUtil;
import com.sk89q.craftbook.sponge.util.SignUtil;
import com.sk89q.craftbook.sponge.util.SpongeMechanicData;

public class ICSocket extends SpongeBlockMechanic implements SelfTriggeringMechanic {

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
        return createICData(block).ic;
    }

    @Override
    public String getName() {
        return "IC";
    }

    @Subscribe
    public void onBlockUpdate(BlockUpdateEvent event) {

        for (Location block : event.getAffectedBlocks()) {

            BaseICData data = createICData(block);
            if (data == null) continue;

            Direction facing = LocationUtil.getFacing(block, event.getBlock());

            boolean powered = block.isPowered();// block.isFacePowered(facing);

            if (powered != data.ic.getPinSet().getInput(data.ic.getPinSet().getInputId(data.ic, facing), data.ic)) {
                data.ic.getPinSet().setInput(data.ic.getPinSet().getInputId(data.ic, facing), powered, data.ic);
                data.ic.trigger();
            }
        }
    }

    @Override
    public void onThink(Location block) {

        BaseICData data = createICData(block);
        if (data == null) return;
        if (!(data.ic instanceof SelfTriggeringIC)) return;
        ((SelfTriggeringIC) data.ic).think();
    }

    @Override
    public boolean isValid(Location location) {
        return createICData(location) != null;
    }

    public BaseICData createICData(Location block) {

        if (block.getType() == BlockTypes.WALL_SIGN) {
            ICType<? extends IC> icType = ICManager.getICType(SignUtil.getTextRaw(((Sign) block.getTileEntity().get()).getData().get(), 1));

            if (icType == null) return null;

            BaseICData data = getData(BaseICData.class, block);

            if (data.ic == null) {

                // Initialize new IC.
                data.ic = icType.buildIC(block);
                if (data.ic instanceof SelfTriggeringIC) SelfTriggerManager.register(this, block);
            }

            return data;
        }

        return null;
    }

    public static class BaseICData extends SpongeMechanicData {

        IC ic;

        public BaseICData() {
        }

        @Override
        public DataContainer toContainer() {

            DataContainer container = new MemoryDataContainer();

            container.set(DataQuery.of("icState"), ic);

            return container;
        }
    }
}
