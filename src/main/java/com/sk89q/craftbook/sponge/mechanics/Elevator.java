package com.sk89q.craftbook.sponge.mechanics;

import java.util.EnumSet;

import org.spongepowered.api.block.BlockLoc;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.data.Sign;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityInteractionType;
import org.spongepowered.api.event.player.PlayerInteractBlockEvent;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.RelativePositions;
import org.spongepowered.api.util.event.Subscribe;
import org.spongepowered.api.world.Location;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3f;
import com.sk89q.craftbook.core.util.CachePolicy;

public class Elevator extends SpongeMechanic {

    /*@Subscribe
	public void onSignChange(SignChangeEvent event) {

	}*/

    @Subscribe
    public void onPlayerInteract(PlayerInteractBlockEvent event) {

        if(event.getInteractionType() != EntityInteractionType.RIGHT_CLICK) return;

        transportEntity(event.getPlayer(), event.getBlock(), Direction.DOWN); //Can't read signs atm, assume down.
    }

    public void transportEntity(Entity entity, BlockLoc block, Direction direction) {

        BlockLoc destination = findDestination(block, direction);

        if(destination == block) return; //This elevator has no destination.

        entity.setLocationAndRotation(new Location(destination.getExtent(), new Vector3d(0, destination.getY(), 0)), new Vector3f(0,0,0), EnumSet.<RelativePositions>of(RelativePositions.X, RelativePositions.Z, RelativePositions.PITCH, RelativePositions.YAW));
    }

    /**
     * Gets the destination of an Elevator. If there is none, it returns the start.
     * 
     * @param block The starting block.
     * @param direction The direction to move in.
     * 
     * @return The elevator destination.
     */
    private BlockLoc findDestination(BlockLoc block, Direction direction) {

        int y = block.getY();

        if(direction == Direction.UP || direction == Direction.DOWN) {

            while(direction == Direction.UP ? y < 256 : y >= 0) {
                BlockLoc test = block.getExtent().getBlock(block.getX(), y, block.getZ());

                if(test.getType() == BlockTypes.WALL_SIGN || test.getType() == BlockTypes.STANDING_SIGN) {
                    //It's a sign.

                    Sign sign = test.getData(Sign.class).get();

                    if(sign.getLine(1).equals("[Lift Up]") || sign.getLine(1).equals("[Lift Down]") || sign.getLine(1).equals("[Lift]"))
                        return test;
                }
            }
        } else {
            //We don't currently support non-up/down elevators, this isn't a Roald Dahl novel.
        }

        return block;
    }

    @Override
    public String getName () {
        return "Elevator";
    }

    @Override
    public void onInitialize () {
        // TODO Auto-generated method stub

    }

    @Override
    public CachePolicy getCachePolicy () {
        return null;
    }
}