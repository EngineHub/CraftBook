package com.sk89q.craftbook.sponge.mechanics;

import com.google.common.base.Optional;
import com.me4502.modularframework.module.Module;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.block.LayeredData;
import org.spongepowered.api.data.property.block.TemperatureProperty;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.TickBlockEvent;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.weather.Weathers;

@Module(moduleName = "Snow", onEnable="onInitialize", onDisable="onDisable")
public class Snow extends SpongeMechanic {

    @Listener
    public void onBlockTick(TickBlockEvent event) {
        if ((event.getTargetBlock().getState().getType() == BlockTypes.SNOW_LAYER || event.getTargetBlock().getState().getType() == BlockTypes.SNOW)) {
            if (event.getTargetBlock().getLocation().get().getExtent().getWeather() != Weathers.CLEAR) {
                //Higher the snow.
                if (event.getTargetBlock().getLocation().get().getBlockType() == BlockTypes.SNOW_LAYER && canSeeSky(event.getTargetBlock().getLocation().get()) && event.getTargetBlock().getLocation().get().getProperty(TemperatureProperty.class).get().getValue() <= 0.15f) //Only increase snow at valid blocks, where snow could actually fall.
                    increaseSnow(event.getTargetBlock().getLocation().get(), true);
            } else if(!isBlockBuried(event.getTargetBlock().getLocation().get())) { //Only melt if on top, and too hot.
                //Lower the snow.
                Optional<LayeredData> data = event.getTargetBlock().getLocation().get().get(LayeredData.class);
                if(data.isPresent()) {
                    if(data.get().getValue(Keys.LAYER).get().get() == 0)
                        return;
                } else if (event.getTargetBlock().getLocation().get().getProperty(TemperatureProperty.class).get().getValue() > 0.15f) {
                    return;
                }

                decreaseSnow(event.getTargetBlock().getLocation().get());
            }
        }
    }

    public void increaseSnow(Location location, boolean disperse) {
        Optional<LayeredData> dataOptional = location.get(LayeredData.class);
        if(dataOptional.isPresent()) {
            LayeredData data = dataOptional.get();
            int currentHeight = data.getValue(Keys.LAYER).get().get();
            currentHeight ++;
            if(currentHeight > ((BoundedValue<Integer>)data.getValue(Keys.LAYER).get()).getMaxValue())
                location.setBlockType(BlockTypes.SNOW);
            else {
                if(disperse) {
                    disperseSnow(location, null);
                } else {
                    data.set(Keys.LAYER, currentHeight);
                    location.offer(data);
                }
            }

            if(location.getRelative(Direction.DOWN).getBlockType() == BlockTypes.WATER || location.getRelative(Direction.DOWN).getBlockType() == BlockTypes.FLOWING_WATER)
                location.getRelative(Direction.DOWN).setBlockType(BlockTypes.ICE);
        } else {
            location.setBlockType(BlockTypes.SNOW_LAYER);
        }
    }

    public void decreaseSnow(Location location) {
        Optional<LayeredData> dataOptional = location.get(LayeredData.class);
        if(dataOptional.isPresent()) {
            LayeredData data = dataOptional.get();
            int currentHeight = data.getValue(Keys.LAYER).get().get();
            currentHeight --;
            if(currentHeight < ((BoundedValue<Integer>)data.getValue(Keys.LAYER).get()).getMinValue())
                location.setBlockType(BlockTypes.AIR);
            else {
                data.set(Keys.LAYER, currentHeight);
                location.offer(data);
            }
        } else if (location.getBlockType() == BlockTypes.SNOW) {
            location.setBlockType(BlockTypes.SNOW_LAYER);
            dataOptional = location.getOrCreate(LayeredData.class);
            LayeredData data = dataOptional.get();
            data.set(Keys.LAYER, (int)((BoundedValue)data.getValue(Keys.LAYER).get()).getMaxValue());
            location.offer(data);
        }
    }

    private static final Direction[] VALID_SNOW_DIRECTIONS = new Direction[]{Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.NONE};

    public void disperseSnow(final Location location, Direction ignoredFace) {

        Optional<LayeredData> heightData = location.getOrCreate(LayeredData.class);
        int currentHeight = heightData.get().getValue(Keys.LAYER).get().get();

        for(final Direction dir : VALID_SNOW_DIRECTIONS) {
            if(dir == ignoredFace) continue;
            if(currentHeight == 0 && !(dir == Direction.DOWN || dir == Direction.NONE)) continue; //Stop snow moving around on the ground.
            final Location relative = location.getRelative(dir);
            if(canPlaceSnowAt(relative)) {
                Optional<LayeredData> dataOptional = relative.get(LayeredData.class);
                if(dataOptional.isPresent()) {
                    int otherHeight = dataOptional.get().getValue(Keys.LAYER).get().get();
                    if(dir != Direction.NONE && dir != Direction.DOWN && currentHeight <= otherHeight+1)
                        continue;
                }
                increaseSnow(relative, false);
                if(dir != Direction.NONE) {
                    decreaseSnow(location);
                    CraftBookPlugin.game.getScheduler().createTaskBuilder().delay(40L).execute(() -> {
                        disperseSnow(relative, dir.getOpposite());
                        if(isBlockBuried(location))
                            disperseSnow(location.getRelative(Direction.UP), Direction.NONE);
                    }).submit(CraftBookPlugin.inst());
                }
                break;
            }
        }
    }

    public boolean canSeeSky(Location location) {
        while(location.getBlockY() < location.getExtent().getBlockMax().getY()) {
            location = location.getRelative(Direction.UP);
            if(location.getBlockType() != BlockTypes.AIR && location.getBlockType() != BlockTypes.LEAVES && location.getBlockType() != BlockTypes.LEAVES2)
                return false;
        }
        return true;
    }

    public boolean canPlaceSnowAt(Location location) {
        return location.getBlockType() == BlockTypes.SNOW_LAYER || !(location.getBlockType() == BlockTypes.WATER || location.getBlockType() == BlockTypes.FLOWING_WATER || location.getBlockType() == BlockTypes.LAVA || location.getBlockType() == BlockTypes.FLOWING_LAVA) && location.getBlockType().isReplaceable();
    }

    public boolean isBlockBuried(Location location) {
        return location.getRelative(Direction.UP).getBlockType() == BlockTypes.SNOW_LAYER || location.getRelative(Direction.UP).getBlockType() == BlockTypes.SNOW;
    }

    /*@Listener
    public void onBlockUpdate(BlockUpdateEvent event) {

        if (event.getBlock().getType() == BlockTypes.SNOW || event.getBlock().getType() == BlockTypes.SNOW_LAYER) {
            // Occurred in a block where a snow-related change could have happened.
            for (Location block : event.getLocations()) {

            }
        }
    }*/

    @Override
    public String getName() {
        return "BetterSnow";
    }

    @Override
    public void onInitialize() throws CraftBookException {

        super.onInitialize();

        BlockTypes.SNOW_LAYER.setTickRandomly(true);
        BlockTypes.SNOW.setTickRandomly(true);
    }
}
