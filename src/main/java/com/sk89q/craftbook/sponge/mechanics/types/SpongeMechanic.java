package com.sk89q.craftbook.sponge.mechanics.types;

import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.sk89q.craftbook.core.Mechanic;
import com.sk89q.craftbook.core.mechanics.MechanicData;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.sponge.CraftBookPlugin;

public abstract class SpongeMechanic implements Mechanic {

    private static String name;

    @Override
    public String getName() {
        if (name == null) name = this.getClass().getSimpleName();
        return name;
    }
    
    @Override
    public void onInitialize() throws CraftBookException {

    }

    public <T extends MechanicData> T getData(Class<T> clazz, Location block) {

        StringBuilder builder = new StringBuilder();

        if (block.getExtent() instanceof World) builder.append(((World) block.getExtent()).getName()).append('.');
        builder.append(block.getX()).append('.');
        builder.append(block.getY()).append('.');
        builder.append(block.getZ());

        return CraftBookPlugin.inst().getCache().getMechanicData(clazz, builder.toString());
    }

    @Override
    public <T extends MechanicData> T getData(Class<T> clazz, String locationKey) {

        return CraftBookPlugin.inst().getCache().getMechanicData(clazz, locationKey);
    }
}
