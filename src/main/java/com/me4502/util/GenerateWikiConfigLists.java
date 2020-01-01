package com.me4502.util;

import com.sk89q.craftbook.CraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.developer.ExternalUtilityBase;
import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.logging.Level;

public class GenerateWikiConfigLists extends ExternalUtilityBase {

    public GenerateWikiConfigLists (String[] args) {
        super(args);
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
        if (config.getKeys(path) == null) {
            return;
        }

        PrintWriter pw = new PrintWriter(file, "UTF-8");

        pw.print("Configuration\n");
        pw.append("=============\n\n");

        int nodeLength = "Node".length(), commentLength = "Comment".length(), defaultLength = "Default".length();

        for(String key : config.getKeys(path)) {
            if(config.getProperty(path == null ? key : path + "." + key) != null && !(config.getProperty(path == null ? key : path + "." + key) instanceof Map)) {
                String node = (path == null ? key : path + "." + key);
                if (nodeLength < node.length()) {
                    nodeLength = node.length();
                }
                String def = String.valueOf(config.getProperty(path == null ? key : path + "." + key));
                if (defaultLength < def.length()) {
                    defaultLength = def.length();
                }
                String comment = config.getComment(path == null ? key : path + "." + key);
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

        for(String key : config.getKeys(path)) {
            if(config.getProperty(path == null ? key : path + "." + key) != null && !(config.getProperty(path == null ? key : path + "." + key) instanceof Map)) {
                String comment = config.getComment(path == null ? key : path + "." + key);
                if(comment == null) {
                    System.out.println("[WARNING] Key " + path == null ? key : path + "." + key + " is missing a comment!");
                    comment = "";
                    missingComments ++;
                }
                if(!comment.trim().isEmpty()) comment = comment.trim().substring(2);

                pw.print(padToLength((path == null ? key : path + "." + key), nodeLength + 1));
                pw.print(padToLength(comment, commentLength + 1));
                pw.println(padToLength(String.valueOf(config.getProperty(path == null ? key : path + "." + key)), defaultLength + 1));
            } else
                createConfigSectionFile(new File(folder, key + "/"), config, path == null ? key : path + "." + key);
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

            CraftBookPlugin.availableMechanics.forEach((mech, aClass) -> {
                try {
                    try {
                        if (aClass != null) {

                            CraftBookMechanic me = aClass.newInstance();
                            me.loadConfiguration(CraftBookPlugin.inst().getMechanismsConfig(), "mechanics." + mech + ".");
                        }
                    } catch (Throwable t) {
                        Bukkit.getLogger().log(Level.WARNING, "Failed to load mechanic: " + mech, t);
                    }

                    createConfigSectionFile(configFolder, CraftBookPlugin.inst().getMechanismsConfig(), "mechanics." + mech);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(missingComments + " Comments Are Missing");
    }
}