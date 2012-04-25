package com.sk89q.craftbook.gates.world;

import org.bukkit.Server;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.RestrictedIC;

public class FireBarrage extends FireShooter{

    protected boolean risingEdge;

    public FireBarrage(Server server, Sign sign, boolean risingEdge) {
        super(server, sign, risingEdge);
    }

    @Override
    public String getTitle() {
        return "Fire Barrage";
    }

    @Override
    public String getSignTitle() {
        return "FIRE BARRAGE";
    }

    @Override
    public void trigger(ChipState chip) {
        if (risingEdge && chip.getInput(0) || (!risingEdge && !chip.getInput(0))) {
        	shootFire(5);
        }
    }

    public static class Factory extends AbstractICFactory implements
            RestrictedIC {

        protected boolean risingEdge;

        public Factory(Server server, boolean risingEdge) {
            super(server);
            this.risingEdge = risingEdge;
        }

        @Override
        public IC create(Sign sign) {
            return new FireBarrage(getServer(), sign, risingEdge);
        }
    }

	
}
