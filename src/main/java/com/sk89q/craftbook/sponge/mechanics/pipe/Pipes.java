package com.sk89q.craftbook.sponge.mechanics.pipe;

import com.me4502.modularframework.module.Module;
import com.sk89q.craftbook.sponge.mechanics.pipe.parts.PassthroughPipePart;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import org.spongepowered.api.world.Location;

import java.util.ArrayList;
import java.util.List;

@Module(moduleName = "Pipes", onEnable="onInitialize", onDisable="onDisable")
public class Pipes extends SpongeBlockMechanic {

    private PipePart[] pipeParts;

    @Override
    public void onInitialize() {
        List<PipePart> pipePartList = new ArrayList<>();
        pipePartList.add(new PassthroughPipePart());

        pipeParts = pipePartList.toArray(new PipePart[pipePartList.size()]);
    }

    @Override
    public boolean isValid(Location location) {
        //TODO
        return false;
    }
}
