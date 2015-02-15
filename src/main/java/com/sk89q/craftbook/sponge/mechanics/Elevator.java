package com.sk89q.craftbook.sponge.mechanics;

import org.spongepowered.api.block.BlockLoc;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.data.Sign;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityInteractionType;
import org.spongepowered.api.event.block.data.SignChangeEvent;
import org.spongepowered.api.event.entity.living.human.HumanInteractBlockEvent;
import org.spongepowered.api.event.entity.living.player.PlayerInteractBlockEvent;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.event.Subscribe;
import org.spongepowered.api.world.Location;

import com.flowpowered.math.vector.Vector3d;
import com.sk89q.craftbook.sponge.util.SignUtil;

public class Elevator extends SpongeMechanic {

    @Subscribe
    public void onSignChange(SignChangeEvent event) {

    }

    @Subscribe
    public void onPlayerInteract(HumanInteractBlockEvent event) {

        if(event instanceof PlayerInteractBlockEvent && ((PlayerInteractBlockEvent) event).getInteractionType() != EntityInteractionType.RIGHT_CLICK) return;

        if(event.getBlock().getType() == BlockTypes.WALL_SIGN || event.getBlock().getType() == BlockTypes.STANDING_SIGN) {

            Sign sign = event.getBlock().getData(Sign.class).get();

            boolean down = SignUtil.getTextRaw(sign, 1).equals("[Lift Down]");

            if(down || SignUtil.getTextRaw(sign, 1).equals("[Lift Up]"))
                transportEntity(event.getHuman(), event.getBlock(), down ? Direction.DOWN : Direction.UP);
        }
    }

    public void transportEntity(Entity entity, BlockLoc block, Direction direction) {

        BlockLoc destination = findDestination(block, direction);

        if(destination == block) return; //This elevator has no destination.

        BlockLoc floor = destination.getExtent().getBlock((int) Math.floor(entity.getLocation().getPosition().getX()), destination.getY() + 1, (int) Math.floor(entity.getLocation().getPosition().getZ()));
        // well, unless that's already a ceiling.
        if (floor.getType().isSolidCube()) {
            floor = floor.getRelative(Direction.DOWN);
        }

        // now iterate down until we find enough open space to stand in
        // or until we're 5 blocks away, which we consider too far.
        int foundFree = 0;
        boolean foundGround = false;
        for (int i = 0; i < 5; i++) {
            if (!floor.getType().isSolidCube()) {
                foundFree++;
            } else {
                foundGround = true;
                break;
            }
            if (floor.getY() == 0x0) {
                break;
            }
            floor = floor.getRelative(Direction.DOWN);
        }

        if (!foundGround) {
            if(entity instanceof CommandSource)
                ((CommandSource) entity).sendMessage("No floor!");
            return;
        }
        if (foundFree < 2) {
            if(entity instanceof CommandSource)
                ((CommandSource) entity).sendMessage("Obstructed!");
            return;
        }

        entity.setLocation(new Location(floor.getExtent(), new Vector3d(entity.getLocation().getPosition().getX(), floor.getLocation().getPosition().getY()+1, entity.getLocation().getPosition().getZ())));

        //entity.setLocationAndRotation(new Location(destination.getExtent(), new Vector3d(0, destination.getY(), 0)), new Vector3f(0,0,0), EnumSet.<RelativePositions>of(RelativePositions.X, RelativePositions.Z, RelativePositions.PITCH, RelativePositions.YAW));
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

                y += direction == Direction.UP ? 1 : -1;

                BlockLoc test = block.getExtent().getBlock(block.getX(), y, block.getZ());

                if(test.getType() == BlockTypes.WALL_SIGN || test.getType() == BlockTypes.STANDING_SIGN) {
                    //It's a sign.

                    Sign sign = test.getData(Sign.class).get();

                    if(SignUtil.getTextRaw(sign, 1).equals("[Lift Up]") || SignUtil.getTextRaw(sign, 1).equals("[Lift Down]") || SignUtil.getTextRaw(sign, 1).equals("[Lift]"))
                        return test;
                }
            }
        } else {
            //We don't currently support non-up/down elevators, this isn't a Roald Dahl novel.
        }

        return block;
    }
}