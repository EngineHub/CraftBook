package com.sk89q.craftbook.sponge.mechanics;

import com.flowpowered.math.vector.Vector3d;
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.PermissionNode;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import com.sk89q.craftbook.sponge.util.SignUtil;
import com.sk89q.craftbook.sponge.util.SpongePermissionNode;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.property.block.MatterProperty;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.entity.DisplaceEntityEvent;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.RelativePositions;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.EnumSet;

@Module(moduleName = "Elevator", onEnable="onInitialize", onDisable="onDisable")
public class Elevator extends SpongeBlockMechanic implements DocumentationProvider {

    ConfigValue<Boolean> allowJumpLifts = new ConfigValue<>("allow-jump-lifts", "Allow lifts that the user can control by jumping, or sneaking.", true);

    SpongePermissionNode createPermissions = new SpongePermissionNode("craftbook.elevator.create", "Allows the user to create Elevators", PermissionDescription.ROLE_USER);

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        allowJumpLifts.load(config);

        createPermissions.register();
    }

    @Listener
    public void onSignChange(ChangeSignEvent event) {

        Player player;
        if(event.getCause().first(Player.class).isPresent())
            player = event.getCause().first(Player.class).get();
        else
            return;

        for(String line : getValidSigns()) {
            if(SignUtil.getTextRaw(event.getText(), 1).equalsIgnoreCase(line)) {
                if(!createPermissions.hasPermission(player)) {
                    player.sendMessage(Text.of(TextColors.RED, "You do not have permission to create this mechanic!"));
                    event.setCancelled(true);
                } else {
                    event.getText().lines().set(1, Text.of(line));
                }
            }
        }
    }

    @Listener
    public void onPlayerInteract(InteractBlockEvent.Secondary event) {

        Humanoid human;
        if(event.getCause().first(Humanoid.class).isPresent())
            human = event.getCause().first(Humanoid.class).get();
        else
            return;

        if (SignUtil.isSign(event.getTargetBlock().getLocation().get())) {

            Sign sign = (Sign) event.getTargetBlock().getLocation().get().getTileEntity().get();

            boolean down = SignUtil.getTextRaw(sign, 1).equals("[Lift Down]") || (SignUtil.getTextRaw(sign, 1).equals("[Lift UpDown]") && event.getInteractionPoint().isPresent() && event.getInteractionPoint().get().getY() < 0.5);

            if (down || SignUtil.getTextRaw(sign, 1).equals("[Lift Up]") || (SignUtil.getTextRaw(sign, 1).equals("[Lift UpDown]") && event.getInteractionPoint().isPresent() && event.getInteractionPoint().get().getY() > 0.5)) transportEntity(human, event.getTargetBlock().getLocation().get(), down ? Direction.DOWN : Direction.UP);
        }
    }

    @Listener
    public void onEntityMove(DisplaceEntityEvent.Move.TargetLiving event) {

        Location<World> groundLocation = event.getToTransform().getLocation().getRelative(Direction.DOWN);

        Location<World> signLocation = null;

        if(!allowJumpLifts.getValue())
            return;

        //Look for dat sign
        if(SignUtil.isSign(groundLocation.getRelative(Direction.DOWN)))
            signLocation = groundLocation.getRelative(Direction.DOWN);

        if(signLocation != null) {
            Sign sign = (Sign) signLocation.getTileEntity().get();

            if (SignUtil.getTextRaw(sign, 1).equals("[Lift UpDown]")) {
                if (event.getToTransform().getPosition().getY() > event.getFromTransform().getPosition().getY()) {
                    transportEntity(event.getTargetEntity(), signLocation, Direction.UP); //Jump is up
                } else if(event.getTargetEntity().get(Keys.IS_SNEAKING).orElse(false)) {
                    transportEntity(event.getTargetEntity(), signLocation, Direction.DOWN); //Sneak is down
                }
            }
        }
    }

    public void transportEntity(Entity entity, Location<World> block, Direction direction) {

        Location<World> destination = findDestination(block, direction);

        if (destination == block) {
            if (entity instanceof CommandSource) ((CommandSource) entity).sendMessage(Text.of("This lift has no destination!"));
            return; // This elevator has no destination.
        }

        //Calculate difference.
        destination = destination.add(0, Math.ceil(entity.getLocation().getPosition().getY() - block.getY()) + 1, 0);

        Location<World> floor = destination.getExtent().getLocation((int) Math.floor(entity.getLocation().getBlockX()), destination.getBlockY() + 1, (int) Math.floor(entity.getLocation().getBlockZ()));
        // well, unless that's already a ceiling.
        if (floor.getBlockType().getProperty(MatterProperty.class).get().getValue() == MatterProperty.Matter.SOLID) {
            floor = floor.getRelative(Direction.DOWN);
        }

        // now iterate down until we find enough open space to stand in
        // or until we're 5 blocks away, which we consider too far.
        int foundFree = 0;
        boolean foundGround = false;
        for (int i = 0; i < 5; i++) {
            if (floor.getBlockType().getProperty(MatterProperty.class).get().getValue() != MatterProperty.Matter.SOLID || SignUtil.isSign(floor)) {
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

        // entity.setLocation(new Location(floor.getExtent(), new Vector3d(entity.getLocation().getPosition().getX(),
        // floor.getLocation().getPosition().getY()+1, entity.getLocation().getPosition().getZ())));

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
    public boolean isValid(Location location) {
        if (SignUtil.isSign(location)) {

            Sign sign = (Sign) location.getTileEntity().get();

            for(String signLine : getValidSigns()) {
                if(SignUtil.getTextRaw(sign, 1).equals(signLine))
                    return true;
            }
        }

        return false;
    }

    public static String[] getValidSigns() {
        return new String[] {
                "[Lift Down]", "[Lift Up]", "[Lift]", "[Lift UpDown]"
        };
    }

    @Override
    public String getPath() {
        return "mechanics/elevator";
    }

    @Override
    public String[] getMainDocumentation() {
        return new String[]{
                "=========",
                "Elevators",
                "=========",
                ""
        };
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue<?>[]{
                allowJumpLifts
        };
    }

    @Override
    public PermissionNode[] getPermissionNodes() {
        return new PermissionNode[]{
            createPermissions
        };
    }
}
