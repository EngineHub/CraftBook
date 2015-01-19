package com.sk89q.craftbook.sponge;

import java.util.HashSet;
import java.util.Set;

import org.spongepowered.api.Game;
import org.spongepowered.api.event.state.PreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.util.event.Subscribe;

import com.sk89q.craftbook.core.CraftBookAPI;
import com.sk89q.craftbook.core.Mechanic;
import com.sk89q.craftbook.sponge.mechanics.Elevator;

@Plugin(id = "CraftBook", name = "CraftBook", version = "4.0", dependencies = "required-after:WorldEdit@[6.0,)")
public class CraftBookPlugin extends CraftBookAPI {

    public static Game game;

    private Set<Mechanic> enabledMechanics = new HashSet<Mechanic>();

    @Subscribe
    public void onPreInitialization(PreInitializationEvent event) {

        game = event.getGame();
        instance = this;

        discoverMechanics();

        for(Mechanic mech : getAvailableMechanics()) {

            //TODO is enabled check.

            enabledMechanics.add(mech);
        }
    }

    @Override
    public void discoverMechanics() {

        registerMechanic(new Elevator());
    }
}