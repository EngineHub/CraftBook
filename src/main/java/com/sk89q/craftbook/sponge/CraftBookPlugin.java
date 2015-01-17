package com.sk89q.craftbook.sponge;

import java.util.HashSet;
import java.util.Set;

import org.spongepowered.api.event.state.PreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.util.event.Subscribe;

import com.sk89q.craftbook.core.CraftBookAPI;
import com.sk89q.craftbook.core.Mechanic;
import com.sk89q.craftbook.core.MechanicFactory;

@Plugin(id = "CraftBook", name = "CraftBook", version = "4.0", dependencies = "required-after:WorldEdit@[6.0,)")
public class CraftBookPlugin extends CraftBookAPI {

    private Set<MechanicFactory<? extends Mechanic>> enabledMechanics = new HashSet<MechanicFactory<? extends Mechanic>>();

    @Subscribe
    public void onPreInitialization(PreInitializationEvent event) {

        instance = this;

        discoverFactories();

        for(MechanicFactory<? extends Mechanic> mech : getAvailableMechanics()) {

            //TODO is enabled check.

            enabledMechanics.add(mech);
        }
    }

    @Override
    public void discoverFactories() {

    }
}