package com.sk89q.craftbook.sponge.mechanics;

import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.event.block.BlockRandomTickEvent;
import org.spongepowered.api.event.block.BlockUpdateEvent;
import org.spongepowered.api.util.event.Subscribe;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.weather.WeatherUniverse;
import org.spongepowered.api.world.weather.Weathers;

import com.sk89q.craftbook.core.util.CraftBookException;

public class Snow extends SpongeMechanic {

    @Subscribe
    public void onBlockTick(BlockRandomTickEvent event) {

        if (event.getBlock().getExtent() instanceof WeatherUniverse) {
            if (event.getBlock().getType() == BlockTypes.SNOW_LAYER) {
                if (event.getBlock().getExtent().getWeather() != Weathers.CLEAR) {
                    // Temporarily just set to a full snow block.
                    event.getBlock().replaceWith(BlockTypes.SNOW);
                }
            }
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
    }
}
