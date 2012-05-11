package com.sk89q.craftbook.gates.weather;


import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import org.bukkit.Server;
import org.bukkit.block.Sign;

public class TStormSensor extends AbstractIC {

    protected boolean risingEdge;

    public TStormSensor(Server server, Sign sign, boolean risingEdge) {

        super(server, sign);
        this.risingEdge = risingEdge;
    }

    @Override
    public String getTitle() {

        return "Is It a Storm";
    }

    @Override
    public String getSignTitle() {

        return "IS IT A STORM";
    }

    @Override
    public void trigger(ChipState chip) {

        if (risingEdge && chip.getInput(0) || (!risingEdge && !chip.getInput(0))) {
            chip.setOutput(0, getSign().getWorld().isThundering());
        }
    }


    public static class Factory extends AbstractICFactory {

        protected boolean risingEdge;

        public Factory(Server server, boolean risingEdge) {

            super(server);
            this.risingEdge = risingEdge;
        }

        @Override
        public IC create(Sign sign) {

            return new TStormSensor(getServer(), sign, risingEdge);
        }
    }

}
