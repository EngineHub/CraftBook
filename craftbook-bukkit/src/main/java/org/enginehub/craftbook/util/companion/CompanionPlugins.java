/*
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package org.enginehub.craftbook.util.companion;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;

public class CompanionPlugins {

    /**
     * Required dependencies
     */
    private WorldEditPlugin worldEditPlugin;

    /**
     * Optional dependencies
     */
    private Economy economy;
    private WorldGuardPlugin worldGuardPlugin;

    public void initiate(CraftBookPlugin plugin) {
        // Check plugin for checking the active states of a plugin
        Plugin checkPlugin;

        // Check for WorldEdit
        checkPlugin = plugin.getServer().getPluginManager().getPlugin("WorldEdit");
        if (checkPlugin instanceof WorldEditPlugin) {
            worldEditPlugin = (WorldEditPlugin) checkPlugin;
        } else {
            try {
                //noinspection UnusedDeclaration
                @SuppressWarnings("unused")
                String s = WorldEditPlugin.CUI_PLUGIN_CHANNEL;
            } catch (Throwable t) {
                plugin.getLogger().severe("WorldEdit detection has failed!");
                plugin.getLogger().severe("WorldEdit is a required dependency, Craftbook disabled!");
                return;
            }
        }

        // Resolve WorldGuard
        checkPlugin = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        if (checkPlugin instanceof WorldGuardPlugin) {
            worldGuardPlugin = (WorldGuardPlugin) checkPlugin;
        } else worldGuardPlugin = null;

        // Resolve Vault
        try {
            RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            if (economyProvider != null) economy = economyProvider.getProvider();
            else economy = null;
        } catch (Throwable e) {
            economy = null;
        }
    }

    /**
     * Gets the Vault {@link Economy} service if it exists, this method should be used for economic
     * actions.
     *
     * This method can return null.
     *
     * @return The vault {@link Economy} service
     */
    public Economy getEconomy() {
        return this.economy;
    }

    /**
     * Gets a copy of the {@link WorldEditPlugin}.
     *
     * This method cannot return null.
     *
     * @return The {@link WorldEditPlugin} instance
     */
    public WorldEditPlugin getWorldEdit() {
        return this.worldEditPlugin;
    }

    /**
     * Gets the {@link WorldGuardPlugin} for non-build checks.
     *
     * This method can return null.
     *
     * @return {@link WorldGuardPlugin}
     */
    public WorldGuardPlugin getWorldGuard() {
        return this.worldGuardPlugin;
    }
}
