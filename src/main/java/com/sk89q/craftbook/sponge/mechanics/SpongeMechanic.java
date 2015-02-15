package com.sk89q.craftbook.sponge.mechanics;

import org.spongepowered.api.block.BlockLoc;
import org.spongepowered.api.world.World;

import com.sk89q.craftbook.core.Mechanic;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.util.SpongeMechanicData;

public abstract class SpongeMechanic implements Mechanic {

    private String name;

    @Override
    public String getName() {
        if(name == null)
            name = this.getClass().getSimpleName();
        return name;
    }

    @Override
    public void onInitialize() throws CraftBookException {

    }

    public SpongeMechanicData getData(BlockLoc block) {

        StringBuilder builder = new StringBuilder();

        if(block.getExtent() instanceof World)
            builder.append(((World) block.getExtent()).getName()).append('.');
        builder.append(block.getX()).append('.');
        builder.append(block.getY()).append('.');
        builder.append(block.getZ());

        return (SpongeMechanicData) CraftBookPlugin.inst().getCache().getMechanicData(builder.toString());
    }

    @Override
    public SpongeMechanicData getData(String locationKey) {

        return (SpongeMechanicData) CraftBookPlugin.inst().getCache().getMechanicData(locationKey);
    }
}