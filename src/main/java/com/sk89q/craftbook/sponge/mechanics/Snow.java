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

import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.block.LayeredData;
import org.spongepowered.api.data.property.block.ReplaceableProperty;
import org.spongepowered.api.data.property.block.TemperatureProperty;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.TickBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.weather.Weathers;

import java.util.Optional;

@Module(moduleName = "Snow", onEnable="onInitialize", onDisable="onDisable")
public class Snow extends SpongeMechanic implements DocumentationProvider {

    /**
     * An array of directions that snow can move in. In order of preference.
     */
    private static final Direction[] VALID_SNOW_DIRECTIONS = new Direction[]{Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private ConfigValue<Boolean> dispersionMode = new ConfigValue<>("dispersion-mode", "A realistic version of snow that disperses as it piles.", false);
    private ConfigValue<Boolean> highPiling = new ConfigValue<>("high-piling", "Allows snow to pile up multiple blocks.", false);
    private ConfigValue<Boolean> waterFreezing = new ConfigValue<>("water-freezing", "Allows snow to freeze water beneath it.", false);

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        //Give the snow blocks the ability to tick randomly.
        BlockTypes.SNOW_LAYER.setTickRandomly(true);
        BlockTypes.SNOW.setTickRandomly(true);

        dispersionMode.load(config);
        highPiling.load(config);
        waterFreezing.load(config);
    }

    @Listener
    public void onBlockTick(TickBlockEvent event) {
        event.getTargetBlock().getLocation().ifPresent(location -> {
            if (isSnowBlock(location)) {
                if (getTemperature(location) == TemperatureType.FREEZING) {
                    //Only increase snow at valid blocks, where snow could actually fall.
                    if (location.getBlockType() == BlockTypes.SNOW_LAYER && canSnowReach(location))
                        increaseSnow(location, true);
                } else if (!isBlockBuried(location) && getTemperature(location) == TemperatureType.WARM) {
                    //Only melt if on top, and too hot.
                    if(location.get(Keys.LAYER).orElse(0) == 1) {
                        return;
                    }

                    //Lower the snow
                    decreaseSnow(location);
                } else if (!isBlockBuried(location)) {
                    disperseSnow(location, null);
                }
            }
        });
    }

    private void increaseSnow(Location<?> location, boolean disperse) {
        Optional<MutableBoundedValue<Integer>> optionalHeightValue = location.getValue(Keys.LAYER);
        if(location.getBlockType() == BlockTypes.SNOW_LAYER && optionalHeightValue.isPresent()) {
            MutableBoundedValue<Integer> heightValue = optionalHeightValue.get();
            int newHeight = heightValue.get() + 1;

            if(newHeight > heightValue.getMaxValue()) {
                if(highPiling.getValue())
                    location.setBlockType(BlockTypes.SNOW, Cause.of(NamedCause.source(CraftBookPlugin.<CraftBookPlugin>inst().getContainer())));
            } else {
                location.offer(Keys.LAYER, newHeight);
                if(disperse)
                    disperseSnow(location, null);
            }

            if(waterFreezing.getValue()) {
                Location down = location.getRelative(Direction.DOWN);

                if (down.getBlockType() == BlockTypes.WATER || down.getBlockType() == BlockTypes.FLOWING_WATER)
                    down.setBlockType(BlockTypes.ICE, Cause.of(NamedCause.source(CraftBookPlugin.<CraftBookPlugin>inst().getContainer())));
            }
        } else {
            location.setBlockType(BlockTypes.SNOW_LAYER, Cause.of(NamedCause.source(CraftBookPlugin.<CraftBookPlugin>inst().getContainer())));
        }
    }

    private static void decreaseSnow(Location<?> location) {
        Optional<MutableBoundedValue<Integer>> optionalHeightValue = location.getValue(Keys.LAYER);
        if(optionalHeightValue.isPresent()) {
            MutableBoundedValue<Integer> heightValue = optionalHeightValue.get();
            int newHeight = heightValue.get() - 1;

            if(newHeight < heightValue.getMinValue())
                location.setBlockType(BlockTypes.AIR, Cause.of(NamedCause.source(CraftBookPlugin.<CraftBookPlugin>inst().getContainer())));
            else
                location.offer(Keys.LAYER, newHeight);
        } else if (location.getBlockType() == BlockTypes.SNOW) {
            location.setBlockType(BlockTypes.SNOW_LAYER, Cause.of(NamedCause.source(CraftBookPlugin.<CraftBookPlugin>inst().getContainer())));
            LayeredData data = location.getOrCreate(LayeredData.class).get();
            data.set(Keys.LAYER, data.getValue(Keys.LAYER).get().getMaxValue());
            location.offer(data);
        }
    }

    private void disperseSnow(final Location<?> location, Direction ignoredFace) {
        if(!dispersionMode.getValue())
            return;

        int currentHeight = location.get(Keys.LAYER).orElse(0);
        if(currentHeight == 0)
            return;

        Direction currentSmallest = null;
        int currentSmallestInt = Integer.MAX_VALUE;

        for(final Direction dir : VALID_SNOW_DIRECTIONS) {
            if(dir == ignoredFace || (currentHeight == 1 && dir != Direction.DOWN)) continue;

            final Location<?> relative = location.getRelative(dir);
            if(canPlaceSnowAt(relative)) {
                int otherHeight = relative.get(Keys.LAYER).orElse(0);
                if(otherHeight >= 1) {
                    if(dir != Direction.DOWN && currentHeight < otherHeight+2) {
                        continue;
                    }
                }
                if(dir == Direction.DOWN) {
                    increaseSnow(relative, false);
                    decreaseSnow(location);
                    Sponge.getGame().getScheduler().createTaskBuilder().delayTicks(20L).execute(() -> {
                        disperseSnow(relative, dir.getOpposite());
                        if (isBlockBuried(location))
                            disperseSnow(location.getRelative(Direction.UP), Direction.NONE);
                    }).submit(CraftBookPlugin.inst());
                    break;
                } else if(currentSmallest == null || currentSmallestInt > otherHeight) {
                    currentSmallest = dir;
                    currentSmallestInt = otherHeight;
                    if(currentSmallestInt == 0)
                        break; //Can't get smaller.
                }
            }
        }

        if(currentSmallest != null) {
            Location<?> relative = location.getRelative(currentSmallest);
            increaseSnow(relative, false);
            decreaseSnow(location);
            Direction finalCurrentSmallest = currentSmallest;
            Sponge.getGame().getScheduler().createTaskBuilder().delayTicks(20L).execute(() -> {
                disperseSnow(relative, finalCurrentSmallest.getOpposite());
                if (isBlockBuried(location))
                    disperseSnow(location.getRelative(Direction.UP), Direction.NONE);
            }).submit(CraftBookPlugin.inst());
        }
    }

    /**
     * Determines whether or not snow is able to reach this block.
     *
     * @param location The block.
     * @return If snow can reach it.
     */
    private static boolean canSnowReach(Location<?> location) {
        while(location.getBlockY() < location.getExtent().getBlockMax().getY()) {
            location = location.getRelative(Direction.UP);
            if(location.getBlockType() != BlockTypes.AIR
                    && location.getBlockType() != BlockTypes.LEAVES
                    && location.getBlockType() != BlockTypes.LEAVES2)
                return false;
        }
        return true;
    }

    /**
     * Gets whether the block is replacable with snow.
     *
     * @param location The block.
     * @return If it can be replaced with snow.
     */
    private static boolean canPlaceSnowAt(Location<?> location) {
        if (location.getBlockType() == BlockTypes.SNOW_LAYER || location.getBlockType() == BlockTypes.AIR)
            return true;

        ReplaceableProperty replaceableProperty = location.getBlockType().getProperty(ReplaceableProperty.class).orElse(null);

        return replaceableProperty != null
                && !(location.getBlockType() == BlockTypes.WATER
                || location.getBlockType() == BlockTypes.FLOWING_WATER
                || location.getBlockType() == BlockTypes.LAVA
                || location.getBlockType() == BlockTypes.FLOWING_LAVA)
                && replaceableProperty.getValue() != null
                && replaceableProperty.getValue();
    }

    /**
     * Determines if the specified block is buried under snow.
     *
     * @param location The location of the block.
     * @return If it is buried.
     */
    private static boolean isBlockBuried(Location<?> location) {
        return isSnowBlock(location.getRelative(Direction.UP));
    }

    /**
     * Checks whether the block is snow.
     *
     * @param location The location of the block.
     * @return If it's snow
     */
    private static boolean isSnowBlock(Location<?> location) {
        return location.getBlockType() == BlockTypes.SNOW || location.getBlockType() == BlockTypes.SNOW_LAYER;
    }

    /**
     * Gets the temperature of the block.
     *
     * @param location The location of the block.
     * @return The temperature
     */
    private static TemperatureType getTemperature(Location<World> location) {
        Optional<TemperatureProperty> temperaturePropertyOptional = location.getProperty(TemperatureProperty.class);
        if(!temperaturePropertyOptional.isPresent())
            return TemperatureType.UNKNOWN;
        Double temperature = temperaturePropertyOptional.get().getValue();
        if(temperature == null)
            return TemperatureType.UNKNOWN;
        if(temperature < 0.15 && location.getExtent().getWeather() == Weathers.RAIN)
            return TemperatureType.FREEZING;
        else if (temperature < 0.15)
            return TemperatureType.COLD;
        else
            return TemperatureType.WARM;
    }

    private enum TemperatureType {
        FREEZING, //Snow generates
        COLD, //Snow survives
        WARM, //Snow melts
        UNKNOWN //Unknown - don't do anything
    }

    @Override
    public String getName() {
        return "BetterSnow";
    }

    @Override
    public String getPath() {
        return "mechanics/snow";
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue<?>[]{
                dispersionMode,
                highPiling,
                waterFreezing
        };
    }
}
