package com.sk89q.craftbook.sponge.mechanics;

import com.google.common.base.Optional;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.manipulator.block.LayeredData;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.block.BlockRandomTickEvent;
import org.spongepowered.api.event.block.BlockUpdateEvent;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.weather.Weathers;

public class Snow extends SpongeMechanic {

    @Subscribe
    public void onBlockTick(BlockRandomTickEvent event) {
        if ((event.getBlock().getBlockType() == BlockTypes.SNOW_LAYER || event.getBlock().getBlockType() == BlockTypes.SNOW)) {
            if (event.getBlock().getExtent().getWeather() != Weathers.CLEAR) {
                //Higher the snow.
                if (event.getBlock().getBlockType() == BlockTypes.SNOW_LAYER && canSeeSky(event.getBlock()) && event.getBlock().getTemperature() <= 0.15f) //Only increase snow at valid blocks, where snow could actually fall.
                    increaseSnow(event.getBlock(), true);
            } else if(!isBlockBuried(event.getBlock())) { //Only melt if on top, and too hot.
                //Lower the snow.
                Optional<LayeredData> data = event.getBlock().getData(LayeredData.class);
                if(data.isPresent()) {
                    if(data.get().getValue() == 0)
                        return;
                } else if (event.getBlock().getTemperature() > 0.15f) {
                    return;
                }

                decreaseSnow(event.getBlock());
            }
        }
    }

    public void increaseSnow(Location location, boolean disperse) {
        Optional<LayeredData> dataOptional = location.getData(LayeredData.class);
        if(dataOptional.isPresent()) {
            LayeredData data = dataOptional.get();
            int currentHeight = data.getValue();
            currentHeight ++;
            if(currentHeight > data.getMaxValue())
                location.setBlockType(BlockTypes.SNOW);
            else {
                if(disperse) {
                    disperseSnow(location, null);
                } else {
                    data.setValue(currentHeight);
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
        Optional<LayeredData> dataOptional = location.getData(LayeredData.class);
        if(dataOptional.isPresent()) {
            LayeredData data = dataOptional.get();
            int currentHeight = data.getValue();
            currentHeight --;
            if(currentHeight < data.getMinValue())
                location.setBlockType(BlockTypes.AIR);
            else {
                data.setValue(currentHeight);
                location.offer(data);
            }
        } else if (location.getBlockType() == BlockTypes.SNOW) {
            location.setBlockType(BlockTypes.SNOW_LAYER);
            dataOptional = location.getOrCreate(LayeredData.class);
            LayeredData data = dataOptional.get();
            data.setValue(data.getMaxValue());
            location.offer(data);
        }
    }

    private static final Direction[] VALID_SNOW_DIRECTIONS = new Direction[]{Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.NONE};

    public void disperseSnow(final Location location, Direction ignoredFace) {

        Optional<LayeredData> heightData = location.getOrCreate(LayeredData.class);
        int currentHeight = heightData.get().getValue();

        for(final Direction dir : VALID_SNOW_DIRECTIONS) {
            if(dir == ignoredFace) continue;
            if(currentHeight == 0 && !(dir == Direction.DOWN || dir == Direction.NONE)) continue; //Stop snow moving around on the ground.
            final Location relative = location.getRelative(dir);
            if(canPlaceSnowAt(relative)) {
                Optional<LayeredData> dataOptional = relative.getData(LayeredData.class);
                if(dataOptional.isPresent()) {
                    int otherHeight = dataOptional.get().getValue();
                    if(dir != Direction.NONE && dir != Direction.DOWN && currentHeight <= otherHeight+1)
                        continue;
                }
                increaseSnow(relative, false);
                if(dir != Direction.NONE) {
                    decreaseSnow(location);
                    CraftBookPlugin.game.getScheduler().getTaskBuilder().delay(40L).execute(new Runnable() {
                        @Override
                        public void run() {
                            disperseSnow(relative, dir.getOpposite());
                            if(isBlockBuried(location))
                                disperseSnow(location.getRelative(Direction.UP), Direction.NONE);
                        }
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
        if(location.getBlockType() == BlockTypes.SNOW_LAYER) return true;
        if(location.getBlockType() == BlockTypes.WATER || location.getBlockType() == BlockTypes.FLOWING_WATER || location.getBlockType() == BlockTypes.LAVA || location.getBlockType() == BlockTypes.FLOWING_LAVA) return false;
        return location.getBlockType().isReplaceable();
    }

    public boolean isBlockBuried(Location location) {
        return location.getRelative(Direction.UP).getBlockType() == BlockTypes.SNOW_LAYER || location.getRelative(Direction.UP).getBlockType() == BlockTypes.SNOW;
    }

    @Subscribe
    public void onBlockUpdate(BlockUpdateEvent event) {

        if (event.getBlock().getBlockType() == BlockTypes.SNOW || event.getBlock().getBlockType() == BlockTypes.SNOW_LAYER) {
            // Occurred in a block where a snow-related change could have happened.
            for (Location block : event.getAffectedBlocks()) {

            }
        }
    }

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
