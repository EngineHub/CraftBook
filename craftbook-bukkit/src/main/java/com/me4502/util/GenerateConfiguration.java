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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;

import com.sk89q.craftbook.CraftBookMechanic;
import com.sk89q.craftbook.bukkit.BukkitConfiguration;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.developer.ExternalUtilityBase;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;

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
        BukkitConfiguration config = new BukkitConfiguration(new YAMLProcessor(file, true, YAMLFormat.EXTENDED), Bukkit.getLogger());
        config.load();

        file = new File(getGenerationFolder(), "mechanisms.yml");
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

        YAMLProcessor proc = new YAMLProcessor(file, true, YAMLFormat.EXTENDED);

        try {
            proc.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String> mechs = new ArrayList<>(CraftBookPlugin.availableMechanics.keySet());

        Collections.sort(mechs);

        for(String enabled : mechs) {

            Class<? extends CraftBookMechanic> mechClass = CraftBookPlugin.availableMechanics.get(enabled);
            try {
                if(mechClass != null) {

                    CraftBookMechanic mech = mechClass.newInstance();
                    mech.loadConfiguration(proc, "mechanics." + enabled + ".");
                }
            } catch (Throwable t) {
                Bukkit.getLogger().log(Level.WARNING, "Failed to load mechanic: " + enabled, t);
            }
        }

        Bukkit.getLogger().info("Created config files!");

        proc.save();
    }
}