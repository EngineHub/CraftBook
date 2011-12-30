package com.sk89q.craftbook.bukkit;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

public class CraftBookPluginListener extends ServerListener {
    public CraftBookPluginListener() {
    }

    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        if(BaseBukkitPlugin.worldGuard == null) {
            Plugin worldGuard = BaseBukkitPlugin.server.getPluginManager().getPlugin("WorldGuard");
            if (worldGuard != null) {
                BaseBukkitPlugin.worldGuard = (WorldGuardPlugin) worldGuard;
                BaseBukkitPlugin.logger.info("CraftBook hooked into WorldGuard");
            }
        }
    }
}
