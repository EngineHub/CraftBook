package com.sk89q.craftbook.bukkit;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;

public class MechanismsPluginListener extends ServerListener {
    public MechanismsPluginListener() {
    }

    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        if(MechanismsPlugin.worldGuard == null) {
            Plugin worldGuard = MechanismsPlugin.server.getPluginManager().getPlugin("WorldGuard");
            if (worldGuard != null) {
                MechanismsPlugin.worldGuard = (WorldGuardPlugin) worldGuard;
            }
        }
    }
}
