package com.sk89q.craftbook.mech;

import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.craftbook.mech.ai.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.ArrayList;

public class AIMechanic implements Listener {

    ArrayList<Class<BaseAIMechanic>> mechanics = new ArrayList<Class<BaseAIMechanic>>();

    public AIMechanic(MechanismsPlugin plugin) {

        if (!plugin.getLocalConfiguration().aiSettings.enabled) return;

        if (plugin.getLocalConfiguration().aiSettings.zombieVision) {
            registerAIMechanic(ZombieAIMechanic.class);
        }
        if (plugin.getLocalConfiguration().aiSettings.skeletonCriticals) {
            registerAIMechanic(SkeletonAIMechanic.class);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event) {

        if (event.getTarget() == null || event.getEntity() == null) return;
        for (Class<BaseAIMechanic> mechanic : mechanics) {
            try {
                if (!TargetAIMechanic.class.isAssignableFrom(mechanic)) {
                    continue;
                }
                TargetAIMechanic ai = (TargetAIMechanic) mechanic.getConstructors()[0].newInstance(this,
                        event.getEntity());
                if (ai == null) return;
                ai.onEntityTarget(event);
            } catch (Exception ignored) {
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityShootBow(EntityShootBowEvent event) {

        if (event.getEntity() == null) return;
        for (Class<BaseAIMechanic> mechanic : mechanics) {
            try {
                if (!BowShotAIMechanic.class.isAssignableFrom(mechanic)) {
                    continue;
                }
                BowShotAIMechanic ai = (BowShotAIMechanic) mechanic.getConstructors()[0].newInstance(this,
                        event.getEntity());
                if (ai == null) return;
                ai.onBowShot(event);
            } catch (Exception ignored) {
            }
        }
    }

    @SuppressWarnings("unchecked")
    public boolean registerAIMechanic(Class<?> mechanic) {

        return BaseAIMechanic.class.isAssignableFrom(mechanic) && !mechanics.contains(mechanic) && mechanics.add(
                (Class<BaseAIMechanic>) mechanic);

    }
}