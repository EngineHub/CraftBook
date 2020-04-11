/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
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
package org.enginehub.craftbook.sponge.util.report;

import com.me4502.modularframework.module.ModuleWrapper;
import com.sk89q.worldedit.util.report.DataReport;
import org.enginehub.craftbook.util.ConfigValue;
import org.enginehub.craftbook.util.documentation.DocumentationProvider;
import org.enginehub.craftbook.sponge.CraftBookPlugin;
import org.enginehub.craftbook.sponge.st.SpongeSelfTriggerManager;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SpongeReportWriter extends DataReport {

    public SpongeReportWriter(String title) {
        super(title);
    }

    @Override
    public void appendPlatformSections() {
        appendServerInformation();
        appendPluginInformation();
        appendCraftBookInformation();
        appendGlobalConfiguration();
        appendModuleConfigurations();
    }

    private void appendGlobalConfiguration() {
        appendHeader("Global Configuration");

        LogListBlock log = new LogListBlock();
        LogListBlock configLog = log.putChild("Configuration");

        for (ConfigValue config : CraftBookPlugin.spongeInst().getConfig().getConfigurationNodes()) {
            ConfigurationNode node = SimpleCommentedConfigurationNode.root();
            config.serializeDefault(node);

            configLog.put(config.getKey(), node.getString("UNKNOWN"));
        }

        append(log);
        appendln();
    }

    private void appendServerInformation() {
        appendHeader("Server Information");

        LogListBlock log = new LogListBlock();

        Runtime runtime = Runtime.getRuntime();

        log.put("Java", "%s %s (%s)",
                System.getProperty("java.vendor"),
                System.getProperty("java.version"),
                System.getProperty("java.vendor.url"));
        log.put("Operating system", "%s %s (%s)",
                System.getProperty("os.name"),
                System.getProperty("os.version"),
                System.getProperty("os.arch"));
        log.put("Available processors", runtime.availableProcessors());
        log.put("Free memory", runtime.freeMemory() / 1024 / 1024 + " MB");
        log.put("Max memory", runtime.maxMemory() / 1024 / 1024 + " MB");
        log.put("Total memory", runtime.totalMemory() / 1024 / 1024 + " MB");
        //log.put("Server ID", Sponge.getServer().getServerId());
        //log.put("Server name", Sponge.getServerName());
        log.put("Platform", Sponge.getPlatform().getType().name());
        log.put("Game version", Sponge.getPlatform().getMinecraftVersion().getName());
        log.put("Player count", "%d/%d", Sponge.getServer().getOnlinePlayers().size(), Sponge.getServer().getMaxPlayers());

        append(log);
        appendln();
    }

    private void appendCraftBookInformation() {
        appendHeader("CraftBook Information");

        LogListBlock log = new LogListBlock();

        List<ModuleWrapper> enabledModules = CraftBookPlugin.spongeInst().moduleController.getModules().stream()
                .filter(ModuleWrapper::isEnabled).collect(Collectors.toList());

        int i = enabledModules.size();
        log.put("Mechanics Loaded", "%d", i);

        Optional<SpongeSelfTriggerManager> selfTriggerManagerOptional = CraftBookPlugin.spongeInst().getSelfTriggerManager()
                .map(stm -> (SpongeSelfTriggerManager) stm);
        log.put("ST Mechanics Loaded", "%d",
                selfTriggerManagerOptional.map(stm -> stm.getSelfTriggeringMechanics().size()).orElse(0));

        append(log);
        appendln();

        appendHeader("Loaded Mechanics");

        log = new LogListBlock();

        for(ModuleWrapper<?> mech : enabledModules) {
            log.put(mech.getName(), mech.getId() + '-' + mech.getVersion());
        }

        append(log);
        appendln();
    }

    private void appendPluginInformation() {
        Collection<PluginContainer> plugins = Sponge.getPluginManager().getPlugins();
        appendHeader("Plugins (" + plugins.size() + ')');

        LogListBlock log = new LogListBlock();

        for (PluginContainer plugin : plugins) {
            log.put(plugin.getName(), plugin.getVersion().orElse("UNKNOWN"));
        }

        append(log);
        appendln();
    }

    private void appendModuleConfigurations() {
        appendHeader("Module Configurations");

        LogListBlock log = new LogListBlock();

        List<ModuleWrapper> enabledModules = CraftBookPlugin.spongeInst().moduleController.getModules().stream()
                .filter(ModuleWrapper::isEnabled).collect(Collectors.toList());

        for (ModuleWrapper<?> moduleWrapper : enabledModules) {
            Object module = moduleWrapper.getModule().orElse(null);
            if (module == null || !(module instanceof DocumentationProvider)) {
                continue;
            }
            LogListBlock configLog = log.putChild(moduleWrapper.getName());

            for (ConfigValue config : ((DocumentationProvider) module).getConfigurationNodes()) {
                ConfigurationNode node = SimpleCommentedConfigurationNode.root();
                config.serializeDefault(node);

                configLog.put(config.getKey(), node.getString("UNKNOWN"));
            }
        }

        append(log);
        appendln();
    }
}
