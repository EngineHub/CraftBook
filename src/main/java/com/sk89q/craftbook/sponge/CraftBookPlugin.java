package com.sk89q.craftbook.sponge;

import java.util.HashSet;
import java.util.Set;

import org.spongepowered.api.event.state.PreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.util.event.Subscribe;

import com.sk89q.craftbook.core.CraftBookAPI;
import com.sk89q.craftbook.core.Mechanic;

@Plugin(id = "CraftBook", name = "CraftBook", version = "4.0", dependencies = "required-after:WorldEdit@[6.0,)")
public class CraftBookPlugin extends CraftBookAPI {

    private Set<Mechanic> enabledMechanics = new HashSet<Mechanic>();

    @Subscribe
    public void onPreInitialization(PreInitializationEvent event) {

        instance = this;

        discoverFactories();

        for(Mechanic mech : getAvailableMechanics()) {

            //TODO is enabled check.

            enabledMechanics.add(mech);
        }
    }

    @Override
    public void discoverFactories() {

    }
}