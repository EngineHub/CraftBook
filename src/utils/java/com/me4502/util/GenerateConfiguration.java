package com.me4502.util;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import com.sk89q.craftbook.bukkit.BukkitConfiguration;
import com.sk89q.craftbook.util.developer.ExternalUtilityBase;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;

public class GenerateConfiguration extends ExternalUtilityBase {

    @Override
    public void generate () {

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
        BukkitConfiguration config = new BukkitConfiguration(new YAMLProcessor(file, true, YAMLFormat.EXTENDED), Logger.getLogger(Logger.GLOBAL_LOGGER_NAME));
        config.load();
    }
}