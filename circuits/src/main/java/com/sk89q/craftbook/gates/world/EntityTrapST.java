package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ic.*;
import org.bukkit.Server;
import org.bukkit.block.Sign;

public class EntityTrapST extends EntityTrap implements SelfTriggeredIC {

    public EntityTrapST(Server server, Sign sign) {
        super(server, sign);
    }

    @Override
    public String getTitle() {

        return "Self-triggered Entity Trap";
    }

    @Override
    public String getSignTitle() {

        return "ST ENTITY TRAP";
    }

    @Override
    public void think(ChipState chip) {

        chip.setOutput(0, hurt());
    }


    public static class Factory extends AbstractICFactory implements
            RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new EntityTrapST(getServer(), sign);
        }
    }

    @Override
    public boolean isActive() {

        return true;
    }

    @Override
    public void trigger(ChipState chip) {

    }

}
