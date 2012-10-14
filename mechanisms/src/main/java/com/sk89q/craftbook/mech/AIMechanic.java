package com.sk89q.craftbook.mech;

import java.util.ArrayList;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.craftbook.mech.ai.BaseAIMechanic;
import com.sk89q.craftbook.mech.ai.TargetAIMechanic;
import com.sk89q.craftbook.mech.ai.ZombieAIMechanic;

public class AIMechanic implements Listener {

    public MechanismsPlugin plugin;
    ArrayList<Class<BaseAIMechanic>> mechanics = new ArrayList<Class<BaseAIMechanic>>();

    public AIMechanic(MechanismsPlugin plugin) {

        this.plugin = plugin;

        registerAIMechanic(ZombieAIMechanic.class);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event) {

        if (!plugin.getLocalConfiguration().aiSettings.enabled) return;
        if (event.getTarget() == null || event.getEntity() == null) return;
        for(Class<BaseAIMechanic> mechanic : mechanics) {
            try {
                if(!TargetAIMechanic.class.isAssignableFrom(mechanic)) continue;
                TargetAIMechanic ai = (TargetAIMechanic) mechanic.getConstructors()[0].newInstance(this, event.getEntity());
                if (ai == null) return;
                ai.onEntityTarget(event);
            }
            catch(Exception e){}
        }
    }

    @SuppressWarnings("unchecked")
    public boolean registerAIMechanic(Class<?> mechanic) {

        if(!BaseAIMechanic.class.isAssignableFrom(mechanic)) return false;

        if(mechanics.contains(mechanic))
            return false;

        return mechanics.add((Class<BaseAIMechanic>) mechanic);
    }
}