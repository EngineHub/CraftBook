package com.sk89q.craftbook.gates.world;

import org.bukkit.Server;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.SelfTriggeredIC;

public abstract class WirelessRecieverBase extends AbstractIC implements SelfTriggeredIC {

    protected String band;
    
    public WirelessRecieverBase(Server server, Sign block) {
        super(server, block);
        
        band = block.getLine(2);
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void trigger(ChipState chip) {}

    @Override
    public void think(ChipState chip) {
        Boolean val = WirelessTransmitter.getValue(band);
        
        if (val == null)
            return;
        
        chip.setOutput(0, val);
    }

}
