package com.sk89q.craftbook.sponge.mechanics;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import com.sk89q.craftbook.sponge.util.BlockFilter;
import com.sk89q.craftbook.sponge.util.BlockUtil;
import com.sk89q.craftbook.sponge.util.SpongePermissionNode;
import com.sk89q.craftbook.sponge.util.type.BlockFilterListTypeToken;
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
import org.spongepowered.api.event.entity.DismountEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.sk89q.craftbook.sponge.util.locale.TranslationsManager.USE_PERMISSIONS;

@Module(moduleName = "Chairs", onEnable="onInitialize", onDisable="onDisable")
public class Chairs extends SpongeBlockMechanic {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private ConfigValue<List<BlockFilter>> allowedBlocks = new ConfigValue<>("allowed-blocks", "A list of blocks that can be used.", getDefaultBlocks(), new BlockFilterListTypeToken());

    private SpongePermissionNode usePermissions = new SpongePermissionNode("craftbook.chairs.use", "Allows the user to sit in chairs.", PermissionDescription.ROLE_USER);

    private Map<UUID, Chair<?>> chairs;

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        chairs = new HashMap<>();

        allowedBlocks.load(config);

        usePermissions.register();
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

    private Chair<?> addChair(Player player, Location<World> location) {
        Entity entity = location.getExtent().createEntity(EntityTypes.ARMOR_STAND, location.getBlockPosition().toDouble().sub(-0.5, 1, -0.5));
        entity.offer(Keys.INVISIBLE, true);
        entity.offer(Keys.ARMOR_STAND_HAS_GRAVITY, false);

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
            chair.chairEntity.removePassenger(passenger);
            passenger.sendMessage(Text.of(TextColors.YELLOW, "You stand up!"));
            Sponge.getScheduler().createTaskBuilder().delayTicks(5L).execute(() -> passenger.setLocation(chair.playerExitLocation)).submit(CraftBookPlugin.inst());
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

            if (chairs.containsKey(player.getUniqueId())) {
                removeChair(chairs.get(player.getUniqueId()));
            } else {
                addChair(player, location);
            }
        });
    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event) {
        event.getTransactions().forEach((transaction) -> transaction.getOriginal().getLocation().ifPresent((location) -> {
            Chair<?> chair = getChair(location);
            removeChair(chair);
        }));
    }

    @Listener
    public void onDismount(DismountEntityEvent event) {
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
