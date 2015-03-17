package com.sk89q.craftbook.sponge.mechanics.ics.chips.logic;

import org.spongepowered.api.block.BlockLoc;

import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.mechanics.ics.ICType;
import com.sk89q.craftbook.sponge.mechanics.ics.PinSet;

public class Repeater extends IC {

	public Repeater(ICType<IC> type, BlockLoc block) {
		super(type, block);
	}

	@Override
	public void trigger(PinSet pinset) {
		
		for(int i = 0; i < pinset.getInputCount(); i++)
			pinset.setOutput(i, pinset.getInput(i, this), this);
	}
}
