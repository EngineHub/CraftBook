package com.sk89q.craftbook.sponge.mechanics;

import com.flowpowered.math.vector.Vector3d;
import com.me4502.modularframework.module.Module;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import com.sk89q.craftbook.sponge.util.SignUtil;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityInteractionTypes;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.block.tileentity.SignChangeEvent;
import org.spongepowered.api.event.entity.living.human.HumanInteractBlockEvent;
import org.spongepowered.api.event.entity.player.PlayerInteractBlockEvent;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.RelativePositions;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.world.Location;

import java.util.EnumSet;

@Module(moduleName = "Elevator", onEnable="onInitialize", onDisable="onDisable")
public class Elevator extends SpongeBlockMechanic {

    @Subscribe
    public void onSignChange(SignChangeEvent event) {

    }

    @Subscribe
    public void onPlayerInteract(HumanInteractBlockEvent event) {

        if (event instanceof PlayerInteractBlockEvent && ((PlayerInteractBlockEvent) event).getInteractionType() != EntityInteractionTypes.USE) return;

        if (SignUtil.isSign(event.getBlock())) {

            Sign sign = (Sign) event.getBlock().getTileEntity().get();

            boolean down = SignUtil.getTextRaw(sign, 1).equals("[Lift Down]");

            if (down || SignUtil.getTextRaw(sign, 1).equals("[Lift Up]")) transportEntity(event.getEntity(), event.getBlock(), down ? Direction.DOWN : Direction.UP);
        }
    }

    public void transportEntity(Entity entity, Location block, Direction direction) {

        Location destination = findDestination(block, direction);

        if (destination == block) return; // This elevator has no destination.

        Location floor = destination.getExtent().getLocation((int) Math.floor(entity.getLocation().getBlockX()), destination.getBlockY() + 1, (int) Math.floor(entity.getLocation().getBlockZ()));
        // well, unless that's already a ceiling.
        if (floor.getBlockType().isSolidCube()) {
            floor = floor.getRelative(Direction.DOWN);
        }

        // now iterate down until we find enough open space to stand in
        // or until we're 5 blocks away, which we consider too far.
        int foundFree = 0;
        boolean foundGround = false;
        for (int i = 0; i < 5; i++) {
            if (!floor.getBlockType().isSolidCube()) {
                foundFree++;
            } else {
                foundGround = true;
                break;
            }
            if (floor.getY() == 0) {
                break;
            }
            floor = floor.getRelative(Direction.DOWN);
        }

        if (!foundGround) {
            if (entity instanceof CommandSource) ((CommandSource) entity).sendMessage(Texts.of("No floor!"));
            return;
        }
        if (foundFree < 2) {
            if (entity instanceof CommandSource) ((CommandSource) entity).sendMessage(Texts.of("Obstructed!"));
            return;
        }

        // entity.setLocation(new Location(floor.getExtent(), new Vector3d(entity.getLocation().getPosition().getX(),
        // floor.getLocation().getPosition().getY()+1, entity.getLocation().getPosition().getZ())));

        entity.setLocationAndRotation(new Location(destination.getExtent(), new Vector3d(0, destination.getY() - 1, 0)), new Vector3d(0, 0, 0), EnumSet.of(RelativePositions.X, RelativePositions.Z, RelativePositions.PITCH, RelativePositions.YAW));
        if (entity instanceof CommandSource) ((CommandSource) entity).sendMessage(Texts.of("You've gone " + (direction == Direction.DOWN ? "down" : "up") + " a floor!"));
    }

    /**
     * Gets the destination of an Elevator. If there is none, it returns the start.
     * 
     * @param block The starting block.
     * @param direction The direction to move in.
     * @return The elevator destination.
     */
    private Location findDestination(Location block, Direction direction) {

        int y = block.getBlockY();

        if (direction == Direction.UP || direction == Direction.DOWN) {

            while (direction == Direction.UP ? y < 256 : y > 0) {

                y += direction == Direction.UP ? 1 : -1;

                Location test = block.getExtent().getLocation(block.getBlockX(), y, block.getBlockZ());

                if (SignUtil.isSign(test)) {
                    // It's a sign.

                    Sign sign = (Sign) test.getTileEntity().get();

                    if (SignUtil.getTextRaw(sign, 1).equals("[Lift Up]") || SignUtil.getTextRaw(sign, 1).equals("[Lift Down]") || SignUtil.getTextRaw(sign, 1).equals("[Lift]")) return test;
                }
            }
        } else {
            // We don't currently support non-up/down elevators, this isn't a Roald Dahl novel.
        }

        return block;
    }

    @Override
    public boolean isValid(Location location) {
        if (SignUtil.isSign(location)) {

            Sign sign = (Sign) location.getTileEntity().get();

            return SignUtil.getTextRaw(sign, 1).equals("[Lift Down]") || SignUtil.getTextRaw(sign, 1).equals("[Lift Up]") || SignUtil.getTextRaw(sign, 1).equals("[Lift]");
        }

        return false;
    }
}
