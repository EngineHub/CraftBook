package com.sk89q.craftbook.sponge.mechanics;

import org.spongepowered.api.block.BlockLoc;

import com.sk89q.craftbook.core.Mechanic;
import com.sk89q.craftbook.core.mechanics.MechanicData;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.sponge.CraftBookPlugin;

public abstract class SpongeMechanic implements Mechanic {

    @Override
    public void onInitialize() throws CraftBookException {

        CraftBookPlugin.game.getEventManager().register(CraftBookPlugin.inst(), this);
    }

    @Override
    public MechanicData getData(BlockLoc location) {

        //TODO
        return null;
    }
}
