package com.me4502.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.developer.ExternalUtilityBase;
import com.sk89q.util.yaml.YAMLProcessor;

public class GenerateWikiConfigLists extends ExternalUtilityBase {

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
        PrintWriter pw = new PrintWriter(file, "UTF-8");
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
                    missingComments++;
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

    static int missingComments = 0;

    @Override
    public void generate () {

        File configFolder = new File("configs/");
        configFolder.mkdir();

        missingComments = 0;
        try {
            createConfigSectionFile(configFolder, CraftBookPlugin.inst().getConfiguration().config, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(missingComments + " Comments Are Missing");
    }
}