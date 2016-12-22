package com.sk89q.craftbook.sponge.mechanics;

import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DoublePlantTypes;
import org.spongepowered.api.data.type.PortionTypes;
import org.spongepowered.api.data.type.ShrubTypes;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.TickBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

@Module(moduleName = "BetterPhysics", onEnable="onInitialize", onDisable="onDisable")
public class BetterPlants extends SpongeBlockMechanic implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private ConfigValue<Boolean> fernFarming = new ConfigValue<>("fern-farming", "Enables the 'fern farming' plants mechanic.", true);

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        fernFarming.load(config);

        if (fernFarming.getValue()) {
            BlockTypes.TALLGRASS.setTickRandomly(true);
        }
    }

    @Listener
    public void onTick(TickBlockEvent.Random event) {
        event.getTargetBlock().getLocation().ifPresent(worldLocation -> {
            if (isValidFernFarming(worldLocation) && ThreadLocalRandom.current().nextInt(10) == 0) {
                worldLocation.setBlock(BlockState.builder().blockType(BlockTypes.DOUBLE_PLANT).add(Keys.DOUBLE_PLANT_TYPE, DoublePlantTypes.FERN).add(Keys.PORTION_TYPE, PortionTypes.BOTTOM).build(), Cause.source(CraftBookPlugin.spongeInst().getContainer()).build());
                worldLocation.getRelative(Direction.UP).setBlock(BlockState.builder().blockType(BlockTypes.DOUBLE_PLANT).add(Keys.DOUBLE_PLANT_TYPE, DoublePlantTypes.FERN).add(Keys.PORTION_TYPE, PortionTypes.TOP).build(), Cause.source(CraftBookPlugin.spongeInst().getContainer()).build());
            }
        });
    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event, @First Player player) {
        event.getTransactions().stream()
                .map(Transaction::getOriginal)
                .filter(snapshot -> snapshot.getState().getType() == BlockTypes.DOUBLE_PLANT)
                .filter(snapshot -> snapshot.getState().get(Keys.DOUBLE_PLANT_TYPE).orElse(DoublePlantTypes.GRASS).equals(DoublePlantTypes.FERN))
                .filter(snapshot -> snapshot.getState().get(Keys.PORTION_TYPE).orElse(PortionTypes.BOTTOM).equals(PortionTypes.TOP))
                .forEach(snapshot -> Sponge.getScheduler().createTaskBuilder().execute(task -> {
                    System.out.println(snapshot.getLocation().get().toString());
                    snapshot.getLocation().get().getRelative(Direction.DOWN)
                            .setBlock(BlockState.builder().blockType(BlockTypes.TALLGRASS).add(Keys.SHRUB_TYPE, ShrubTypes.FERN).build(),
                                    CraftBookPlugin.spongeInst().getCause().build());
                    if (player.get(Keys.GAME_MODE).orElse(GameModes.SURVIVAL) != GameModes.CREATIVE) {
                        Item item = (Item) snapshot.getLocation().get().getExtent().createEntity(EntityTypes.ITEM, snapshot.getPosition());
                        item.offer(Keys.REPRESENTED_ITEM, ItemStack.builder().itemType(ItemTypes.TALLGRASS).add(Keys.SHRUB_TYPE, ShrubTypes.FERN).build().createSnapshot());
                        snapshot.getLocation().get().spawnEntity(item, CraftBookPlugin.spongeInst().getCause().build());
                    }
                }).submit(CraftBookPlugin.spongeInst().getContainer()));
    }

    @Override
    public String getPath() {
        return "mechanics/better_plants";
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue<?>[] {
                fernFarming
        };
    }

    private boolean isValidFernFarming(Location<?> location) {
        return fernFarming.getValue()
                && location.getBlockType().equals(BlockTypes.TALLGRASS)
                && ShrubTypes.FERN.equals(location.get(Keys.SHRUB_TYPE).orElse(null))
                && location.getRelative(Direction.UP).getBlockType() == BlockTypes.AIR;
    }

    @Override
    public boolean isValid(Location<?> location) {
        return isValidFernFarming(location);
    }
}
