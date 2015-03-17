package com.sk89q.craftbook.sponge.mechanics.integratedcircuits;

import org.spongepowered.api.block.BlockLoc;

public abstract class IC {

	ICType<IC> type;
	
	public IC(ICType<IC> type) {
		this.type = type;
	}
	
    public abstract BlockLoc getBlock();
    
    public ICType<IC> getType() {
    	return type;
    }
}
