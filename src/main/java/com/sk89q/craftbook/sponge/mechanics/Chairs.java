/*
 * CraftBook Copyright (C) 2010-2016 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2016 me4502 <http://www.me4502.com>
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

import static com.sk89q.craftbook.sponge.util.locale.TranslationsManager.USE_PERMISSIONS;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.PermissionNode;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import com.sk89q.craftbook.sponge.util.*;
import com.sk89q.craftbook.sponge.util.type.TypeTokens;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.RideEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Module(moduleName = "Chairs", onEnable="onInitialize", onDisable="onDisable")
public class Chairs extends SpongeBlockMechanic implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private ConfigValue<List<BlockFilter>> allowedBlocks = new ConfigValue<>("allowed-blocks", "A list of blocks that can be used.", getDefaultBlocks(), new TypeTokens.BlockFilterListTypeToken());
    private ConfigValue<Boolean> exitAtEntry = new ConfigValue<>("exit-at-last-position", "Moves player's to their entry position when they exit the chair.", false);
    private ConfigValue<Boolean> requireSigns = new ConfigValue<>("require-sign", "Require signs on the chairs.", false);
    private ConfigValue<Integer> maxSignDistance = new ConfigValue<Integer>("max-sign-distance", "The distance the sign can be from the clicked chair.", 3);

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

        usePermissions.register();

        Sponge.getGame().getScheduler().createTaskBuilder().intervalTicks(10).execute(task -> {
            for (Map.Entry<UUID, Chair<?>> chair : new HashSet<>(chairs.entrySet())) {
                Player player = Sponge.getGame().getServer().getPlayer(chair.getKey()).orElse(null);
                if (player == null) {
                    removeChair(chair.getValue());
                    return;
                }

                chair.getValue().chairEntity.setRotation(new Vector3d(0, player.getRotation().getY(), 0));
            }
        }).submit(CraftBookPlugin.inst());
    }

    @Override
    public void onDisable() {
        super.onDisable();

        chairs.clear();
    }

    @Override
    public boolean isValid(Location<?> location) {
        return BlockUtil.doesStatePassFilters(allowedBlocks.getValue(), location.getBlock());
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

    private Chair<?> addChair(Player player, Location<World> location) {
        Entity entity = location.getExtent().createEntity(EntityTypes.ARMOR_STAND, location.getBlockPosition().toDouble().sub(-0.5, 1, -0.5));
        entity.offer(Keys.INVISIBLE, true);
        entity.offer(Keys.HAS_GRAVITY, false);
        entity.setRotation(new Vector3d(0, player.getRotation().getY(), 0));

        location.getExtent().spawnEntity(entity, Cause.of(NamedCause.of("root", SpawnCause.builder().type(SpawnTypes.CUSTOM).build()), NamedCause.source(player)));

        Chair<?> chair = new Chair<>((ArmorStand) entity, location, player.getLocation());

        entity.addPassenger(player);
        player.sendMessage(Text.of(TextColors.YELLOW, "You sit down!"));

        chairs.put(player.getUniqueId(), chair);
        return chair;
    }

    private void removeChair(Chair<?> chair) {
        Player passenger = chair.chairEntity.getPassengers().stream().filter((entity -> entity instanceof Player)).map(e -> (Player) e).findFirst().orElse(null);
        if (passenger != null) {
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
        event.getTargetBlock().getLocation().ifPresent((location) -> {
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

            if (chairs.containsKey(player.getUniqueId())) {
                removeChair(chairs.get(player.getUniqueId()));
            }

            addChair(player, location);
        });
    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event) {
        event.getTransactions().forEach((transaction) -> transaction.getOriginal().getLocation().ifPresent((location) -> {
            Chair<?> chair = getChair(location);
            if (chair != null)
                removeChair(chair);
        }));
    }

    @Listener
    public void onDismount(RideEntityEvent.Dismount event) {
        if (event.getTargetEntity() instanceof ArmorStand) {
            Chair<?> chair = getChair(event.getTargetEntity());
            if (chair != null)
                removeChair(chair);
        }
    }

    private static List<BlockFilter> getDefaultBlocks() {
        List<BlockFilter> states = Lists.newArrayList();
        states.addAll(Sponge.getRegistry().getAllOf(BlockType.class).stream()
                .filter(blockType -> blockType.getName().toLowerCase().contains("stairs"))
                .map(blockType -> new BlockFilter(blockType.getName())).collect(Collectors.toList()));
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
                maxSignDistance
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
