package com.sk89q.craftbook.sponge.mechanics.ics.pinsets;

import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.util.SignUtil;
import org.spongepowered.api.world.Location;

public class PinsSISO extends PinSet {

    @Override
    public int getInputCount() {
        return 1;
    }

    @Override
    public int getOutputCount() {
        return 1;
    }

    @Override
    public String getName() {
        return "SISO";
    }

    @Override
    public Location getPinLocation(int id, IC ic) {
        switch(id) {
            case 0:
                return ic.getBlock().getRelative(SignUtil.getFront(ic.getBlock()));
            case 1:
                return ic.getBlock().getRelative(SignUtil.getBack(ic.getBlock())).getRelative(SignUtil.getBack(ic.getBlock()));
            default:
                return null;
        }
    }

}
