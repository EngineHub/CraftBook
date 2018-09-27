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