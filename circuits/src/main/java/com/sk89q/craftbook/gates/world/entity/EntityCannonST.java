package com.sk89q.craftbook.gates.world.entity;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.ic.SelfTriggeredIC;

public class EntityCannonST extends EntityCannon implements SelfTriggeredIC {

    public EntityCannonST (Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    @Override
    public String getTitle () {

        return "Self-Triggered Entity Cannon";
    }

    @Override
    public String getSignTitle () {

        return "ENTITY CANNON ST";
    }

    @Override
    public boolean isActive () {
        return true;
    }

    @Override
    public void think (ChipState state) {
        state.setOutput(0, shoot());
    }

    public static class Factory extends EntityCannon.Factory implements RestrictedIC {

        public Factory (Server server) {

            super(server);
        }

        @Override
        public IC create (ChangedSign sign) {

            return new EntityCannonST(getServer(), sign, this);
        }
    }
}