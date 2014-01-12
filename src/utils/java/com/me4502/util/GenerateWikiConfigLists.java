package com.me4502.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.logging.Logger;

import com.sk89q.craftbook.bukkit.BukkitConfiguration;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;

public class GenerateWikiConfigLists {

    public static void main(String[] args) {

        if(!new File("config.yml").exists()) {
            try {
                new File("config.yml").createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            new File("config.yml").delete();
            try {
                new File("config.yml").createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        BukkitConfiguration config = new BukkitConfiguration(new YAMLProcessor(new File("config.yml"), true, YAMLFormat.EXTENDED), Logger.getLogger(Logger.GLOBAL_LOGGER_NAME));
        config.load();

        File configFolder = new File("configs/");
        configFolder.mkdir();

        try {
            createConfigSectionFile(configFolder, config.config, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createConfigSectionFile(File folder, YAMLProcessor config, String path) throws IOException {

        String fpath = path;
        if(fpath == null) fpath = "root";
        else if(fpath.contains("."))
            fpath = RegexUtil.PERIOD_PATTERN.split(fpath)[RegexUtil.PERIOD_PATTERN.split(fpath).length-1];
        File file = new File(folder, fpath + ".txt");
        if(!file.exists()) {
            new File(file.getParent()).mkdirs();
            file.createNewFile();
        } else {
            file.delete();
            file.createNewFile();
        }
        PrintWriter pw = new PrintWriter(file);
        int lines = 0;

        pw.println("== Configuration ==");
        pw.println();
        pw.println("{| class=\"wiki-table sortable\"");
        pw.println("|-");
        pw.println("! Configuration Node and Path");
        pw.println("! Default Value");
        pw.println("! Effect");

        for(String key : config.getKeys(path)) {
            if(config.getProperty(path == null ? key : path + "." + key) != null && !(config.getProperty(path == null ? key : path + "." + key) instanceof Map)) {
                pw.println("|-");
                pw.println("| " + (path == null ? key : path + "." + key));
                pw.println("| " + String.valueOf(config.getProperty(path == null ? key : path + "." + key)));
                String comment = config.getComment(path == null ? key : path + "." + key);
                if(comment == null) {
                    System.out.println("[WARNING] Key " + path == null ? key : path + "." + key + " is missing a comment!");
                    comment = "";
                }
                if(!comment.trim().isEmpty()) comment = comment.trim().substring(2);
                pw.println("| " + comment);
                lines++;
            } else
                createConfigSectionFile(new File(folder, key + "/"), config, path == null ? key : path + "." + key);
        }

        pw.println("|}");

        pw.close();

        if(lines == 0)
            file.delete();
        folder.delete(); //Delete if empty.
    }
}