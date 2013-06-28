package com.me4502.util;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import com.sk89q.craftbook.bukkit.BukkitConfiguration;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;

public class GenerateConfiguration {

    public static void main(String[] args) {

        if(!new File("config.yml").exists()) try {
            new File("config.yml").createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BukkitConfiguration config = new BukkitConfiguration(new YAMLProcessor(new File("config.yml"), true, YAMLFormat.EXTENDED), Logger.getGlobal());
        config.load();
    }
}