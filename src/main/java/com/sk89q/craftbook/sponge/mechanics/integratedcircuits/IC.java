package com.sk89q.craftbook.sponge.mechanics.integratedcircuits;

import org.spongepowered.api.block.BlockLoc;

public abstract class IC {

	ICType type;
	
	public IC(ICType type) {
		this.type = type;
	}
	
    public abstract BlockLoc getBlock();
    
    public ICType getType() {
    	return type;
    }
}
