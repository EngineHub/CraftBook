package com.sk89q.craftbook.sponge.mechanics;

import com.google.common.base.Optional;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.manipulator.block.LayeredData;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.block.BlockRandomTickEvent;
import org.spongepowered.api.event.block.BlockUpdateEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.weather.Weathers;

import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;

public class Snow extends SpongeMechanic {

    @Subscribe
    public void onBlockTick(BlockRandomTickEvent event) {
        if (event.getBlock().getType() == BlockTypes.SNOW_LAYER || event.getBlock().getType() == BlockTypes.SNOW) {
            if (event.getBlock().getExtent().getWeather() != Weathers.CLEAR) {
                //Higher the snow.
                if (event.getBlock().getType() == BlockTypes.SNOW_LAYER)
                    increaseSnow(event.getBlock());
            } else {
                //Lower the snow.
                decreaseSnow(event.getBlock());
            }
        }
    }

    public void increaseSnow(Location location) {

        Optional<LayeredData> dataOptional = location.getData(LayeredData.class);
        if(dataOptional.isPresent()) {
            LayeredData data = dataOptional.get();
            int currentHeight = data.getValue();
            currentHeight ++;
            if(currentHeight > data.getMaxValue())
                location.replaceWith(BlockTypes.SNOW);
            else {
                data.setValue(currentHeight);
                location.offer(data);
            }
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
        } else {
            location.replaceWith(BlockTypes.SNOW_LAYER);
            dataOptional = location.getOrCreate(LayeredData.class);
            LayeredData data = dataOptional.get();
            data.setValue(data.getMaxValue());
            location.offer(data);
        }
    }

    @Subscribe
    public void onBlockUpdate(BlockUpdateEvent event) {

        if (event.getBlock().getType() == BlockTypes.SNOW || event.getBlock().getType() == BlockTypes.SNOW_LAYER || event.getBlock().getType() == BlockTypes.AIR) {
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
