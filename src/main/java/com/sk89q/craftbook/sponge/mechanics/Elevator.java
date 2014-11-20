package com.sk89q.craftbook.sponge.mechanics;

import org.spongepowered.api.block.Block;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.util.Direction;

@Mechanic(name = "Elevator")
public class Elevator {

	/*@Subscribe
	public void onSignChange(SignChangeEvent event) {

	}

	@Subscribe
	public void onPlayerInteract(PlayerInteractEvent event) {

		transportEntity(event.getPlayer(), event.getBlock(), Direction.DOWN); //Can't read signs atm, assume down.
	}*/

	public void transportEntity(Entity entity, Block block, Direction direction) {

		Block destination = findDestination(block, direction);

		//TODO
	}

	/**
	 * Gets the destination of an Elevator. If there is none, it returns the start.
	 * 
	 * @param block The starting block.
	 * @param direction The direction to move in.
	 * 
	 * @return The elevator destination.
	 */
	private Block findDestination(Block block, Direction direction) {

		int y = block.getY();

		if(direction == Direction.UP || direction == Direction.DOWN) {

			while(direction == Direction.UP ? y < 256 : y >= 0) {
				Block test = block.getExtent().getBlock(block.getX(), y, block.getZ());

				if(test.getType() == BlockTypes.WALL_SIGN || test.getType() == BlockTypes.STANDING_SIGN) {
					//It's a sign.

					//TODO Can't go any further yet with Sponge. Assume it's an elevator.
					return test;
				}
			}
		} else {
			//We don't currently support non-up/down elevators, this isn't a Roald Dahl novel.
		}

		return block;
	}
}