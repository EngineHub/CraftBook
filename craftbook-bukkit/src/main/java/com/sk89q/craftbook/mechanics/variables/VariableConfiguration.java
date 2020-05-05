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

package com.sk89q.craftbook.mechanics.variables;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.Tuple2;
import com.sk89q.craftbook.util.profile.Profile;
import com.sk89q.craftbook.util.profile.resolver.HttpRepositoryService;
import com.sk89q.craftbook.util.profile.resolver.ProfileService;
import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Logger;

public class VariableConfiguration {

    public final YAMLProcessor config;
    protected final Logger logger;

    public VariableConfiguration(YAMLProcessor config, Logger logger) {

        this.config = config;
        this.logger = logger;
    }

    public void load() {

        try {
            config.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        boolean shouldSave = false;

        if(config.getKeys("variables") == null) return;
        for(String key : config.getKeys("variables")) {

            String[] keys = RegexUtil.PIPE_PATTERN.split(key, 2);
            if(keys.length == 1) {
                keys = new String[]{"global", key};
            } else if (CraftBookPlugin.inst().getConfiguration().convertNamesToCBID) {
                if(CraftBookPlugin.inst().getUUIDMappings().getUUID(keys[0]) != null) continue;
                OfflinePlayer player = Bukkit.getOfflinePlayer(keys[0]);
                if(player.hasPlayedBefore()) {
                    try {
                        ProfileService resolver = HttpRepositoryService.forMinecraft();
                        Profile profile = resolver.findByName(player.getName()); // May be null

                        UUID uuid = profile.getUniqueId();
                        keys[0] = CraftBookPlugin.inst().getUUIDMappings().getCBID(uuid);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    shouldSave = true;
                }
            }

            String value = String.valueOf(config.getProperty("variables." + key));

            if(RegexUtil.VARIABLE_KEY_PATTERN.matcher(keys[1]).find() && RegexUtil.VARIABLE_VALUE_PATTERN.matcher(value).find()) {
                VariableManager.instance.setVariable(keys[1], keys[0], value);
            }
        }

        if(shouldSave)
            save();
    }

    public void save() {

        config.clear();

        for(Entry<Tuple2<String, String>, String> var : VariableManager.instance.getVariableStore().entrySet()) {

            if(RegexUtil.VARIABLE_KEY_PATTERN.matcher(var.getKey().a).find() && RegexUtil.VARIABLE_VALUE_PATTERN.matcher(var.getValue()).find())
                config.setProperty("variables." + var.getKey().b + '|' + var.getKey().a, var.getValue());
        }
        config.save();
    }
}