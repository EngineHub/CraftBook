package com.sk89q.craftbook.gates.world;

import net.minecraft.server.Packet61WorldEvent;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.util.SignUtil;

public class ParticleEffect extends AbstractIC {

    protected boolean risingEdge;

    public ParticleEffect(Server server, Sign sign, boolean risingEdge) {
        super(server, sign);
        this.risingEdge = risingEdge;
    }

    @Override
    public String getTitle() {
        return "Particle Effect";
    }

    @Override
    public String getSignTitle() {
        return "PARTICLE EFFECT";
    }

    @Override
    public void trigger(ChipState chip) {
        if (risingEdge && chip.getInput(0) || (!risingEdge && !chip.getInput(0))) {
        	doEffect(chip);
        }
    }
    
    public void doEffect(ChipState chip)
    {
    	try
    	{
	    	int effectID = Integer.parseInt(getSign().getLine(2).split(":")[0]);
	    	int effectData;
	    	try {
	    		effectData = Integer.parseInt(getSign().getLine(2).split(":")[1]);
	    	}
	    	catch(Exception e){
	    		effectData = 0;
	    	}
	    	int times = Integer.parseInt(getSign().getLine(3));
	    	Block b = SignUtil.getBackBlock(getSign().getBlock());
	    	for(int i = 0; i < times; i++)
	    		((CraftServer)getServer()).getHandle().sendPacketNearby(b.getX(), b.getY()+1,b.getZ(), 50, ((CraftWorld) getSign().getWorld()).getHandle().dimension, new Packet61WorldEvent(effectID, b.getX(), b.getY()+1,b.getZ(),effectData));
    	}
    	catch(Exception e){}
    }
    
    public static class Factory extends AbstractICFactory {

        protected boolean risingEdge;

        public Factory(Server server, boolean risingEdge) {
            super(server);
            this.risingEdge = risingEdge;
        }

        @Override
        public IC create(Sign sign) {
            return new ParticleEffect(getServer(), sign, risingEdge);
        }
    }
}
