package com.sk89q.craftbook.gates.logic;

import org.bukkit.*;
import org.bukkit.block.*;

import com.sk89q.craftbook.ic.*;

public class InvertedRsNandLatch extends AbstractIC {
    public InvertedRsNandLatch(Server server, Sign sign) {
        super(server, sign);
    }
    
    @Override
    public String getTitle() {
        return "Inverted RS NAND latch";
    }
    
    @Override
    public String getSignTitle() {
        return "INV RS NAND LAT";
    }
    
    @Override
    public void trigger(ChipState chip) {
        boolean set = chip.get(1);
        boolean reset = chip.get(2);
        if (!set && !reset) {
            chip.set(3, true);
        } else if (set && !reset) {
            chip.set(3, false);
        } else if (!set && reset) {
            chip.set(3, true);
        }
    }
    
    public static class Factory extends AbstractICFactory {
        public Factory(Server server) {
            super(server);
        }
        
        @Override
        public IC create(Sign sign) {
            return new InvertedRsNandLatch(getServer(), sign);
        }
    }
}

