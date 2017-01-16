package com.sk89q.craftbook.sponge.mechanics.ics;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;

public abstract class ICFactory<T extends IC> {

    public T create(Player player, List<Text> lines, Location<World> location) throws InvalidICException {
        return createInstance(location);
    }

    public abstract T createInstance(Location<World> location);
}
