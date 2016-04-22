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

import com.me4502.modularframework.module.Module;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.block.LayeredData;
import org.spongepowered.api.data.property.block.ReplaceableProperty;
import org.spongepowered.api.data.property.block.TemperatureProperty;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.TickBlockEvent;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.weather.Weathers;

import java.util.Optional;

@Module(moduleName = "Snow", onEnable="onInitialize", onDisable="onDisable")
public class Snow extends SpongeMechanic {

    /**
     * An array of directions that snow can move in. In order of preference.
     */
    private static final Direction[] VALID_SNOW_DIRECTIONS = new Direction[]{Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.NONE};

    @Override
    public void onInitialize() throws CraftBookException {

        super.onInitialize();

        //Give the snow blocks the ability to tick randomly.
        BlockTypes.SNOW_LAYER.setTickRandomly(true);
        BlockTypes.SNOW.setTickRandomly(true);
    }

    @Listener
    public void onBlockTick(TickBlockEvent event) {
        Location<World> location = event.getTargetBlock().getLocation().orElse(null);
        if(location == null) return;
        if ((event.getTargetBlock().getState().getType() == BlockTypes.SNOW_LAYER
                || event.getTargetBlock().getState().getType() == BlockTypes.SNOW)) {
            if (location.getExtent().getWeather() != Weathers.CLEAR) {
                //Only increase snow at valid blocks, where snow could actually fall.
                if (location.getBlockType() == BlockTypes.SNOW_LAYER && canSnowReach(location)
                        && location.getProperty(TemperatureProperty.class).get().getValue() <= 0.15f)
                    increaseSnow(location, true);
            } else if(!isBlockBuried(location)) { //Only melt if on top, and too hot.
                //Lower the snow.
                if(location.get(Keys.LAYER).orElse(-1) == 0) {
                    return;
                } else if (location.getProperty(TemperatureProperty.class).get().getValue() > 0.15f) {
                    return;
                }

                decreaseSnow(location);
            }
        }
    }

    private void increaseSnow(Location<?> location, boolean disperse) {
        Optional<MutableBoundedValue<Integer>> optionalHeightValue = location.getValue(Keys.LAYER);
        if(optionalHeightValue.isPresent()) {
            MutableBoundedValue<Integer> heightValue = optionalHeightValue.get();
            int currentHeight = heightValue.get() + 1;
            if(currentHeight > heightValue.getMaxValue())
                location.setBlockType(BlockTypes.SNOW);
            else {
                if(disperse) {
                    disperseSnow(location, null);
                } else {
                    location.offer(Keys.LAYER, currentHeight);
                }
            }

            if(location.getRelative(Direction.DOWN).getBlockType() == BlockTypes.WATER
                    || location.getRelative(Direction.DOWN).getBlockType() == BlockTypes.FLOWING_WATER)
                location.getRelative(Direction.DOWN).setBlockType(BlockTypes.ICE);
        } else {
            location.setBlockType(BlockTypes.SNOW_LAYER);
        }
    }

    private void decreaseSnow(Location<?> location) {
        Optional<MutableBoundedValue<Integer>> optionalHeightValue = location.getValue(Keys.LAYER);
        if(optionalHeightValue.isPresent()) {
            MutableBoundedValue<Integer> heightValue = optionalHeightValue.get();
            int currentHeight = heightValue.get() - 1;
            if(currentHeight < heightValue.getMinValue())
                location.setBlockType(BlockTypes.AIR);
            else
                location.offer(Keys.LAYER, currentHeight);
        } else if (location.getBlockType() == BlockTypes.SNOW) {
            location.setBlockType(BlockTypes.SNOW_LAYER);
            LayeredData data = location.getOrCreate(LayeredData.class).get();
            data.set(Keys.LAYER, data.getValue(Keys.LAYER).get().getMaxValue());
            location.offer(data);
        }
    }

    private void disperseSnow(final Location<?> location, Direction ignoredFace) {
        int currentHeight = location.get(Keys.LAYER).orElse(-1);
        if(currentHeight == -1)
            return;

        for(final Direction dir : VALID_SNOW_DIRECTIONS) {
            if(dir == ignoredFace) continue;
            if(currentHeight == 0 && !(dir == Direction.DOWN || dir == Direction.NONE)) continue; //Stop snow moving around on the ground.
            final Location<?> relative = location.getRelative(dir);
            if(canPlaceSnowAt(relative)) {
                int otherHeight = relative.get(Keys.LAYER).orElse(-1);
                if(otherHeight >= 0) {
                    if(dir != Direction.NONE && dir != Direction.DOWN && currentHeight <= otherHeight+1)
                        continue;
                }
                increaseSnow(relative, false);
                if(dir != Direction.NONE) {
                    decreaseSnow(location);
                    Sponge.getGame().getScheduler().createTaskBuilder().delayTicks(40L).execute(() -> {
                        disperseSnow(relative, dir.getOpposite());
                        if(isBlockBuried(location))
                            disperseSnow(location.getRelative(Direction.UP), Direction.NONE);
                    }).submit(CraftBookPlugin.inst());
                }
                break;
            }
        }
    }

    /**
     * Determines whether or not snow is able to reach this block.
     *
     * @param location The block.
     * @return If snow can reach it.
     */
    private boolean canSnowReach(Location<?> location) {
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
    private boolean canPlaceSnowAt(Location<?> location) {
        if (location.getBlockType() == BlockTypes.SNOW_LAYER)
            return true;

        Optional<ReplaceableProperty> replaceableProperty = location.getBlockType().getProperty(ReplaceableProperty.class);

        return replaceableProperty.isPresent()
                && !(location.getBlockType() == BlockTypes.WATER
                || location.getBlockType() == BlockTypes.FLOWING_WATER
                || location.getBlockType() == BlockTypes.LAVA
                || location.getBlockType() == BlockTypes.FLOWING_LAVA)
                && replaceableProperty.get().getValue();
    }

    /**
     * Determines if the specified block is buried under snow.
     *
     * @param location The location of the block.
     * @return If it is buried.
     */
    private boolean isBlockBuried(Location<?> location) {
        return location.getRelative(Direction.UP).getBlockType() == BlockTypes.SNOW_LAYER
                || location.getRelative(Direction.UP).getBlockType() == BlockTypes.SNOW;
    }

    @Override
    public String getName() {
        return "BetterSnow";
    }
}
