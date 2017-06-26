/*
 * CraftBook Copyright (C) 2010-2017 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2017 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package com.sk89q.craftbook.sponge.mechanics;

import com.flowpowered.math.vector.Vector3d;
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.PermissionNode;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeSignMechanic;
import com.sk89q.craftbook.sponge.util.LocationUtil;
import com.sk89q.craftbook.sponge.util.SignUtil;
import com.sk89q.craftbook.sponge.util.SpongePermissionNode;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.property.block.MatterProperty;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.cause.Named;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.RelativePositions;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.EnumSet;
import java.util.Optional;

@Module(id = "elevator", name = "Elevator", onEnable="onInitialize", onDisable="onDisable")
public class Elevator extends SpongeSignMechanic implements DocumentationProvider {

    private ConfigValue<Boolean> allowJumpLifts = new ConfigValue<>("allow-jump-lifts", "Allow lifts that the user can control by jumping, or sneaking.", true);
    private ConfigValue<Boolean> allowButtonLifts = new ConfigValue<>("allow-button-lifts", "Allow lifts to be controlled by buttons opposite the sign.", true);

    private SpongePermissionNode createPermissions = new SpongePermissionNode("craftbook.elevator", "Allows the user to create Elevators", PermissionDescription.ROLE_USER);
    private SpongePermissionNode usePermissions = new SpongePermissionNode("craftbook.elevator.use", "Allows the user to use Elevators", PermissionDescription.ROLE_USER);

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        allowJumpLifts.load(config);
        allowButtonLifts.load(config);

        createPermissions.register();
        usePermissions.register();
    }

    @Override
    public String[] getValidSigns() {
        return new String[] {
                "[Lift Down]", "[Lift Up]", "[Lift]", "[Lift UpDown]"
        };
    }

    @Override
    public SpongePermissionNode getCreatePermission() {
        return this.createPermissions;
    }

    @Listener
    public void onPlayerInteract(InteractBlockEvent.Secondary.MainHand event, @Named(NamedCause.SOURCE) Player player) {
        event.getTargetBlock().getLocation().ifPresent((location) -> {
            Location<World> signLocation = location;

            if (allowButtonLifts.getValue() &&
                    signLocation.getBlockType() == BlockTypes.STONE_BUTTON || signLocation.getBlockType() == BlockTypes.WOODEN_BUTTON) {
                Direction backDir = SignUtil.getBack(signLocation);
                signLocation = signLocation.getRelative(backDir).getRelative(backDir);
            }

            if (SignUtil.isSign(signLocation)) {
                Sign sign = (Sign) signLocation.getTileEntity().get();

                Optional<Vector3d> interactionPoint = event.getInteractionPoint();

                boolean down = "[Lift Down]".equals(SignUtil.getTextRaw(sign, 1)) || ("[Lift UpDown]".equals(SignUtil.getTextRaw(sign, 1)) && interactionPoint.isPresent() && interactionPoint.get().getY() < 0.5);

                if (down || "[Lift Up]".equals(SignUtil.getTextRaw(sign, 1)) || ("[Lift UpDown]".equals(SignUtil.getTextRaw(sign, 1)) && interactionPoint.isPresent() && interactionPoint.get().getY() > 0.5)) {
                    if (!usePermissions.hasPermission(player)) {
                        player.sendMessage(Text.of(TextColors.RED, "You don't have permission to use this mechanic!"));
                        return;
                    }

                    transportEntity(player, location, down ? Direction.DOWN : Direction.UP);
                }
            }
        });
    }

    @Listener
    public void onEntityMove(MoveEntityEvent event) {
        if(!allowJumpLifts.getValue())
            return;

        if(!LocationUtil.isLocationWithinWorld(event.getToTransform().getLocation()))
            return;

        Location<World> groundLocation = event.getToTransform().getLocation().getRelative(Direction.DOWN);

        //Look for dat sign
        for (Location<World> location : SignUtil.getAttachedSigns(groundLocation)) {
            Sign sign = (Sign) location.getTileEntity().orElse(null);

            if (sign != null && "[Lift UpDown]".equals(SignUtil.getTextRaw(sign, 1))) {
                if (event.getTargetEntity() instanceof Subject) {
                    if (!usePermissions.hasPermission((Subject) event.getTargetEntity())) {
                        return;
                    }
                }

                if (event.getToTransform().getPosition().getY() > event.getFromTransform().getPosition().getY()) {
                    transportEntity(event.getTargetEntity(), location, Direction.UP); //Jump is up
                } else if (event.getTargetEntity().get(Keys.IS_SNEAKING).orElse(false)) {
                    transportEntity(event.getTargetEntity(), location, Direction.DOWN); //Sneak is down
                }

                break;
            }
        }
    }

    private void transportEntity(Entity entity, Location<World> block, Direction direction) {
        Location<World> destination = findDestination(block, direction);

        if (destination == block) {
            if (entity instanceof CommandSource) ((CommandSource) entity).sendMessage(Text.of("This lift has no destination!"));
            return; // This elevator has no destination.
        }

        //Calculate difference.
        destination = destination.add(0, Math.ceil(entity.getLocation().getPosition().getY() - block.getY()) + 1, 0);

        Location<World> floor = destination.getExtent().getLocation((int) Math.floor(entity.getLocation().getBlockX()), destination.getBlockY() + 1, (int) Math.floor(entity.getLocation().getBlockZ()));
        // well, unless that's already a ceiling.
        if (floor.getBlockType().getProperty(MatterProperty.class).map(MatterProperty::getValue).orElse(MatterProperty.Matter.GAS) == MatterProperty.Matter.SOLID) {
            floor = floor.getRelative(Direction.DOWN);
        }

        // now iterate down until we find enough open space to stand in
        // or until we're 5 blocks away, which we consider too far.
        int foundFree = 0;
        boolean foundGround = false;
        for (int i = 0; i < 5; i++) {
            if (floor.getBlockType().getProperty(MatterProperty.class).map(MatterProperty::getValue).orElse(MatterProperty.Matter.GAS) != MatterProperty.Matter.SOLID || SignUtil.isSign(floor)) {
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
            if (entity instanceof CommandSource) ((CommandSource) entity).sendMessage(Text.of("No floor!"));
            return;
        }
        if (foundFree < 2) {
            if (entity instanceof CommandSource) ((CommandSource) entity).sendMessage(Text.of("Obstructed!"));
            return;
        }

        entity.setLocationAndRotation(new Location<>(destination.getExtent(), new Vector3d(0, destination.getY() - 1, 0)), new Vector3d(0, 0, 0), EnumSet.of(RelativePositions.X, RelativePositions.Z, RelativePositions.PITCH, RelativePositions.YAW));
        if (entity instanceof CommandSource) ((CommandSource) entity).sendMessage(Text.of("You've gone " + (direction == Direction.DOWN ? "down" : "up") + " a floor!"));
    }

    /**
     * Gets the destination of an Elevator. If there is none, it returns the start.
     * 
     * @param block The starting block.
     * @param direction The direction to move in.
     * @return The elevator destination.
     */
    private Location<World> findDestination(Location<World> block, Direction direction) {

        int y = block.getBlockY();

        if (direction == Direction.UP || direction == Direction.DOWN) {
            while (direction == Direction.UP ? y < 255 : y > 0) {
                y += direction == Direction.UP ? 1 : -1;

                Location<World> test = block.getExtent().getLocation(block.getBlockX(), y, block.getBlockZ());

                if (allowButtonLifts.getValue() &&
                        test.getBlockType() == BlockTypes.STONE_BUTTON || test.getBlockType() == BlockTypes.WOODEN_BUTTON) {
                    Direction backDir = SignUtil.getBack(test);
                    test = test.getRelative(backDir).getRelative(backDir);
                }

                if (SignUtil.isSign(test)) {
                    // It's a sign.
                    if(isValid(test))
                        return test;
                }
            }
        }

        // We don't currently support non-up/down elevators, this isn't a Roald Dahl novel.

        return block;
    }

    @Override
    public String getPath() {
        return "mechanics/elevator";
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue<?>[]{
                allowJumpLifts,
                allowButtonLifts
        };
    }

    @Override
    public PermissionNode[] getPermissionNodes() {
        return new PermissionNode[]{
                createPermissions,
                usePermissions
        };
    }
}
