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
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.core.mechanic.MechanicType;
import com.sk89q.craftbook.util.developer.ExternalUtilityBase;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.logging.Level;

public class GenerateWikiConfigLists extends ExternalUtilityBase {

    public GenerateWikiConfigLists (String[] args) {
        super(args);
    }

    public static void createConfigSectionFile(File folder, YAMLProcessor config, String path) throws IOException {
        File file = new File(folder, (path == null ? "root" : path) + ".txt");
        if(!file.exists()) {
            new File(file.getParent()).mkdirs();
        } else {
            file.delete();
        }
        file.createNewFile();
        PrintWriter pw = new PrintWriter(file, "UTF-8");

        pw.print("Configuration\n");
        pw.append("=============\n\n");

        int nodeLength = "Node".length(), commentLength = "Comment".length(), defaultLength = "Default".length();

        for(String key : config.getKeys(null)) {
            Object property = config.getProperty(key);
            if(property != null && !(property instanceof Map)) {
                if (nodeLength < key.length()) {
                    nodeLength = key.length();
                }
                String def = String.valueOf(property);
                if (defaultLength < def.length()) {
                    defaultLength = def.length();
                }
                String comment = config.getComment(key);
                if(comment == null) {
                    comment = "";
                }
                if(!comment.trim().isEmpty()) comment = comment.trim().substring(2);
                if (commentLength < comment.length()) {
                    commentLength = comment.length();
                }
            }
        }

        String border = createStringOfLength(nodeLength, '=') + ' ' + createStringOfLength(commentLength, '=') + ' ' + createStringOfLength(defaultLength, '=');

        pw.println(border);
        pw.print(padToLength("Node", nodeLength + 1));
        pw.print(padToLength("Comment", commentLength + 1));
        pw.println(padToLength("Default", defaultLength + 1));;
        pw.println(border);

        for(String key : config.getKeys(null)) {
            String comment = config.getComment(key);
            if(comment == null) {
                System.out.println("[WARNING] Key " + key + " is missing a comment!");
                comment = "";
                missingComments ++;
            }
            if(!comment.trim().isEmpty()) comment = comment.trim().substring(2);

            pw.print(padToLength(key, nodeLength + 1));
            pw.print(padToLength(comment, commentLength + 1));
            pw.println(padToLength(String.valueOf(config.getProperty(key)), defaultLength + 1));
        }
        pw.println(border);

        pw.close();
    }

    static int missingComments = 0;

    public static String createStringOfLength(int length, char character) {
        StringBuilder ret = new StringBuilder();

        for(int i = 0; i < length; i++)
            ret.append(character);

        return ret.toString();
    }

    public static String padToLength(String input, int length) {
        StringBuilder builder = new StringBuilder(input);
        while(builder.length() < length)
            builder.append(' ');
        return builder.toString();
    }

    @Override
    public void generate(String[] args) {

        File configFolder = new File(getGenerationFolder(), "configs/");
        configFolder.mkdir();

        missingComments = 0;
        try {
            createConfigSectionFile(configFolder, CraftBookPlugin.inst().getConfiguration().config, null);

            MechanicType.REGISTRY.values().forEach(mechanicRegistration -> {
                try {
                    CraftBookMechanic me = mechanicRegistration.create();
                    File file = new File(new File(CraftBookPlugin.inst().getDataFolder(), "mechanics"), mechanicRegistration.getName() + ".yml");

                    YAMLProcessor mechanicConfig = new YAMLProcessor(file, true, YAMLFormat.EXTENDED);

                    try {
                        mechanicConfig.load();
                    } catch (FileNotFoundException e) {
                        // Ignore this one.
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    me.loadFromConfiguration(mechanicConfig);

                    createConfigSectionFile(configFolder, mechanicConfig, mechanicRegistration.getName());
                } catch (Throwable t) {
                    Bukkit.getLogger().log(Level.WARNING, "Failed to load mechanic: " + mechanicRegistration.getName(), t);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(missingComments + " Comments Are Missing");
    }
}