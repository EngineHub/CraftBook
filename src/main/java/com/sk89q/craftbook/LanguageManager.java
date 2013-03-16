package com.sk89q.craftbook;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import net.minecraft.server.v1_5_R1.LocaleLanguage;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_5_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.RegexUtil;

/**
 * @author Me4502
 */
public class LanguageManager {

    final CraftBookPlugin plugin = CraftBookPlugin.inst();

    HashMap<String, HashMap<String, String>> languageMap = new HashMap<String, HashMap<String, String>>();

    public LanguageManager() {

        checkForLanguages();
    }

    public void checkForLanguages() {

        List<String> languages = plugin.getConfiguration().languages;
        for (String language : languages) {
            language = language.trim();
            HashMap<String, String> languageData = new HashMap<String, String>();
            File f = new File(plugin.getDataFolder(), language + ".txt");
            try {
                BufferedReader br = new BufferedReader(new FileReader(f));
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().length() == 0 || line.trim().startsWith("#"))
                        continue;
                    String[] split = RegexUtil.COLON_PATTERN.split(line);
                    if (split.length != 2) {
                        continue;
                    }
                    languageData.put(split[0], split[1]);
                }
                br.close();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE,
                        "[CraftBook] could not find file: " + plugin.getDataFolder().getName() + File.pathSeparator +
                        language + ".txt");
            }
            languageMap.put(language, languageData);
        }
    }

    @Deprecated
    public String getString(String message) {

        HashMap<String, String> languageData = languageMap.get(plugin.getConfiguration().language);
        if (languageData == null) return "Missing Language File!";
        String translated = languageData.get(ChatColor.stripColor(message));
        if (translated == null) return message;
        return translated;
    }

    public String getString(String message, String language) {

        if(language == null)
            language = plugin.getConfiguration().language;
        HashMap<String, String> languageData = languageMap.get(language);
        if (languageData == null) return getString(message);
        String translated = languageData.get(ChatColor.stripColor(message));
        if (translated == null || translated.length() == 0) {
            languageData = languageMap.get(plugin.getConfiguration().language);
            translated = languageData.get(ChatColor.stripColor(message));
            if (translated == null) return message;
            else return translated;
        }
        return translated;
    }

    public String getPlayersLanguage(Player p) {

        try {
            Field d = LocaleLanguage.class.getDeclaredField("d");
            d.setAccessible(true);
            return (String) d.get(((CraftPlayer) p).getHandle().getLocale());
        } catch (Throwable e) {
            return plugin.getConfiguration().language;
        }
    }

    public Set<String> getLanguages() {

        return languageMap.keySet();
    }
}