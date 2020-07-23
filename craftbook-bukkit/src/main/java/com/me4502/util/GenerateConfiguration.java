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

package com.me4502.util;

import com.sk89q.craftbook.CraftBookMechanic;
import com.sk89q.craftbook.bukkit.BukkitConfiguration;
import com.sk89q.craftbook.core.mechanic.MechanicType;
import com.sk89q.craftbook.util.developer.ExternalUtilityBase;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class GenerateConfiguration extends ExternalUtilityBase {

    public GenerateConfiguration (String[] args) {
        super(args);
    }

    @Override
    public void generate(String[] args) {

        File file = new File(getGenerationFolder(), "config.yml");
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            file.delete();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        BukkitConfiguration config = new BukkitConfiguration(new YAMLProcessor(file, true, YAMLFormat.EXTENDED));
        config.load();

        File mechanicsFolder = new File(getGenerationFolder(), "mechanics");
        if (mechanicsFolder.exists()) {
            mechanicsFolder.delete();
        }
        mechanicsFolder.mkdirs();

        List<String> mechs = new ArrayList<>(MechanicType.REGISTRY.keySet());

        Collections.sort(mechs);

        for(String enabled : mechs) {
            MechanicType<?> mechanicType = MechanicType.REGISTRY.get(enabled);
            try {
                if(mechanicType != null) {
                    CraftBookMechanic mech = mechanicType.getMechanicClass().getDeclaredConstructor().newInstance();
                    mech.loadConfiguration(new File(mechanicsFolder, mechanicType.getName() + ".yml"));
                }
            } catch (Throwable t) {
                Bukkit.getLogger().log(Level.WARNING, "Failed to load mechanic: " + enabled, t);
            }
        }

        Bukkit.getLogger().info("Created config files!");
    }
}