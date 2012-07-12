package com.sk89q.craftbook;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.ChatColor;

import com.sk89q.craftbook.bukkit.BaseBukkitPlugin;

public class LanguageManager {

    BaseBukkitPlugin plugin;

    HashMap<String, String> languageData = new HashMap<String, String>();

    public LanguageManager(BaseBukkitPlugin plugin) {
        this.plugin = plugin;
        checkForLanguages();
    }

    public void checkForLanguages() {
        String language = plugin.getLocalConfiguration().commonSettings.language;
        File f = new File(plugin.getDataFolder(), language + ".txt");
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            while((line = br.readLine())!=null) {
                if(line.split(":").length != 2) continue;
                languageData.put(line.split(":")[0], line.split(":")[1]);
            }
            br.close();
        }
        catch(Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[CraftBook] could not find file: " + plugin.getDataFolder().getName() + File.pathSeparator + language + ".txt");
        }
    }

    public String getString(String s) {
        if(languageData.get(ChatColor.stripColor(s)) == null) return s;
        return languageData.get(ChatColor.stripColor(s));
    }
}
