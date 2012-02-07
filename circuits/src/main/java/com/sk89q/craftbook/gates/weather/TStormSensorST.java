package com.sk89q.craftbook.gates.weather;


import org.bukkit.Server;
import org.bukkit.block.Sign;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.SelfTriggeredIC;

public class TStormSensorST extends AbstractIC implements SelfTriggeredIC {

    protected boolean risingEdge;

    public TStormSensorST(Server server, Sign sign, boolean risingEdge) {
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
    public void trigger(ChipState chip)
    {}
    
    @Override
    public void think(ChipState chip) {
    	chip.setOutput(0, getSign().getWorld().isThundering());
    }
    
	@Override
	public boolean isActive() {
		return true;
	}

    public static class Factory extends AbstractICFactory {

        protected boolean risingEdge;

        public Factory(Server server, boolean risingEdge) {
            super(server);
            this.risingEdge = risingEdge;
        }

        @Override
        public IC create(Sign sign) {
            return new TStormSensorST(getServer(), sign, risingEdge);
        }
    }

}
