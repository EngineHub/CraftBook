package com.sk89q.craftbook.gates.world;

import org.bukkit.Server;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.ic.SelfTriggeredIC;

public class PotionInducer extends AbstractIC implements SelfTriggeredIC{

    protected boolean risingEdge;

    public PotionInducer(Server server, Sign sign, boolean risingEdge) {
        super(server, sign);
        this.risingEdge = risingEdge;
    }

	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public String getTitle() {
		return "Potion Inducer";
	}

	@Override
	public String getSignTitle() {
		return "POTION INDUCER";
	}

	@Override
	public void trigger(ChipState chip) {
		
	}

	@Override
	public void think(ChipState state) {
        if (risingEdge && state.getInput(0) || (!risingEdge && !state.getInput(0))) {
    		for(Player p: getSign().getWorld().getPlayers())
    		{
    			int radius = 10, effectID = 1, effectAmount = 1, effectTime = 10;
    			try
    			{
	    	    	effectID = Integer.parseInt(getSign().getLine(2).split(":")[0]);
	    	    	effectAmount = Integer.parseInt(getSign().getLine(2).split(":")[1]);
	    	    	effectTime = Integer.parseInt(getSign().getLine(2).split(":")[2]);
	    	    	radius = Integer.parseInt(getSign().getLine(3));
    			}
    			catch(Exception e){}
    	    	if(p.getLocation().distance(getSign().getLocation())>radius) continue;
    			p.addPotionEffect(new PotionEffect(PotionEffectType.getById(effectID), effectTime*20, effectAmount-1),true);
    		}
        }
	}

    public static class Factory extends AbstractICFactory implements RestrictedIC{

        protected boolean risingEdge;

        public Factory(Server server, boolean risingEdge) {
            super(server);
            this.risingEdge = risingEdge;
        }

        @Override
        public IC create(Sign sign) {
            return new PotionInducer(getServer(), sign, risingEdge);
        }
    }
}
