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

package com.sk89q.craftbook.bukkit;

import static com.sk89q.worldedit.util.formatting.WorldEditText.reduceToText;

import com.sk89q.bukkit.util.CommandInfo;
import com.sk89q.bukkit.util.CommandRegistration;
import com.sk89q.craftbook.CraftBook;
import com.sk89q.craftbook.CraftBookPlatform;
import com.sk89q.craftbook.YamlConfiguration;
import com.sk89q.craftbook.mechanic.BukkitMechanicManager;
import com.sk89q.craftbook.mechanic.MechanicManager;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.util.PermissionCondition;
import com.sk89q.worldedit.util.report.ReportList;
import com.sk89q.craftbook.util.profile.cache.ProfileCache;
import com.sk89q.craftbook.util.profile.resolver.BukkitPlayerService;
import com.sk89q.craftbook.util.profile.resolver.CacheForwardingService;
import com.sk89q.craftbook.util.profile.resolver.CombinedProfileService;
import com.sk89q.craftbook.util.profile.resolver.HttpRepositoryService;
import com.sk89q.craftbook.util.profile.resolver.ProfileService;
import com.sk89q.craftbook.util.profile.resolver.PaperPlayerService;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.enginehub.piston.CommandManager;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BukkitCraftBookPlatform implements CraftBookPlatform {

    private final MechanicManager mechanicManager = new BukkitMechanicManager();
    private YamlConfiguration config;

    @Override
    public String getPlatformName() {
        return "Bukkit-Official";
    }

    @Override
    public String getPlatformVersion() {
        return null;
    }

    @Override
    public void load() {
        this.mechanicManager.setup();

        // Load the configuration
        try {
            CraftBookPlugin.inst().createDefaultConfiguration("config.yml");
        } catch (Exception ignored) {
        }
        config = new BukkitConfiguration(
                new YAMLProcessor(CraftBook.getInstance().getPlatform().getConfigDir().resolve("config.yml").toFile(), true, YAMLFormat.EXTENDED));

        try {
            config.load();
        } catch (Throwable e) {
            CraftBook.logger.error("Failed to load CraftBook Configuration File! Is it corrupt?", e);
            CraftBook.logger.error("Disabling CraftBook due to invalid Configuration File!");
            Bukkit.getPluginManager().disablePlugin(CraftBookPlugin.inst());
            return;
        }
    }

    @Override
    public void unload() {
        this.mechanicManager.shutdown();
    }

    @Override
    public void registerCommands(CommandManager commandManager) {
        BukkitCommandInspector inspector = new BukkitCommandInspector(CraftBookPlugin.inst(), commandManager);

        CommandRegistration registration = new CommandRegistration(CraftBookPlugin.inst());
        registration.register(commandManager.getAllCommands()
                .map(command -> {
                    String[] permissionsArray = command.getCondition()
                            .as(PermissionCondition.class)
                            .map(PermissionCondition::getPermissions)
                            .map(s -> s.toArray(new String[0]))
                            .orElseGet(() -> new String[0]);

                    String[] aliases = Stream.concat(
                            Stream.of(command.getName()),
                            command.getAliases().stream()
                    ).toArray(String[]::new);
                    // TODO Handle localisation correctly
                    return new CommandInfo(reduceToText(command.getUsage(), WorldEdit.getInstance().getConfiguration().defaultLocale),
                            reduceToText(command.getDescription(), WorldEdit.getInstance().getConfiguration().defaultLocale), aliases,
                            inspector, permissionsArray);
                }).collect(Collectors.toList()));
    }

    public void resetCommandRegistration(CraftBookPlugin plugin) {
        CommandRegistration registration = new CommandRegistration(plugin);
        registration.unregisterCommands();
        plugin.getCommandManager().registerCommandsWith(CraftBook.getInstance().getPlatform());
    }

    @Override
    public Path getConfigDir() {
        return CraftBookPlugin.inst().getDataFolder().toPath();
    }

    @Override
    public MechanicManager getMechanicManager() {
        return this.mechanicManager;
    }

    @Override
    public YamlConfiguration getConfiguration() {
        return this.config;
    }

    @Override
    public void addPlatformReports(ReportList report) {

    }

    @Override
    public ProfileService createProfileService(ProfileCache profileCache) {
        List<ProfileService> services = new ArrayList<>();
        if (PaperLib.isPaper()) {
            // Paper has a shared cache
            services.add(PaperPlayerService.getInstance());
        } else {
            services.add(BukkitPlayerService.getInstance());
        }
        services.add(HttpRepositoryService.forMinecraft());
        return new CacheForwardingService(new CombinedProfileService(services), profileCache);
    }

    @Override
    public boolean isPluginAvailable(String pluginName) {
        return Bukkit.getPluginManager().isPluginEnabled(pluginName);
    }
}
