// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.bukkit;

import org.bukkit.command.CommandSender;

import com.sk89q.craftbook.CommonConfiguration;
import com.sk89q.craftbook.bukkit.Metrics.Graph;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;

/**
 * Plugin for CraftBook's core.
 *
 * @author sk89q
 */
public class CraftBookPlugin extends BaseBukkitPlugin {

    private static CraftBookPlugin instance;

    public static CraftBookPlugin getInstance() {

        return instance;
    }

    private CommonConfiguration config;

    public CraftBookPlugin() {

        instance = this;
    }

    @Override
    public void onEnable() {

        super.onEnable();
        config = new CommonConfiguration(getConfig(), getDataFolder());
        saveConfig();

        registerCommand(Commands.class);

        try {
            Metrics metrics = new Metrics(this);

            Graph graph = metrics.createGraph("Language");
            for (String lan : languageManager.getLanguages()) {
                graph.addPlotter(new Metrics.Plotter(lan) {

                    @Override
                    public int getValue() {

                        return 1;
                    }
                });
            }

            metrics.start();
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void registerEvents() {

    }

    @Override
    public CommonConfiguration getLocalConfiguration() {

        return config;
    }

    public class Commands {

        @Command(
                aliases = "cbreload",
                desc = "Reloads the CraftBook Common config"
                )
        public void reload(CommandContext context, CommandSender sender) {

            getLocalConfiguration().reload();
            sender.sendMessage("The CraftBook Common config has been reloaded.");
        }
    }
}
