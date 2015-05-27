package com.sk89q.craftbook.sponge.mechanics;

import com.google.common.base.Optional;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.manipulator.block.LayeredData;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.block.BlockRandomTickEvent;
import org.spongepowered.api.event.block.BlockUpdateEvent;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.weather.Weathers;

import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;

public class Snow extends SpongeMechanic {

    @Subscribe
    public void onBlockTick(BlockRandomTickEvent event) {
        if ((event.getBlock().getType() == BlockTypes.SNOW_LAYER || event.getBlock().getType() == BlockTypes.SNOW) && canSeeSky(event.getBlock())) {
            if (event.getBlock().getExtent().getWeather() != Weathers.CLEAR) {
                //Higher the snow.
                if (event.getBlock().getType() == BlockTypes.SNOW_LAYER)
                    increaseSnow(event.getBlock(), true);
            } else {
                //Lower the snow.
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
                location.replaceWith(BlockTypes.SNOW);
            else {
                if(disperse) {
                    disperseSnow(location, null);
                } else {
                    data.setValue(currentHeight);
                    location.offer(data);
                }
            }
        } else {
            location.replaceWith(BlockTypes.SNOW_LAYER);
        }
    }

    public void decreaseSnow(Location location) {

        Optional<LayeredData> dataOptional = location.getData(LayeredData.class);
        if(dataOptional.isPresent()) {
            LayeredData data = dataOptional.get();
            int currentHeight = data.getValue();
            currentHeight --;
            if(currentHeight < data.getMinValue())
                location.replaceWith(BlockTypes.AIR);
            else {
                data.setValue(currentHeight);
                location.offer(data);
            }
        } else if (location.getType() == BlockTypes.SNOW) {
            location.replaceWith(BlockTypes.SNOW_LAYER);
            dataOptional = location.getOrCreate(LayeredData.class);
            LayeredData data = dataOptional.get();
            data.setValue(data.getMaxValue());
            location.offer(data);
        }
    }

    private static final Direction[] VALID_SNOW_DIRECTIONS = new Direction[]{Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.NONE};

    public void disperseSnow(Location location, Direction ignoredFace) {

        int currentHeight = location.getOrCreate(LayeredData.class).get().getValue();

        for(final Direction dir : VALID_SNOW_DIRECTIONS) {
            if(dir == ignoredFace) continue;
            final Location relative = location.getRelative(dir);
            if(canPlaceSnowAt(relative)) {
                Optional<LayeredData> dataOptional = relative.getData(LayeredData.class);
                if(dataOptional.isPresent()) {
                    if(currentHeight < dataOptional.get().getValue())
                        continue;
                }
                increaseSnow(relative, false);
                if(dir != Direction.NONE) {
                    decreaseSnow(location);
                    CraftBookPlugin.game.getSyncScheduler().runTaskAfter(CraftBookPlugin.<CraftBookPlugin>inst(), new Runnable() {
                        @Override
                        public void run() {
                            disperseSnow(relative, dir.getOpposite());
                        }
                    }, 40L);
                }
                break;
            }
        }
    }

    public boolean canSeeSky(Location location) {
        while(location.getBlockY() < location.getExtent().getBlockMax().getY()) {
            location = location.getRelative(Direction.UP);
            if(location.getType() != BlockTypes.AIR)
                return false;
        }
        return true;
    }

    public boolean canPlaceSnowAt(Location location) {
        if(location.getType() == BlockTypes.AIR || location.getType() == BlockTypes.SNOW_LAYER)
            return true;

        return false;
    }

    @Subscribe
    public void onBlockUpdate(BlockUpdateEvent event) {

        if (event.getBlock().getType() == BlockTypes.SNOW || event.getBlock().getType() == BlockTypes.SNOW_LAYER) {
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
