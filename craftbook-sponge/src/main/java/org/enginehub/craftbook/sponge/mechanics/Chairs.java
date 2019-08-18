/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
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
package org.enginehub.craftbook.sponge.mechanics;

import static org.enginehub.craftbook.sponge.util.locale.TranslationsManager.USE_PERMISSIONS;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import org.enginehub.craftbook.core.util.ConfigValue;
import org.enginehub.craftbook.core.util.CraftBookException;
import org.enginehub.craftbook.core.util.PermissionNode;
import org.enginehub.craftbook.core.util.documentation.DocumentationProvider;
import org.enginehub.craftbook.sponge.CraftBookPlugin;
import org.enginehub.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import org.enginehub.craftbook.sponge.util.SpongeBlockFilter;
import org.enginehub.craftbook.sponge.util.BlockUtil;
import org.enginehub.craftbook.sponge.util.LocationUtil;
import org.enginehub.craftbook.sponge.util.SignUtil;
import org.enginehub.craftbook.sponge.util.SpongePermissionNode;
import org.enginehub.craftbook.sponge.util.type.TypeTokens;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.RideEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Module(id = "chairs", name = "Chairs", onEnable="onInitialize", onDisable="onDisable")
public class Chairs extends SpongeBlockMechanic implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private ConfigValue<List<SpongeBlockFilter>>
            allowedBlocks = new ConfigValue<>("allowed-blocks", "A list of blocks that can be used.", getDefaultBlocks(), new TypeTokens.BlockFilterListTypeToken());
    private ConfigValue<Boolean> exitAtEntry = new ConfigValue<>("exit-at-last-position", "Moves player's to their entry position when they exit the chair.", false);
    private ConfigValue<Boolean> requireSigns = new ConfigValue<>("require-sign", "Require signs on the chairs.", false);
    private ConfigValue<Integer> maxSignDistance = new ConfigValue<>("max-sign-distance", "The distance the sign can be from the clicked chair.", 3);
    private ConfigValue<Boolean> faceCorrectDirection = new ConfigValue<>("face-correct-direction", "When the player sits, automatically face them the direction of the chair. (If possible)", true);
    private ConfigValue<Boolean> healPassenger = new ConfigValue<>("heal-passenger", "Heal the player when they're sitting in the chair.", false);
    private ConfigValue<Double> healAmount = new ConfigValue<>("heal-amount", "Amount to heal the player by.", 1.0d, TypeToken.of(Double.class));

    private SpongePermissionNode usePermissions = new SpongePermissionNode("craftbook.chairs.use", "Allows the user to sit in chairs.", PermissionDescription.ROLE_USER);

    private Map<UUID, Chair<?>> chairs;

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        chairs = new HashMap<>();

        allowedBlocks.load(config);
        exitAtEntry.load(config);
        requireSigns.load(config);
        maxSignDistance.load(config);
        faceCorrectDirection.load(config);
        healPassenger.load(config);
        healAmount.load(config);

        usePermissions.register();

        Sponge.getGame().getScheduler().createTaskBuilder().intervalTicks(10).execute(task -> {
            for (Map.Entry<UUID, Chair<?>> chair : new HashSet<>(chairs.entrySet())) {
                Player player = Sponge.getGame().getServer().getPlayer(chair.getKey()).orElse(null);
                if (player == null) {
                    removeChair(chair.getValue(), false);
                    return;
                }

                if (healPassenger.getValue()) {
                    if (player.get(Keys.HEALTH).orElse(0d) < player.get(Keys.MAX_HEALTH).orElse(0d)) {
                        player.offer(Keys.HEALTH, Math.min(player.get(Keys.HEALTH).orElse(0d) + healAmount.getValue(), player.get(Keys.MAX_HEALTH).orElse(0d)));
                    }
                }

                if (player.get(Keys.EXHAUSTION).orElse(-20d) > -20d) {
                    player.offer(Keys.EXHAUSTION, player.get(Keys.EXHAUSTION).orElse(-20d) - 0.1d);
                }
            }
        }).submit(CraftBookPlugin.inst());
    }

    @Override
    public void onDisable() {
        super.onDisable();

        chairs.clear();
    }

    @Override
    public boolean isValid(Location<World> location) {
        return BlockUtil.doesStatePassFilters(allowedBlocks.getValue(), location.getBlock()) &&
                (location.getBlockY() == 0 || !location.getRelative(Direction.DOWN).getBlockType().equals(BlockTypes.AIR)) &&
                location.getRelative(Direction.UP).getBlockType().equals(BlockTypes.AIR);
    }

    private boolean hasSign(Location<World> location, List<Location<World>> searched, Location<World> original) {
        boolean found = false;

        for (Direction face : BlockUtil.getDirectFaces()) {
            Location<World> otherBlock = location.getRelative(face);

            if (searched.contains(otherBlock)) continue;
            searched.add(otherBlock);

            if (found) break;

            if (location.getPosition().distanceSquared(original.getPosition()) > Math.pow(maxSignDistance.getValue(), 2)) continue;

            if (SignUtil.isSign(otherBlock) && SignUtil.getFront(otherBlock) == face) {
                found = true;
                break;
            }

            if (Objects.equals(location.getBlockType(), otherBlock.getBlockType())) {
                found = hasSign(otherBlock, searched, original);
            }
        }

        return found;
    }

    private void addChair(Player player, Location<World> location) {
        Entity entity = location.getExtent().createEntity(EntityTypes.ARMOR_STAND, location.getBlockPosition().toDouble().sub(-0.5, 1.2, -0.5));
        entity.offer(Keys.INVISIBLE, true);
        entity.offer(Keys.HAS_GRAVITY, false);

        if (faceCorrectDirection.getValue() && location.supports(Keys.DIRECTION)) {
            Vector3d euler = LocationUtil.cartesianToEuler(location.get(Keys.DIRECTION).orElse(Direction.NONE).getOpposite().asOffset());
            entity.setRotation(euler);
            player.setRotation(euler);
        }

        Sponge.getCauseStackManager().pushCause(player);
        location.getExtent().spawnEntity(entity);
        Sponge.getCauseStackManager().popCause();

        Chair<?> chair = new Chair<>((ArmorStand) entity, location, player.getLocation());

        entity.addPassenger(player);
        player.sendMessage(Text.of(TextColors.YELLOW, "You sit down!"));

        chairs.put(player.getUniqueId(), chair);
    }

    private void removeChair(Chair<?> chair, boolean clearPassengers) {
        Player passenger = chair.chairEntity.getPassengers().stream().filter((entity -> entity instanceof Player)).map(e -> (Player) e).findFirst().orElse(null);
        if (passenger != null && clearPassengers) {
            passenger.setVehicle(null);
            chair.chairEntity.clearPassengers();
            passenger.sendMessage(Text.of(TextColors.YELLOW, "You stand up!"));
            if (exitAtEntry.getValue()) {
                Sponge.getScheduler().createTaskBuilder().delayTicks(5L).execute(() -> passenger.setLocation(chair.playerExitLocation)).submit(CraftBookPlugin.inst());
            }
        }
        chair.chairEntity.remove();
        chairs.values().remove(chair);
    }

    /**
     * Gets the chair by the chair entity.
     *
     * @param entity The chair entity.
     * @return The chair
     */
    private Chair<?> getChair(Entity entity) {
        return chairs.values().stream().filter((chair) -> chair.chairEntity.equals(entity)).findFirst().orElse(null);
    }

    /**
     * Gets the chair by the chair block.
     *
     * @param location The chair block.
     * @return The chair
     */
    private Chair<?> getChair(Location<?> location) {
        return chairs.values().stream().filter((chair) -> chair.chairLocation.getBlockPosition().equals(location.getBlockPosition())).findFirst().orElse(null);
    }

    @Listener
    public void onBlockClick(InteractBlockEvent.Secondary.MainHand event, @First Player player) {
        event.getTargetBlock().getLocation().ifPresent(location -> {
            if (!isValid(location))
                return;

            if (player.get(Keys.IS_SNEAKING).orElse(false))
                return;

            if (!usePermissions.hasPermission(player)) {
                player.sendMessage(USE_PERMISSIONS);
                return;
            }

            if (requireSigns.getValue() && !hasSign(location, new ArrayList<>(), location)) {
                return;
            }

            if (getChair(location) != null) {
                player.sendMessage(Text.of(TextColors.RED, "Chair already occupied!"));
                return;
            }

            if (chairs.containsKey(player.getUniqueId())) {
                removeChair(chairs.get(player.getUniqueId()), true);
            }

            addChair(player, location);
        });
    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event) {
        event.getTransactions().forEach((transaction) -> transaction.getOriginal().getLocation().ifPresent((location) -> {
            Chair<?> chair = getChair(location);
            if (chair != null)
                removeChair(chair, true);
        }));
    }

    @Listener
    public void onDismount(RideEntityEvent.Dismount event, @First Player player) {
        if (event.getTargetEntity() instanceof ArmorStand) {
            Chair<?> chair = getChair(event.getTargetEntity());
            if (chair != null) {
                player.sendMessage(Text.of(TextColors.YELLOW, "You stand up!"));
                Sponge.getScheduler().createTaskBuilder().execute(() -> {
                    removeChair(chair, false);
                    if (!exitAtEntry.getValue()) {
                        Sponge.getGame().getTeleportHelper().getSafeLocation(chair.chairLocation).ifPresent(player::setLocation);
                    }
                }).submit(CraftBookPlugin.inst());
            }
        }
    }

    private static List<SpongeBlockFilter> getDefaultBlocks() {
        List<SpongeBlockFilter> states = Lists.newArrayList();
        states.addAll(Sponge.getRegistry().getAllOf(BlockType.class).stream()
                .filter(blockType -> blockType.getName().toLowerCase().contains("stairs"))
                .map(blockType -> new SpongeBlockFilter(blockType.getName())).collect(Collectors.toList()));
        return states;
    }

    @Override
    public String getPath() {
        return "mechanics/chairs";
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue[]{
                allowedBlocks,
                exitAtEntry,
                requireSigns,
                maxSignDistance,
                faceCorrectDirection,
                healPassenger,
                healAmount
        };
    }

    @Override
    public PermissionNode[] getPermissionNodes() {
        return new PermissionNode[]{
                usePermissions
        };
    }

    /**
     * Data class that stores information necessary
     * to the functionality of chairs.
     */
    static final class Chair<T extends Entity> {
        private T chairEntity;
        private Location<World> chairLocation;
        private Location<World> playerExitLocation;

        Chair(T chairEntity, Location<World> chairLocation, Location<World> playerExitLocation) {
            this.chairEntity = chairEntity;
            this.chairLocation = chairLocation;
            this.playerExitLocation = playerExitLocation;
        }
    }
}
