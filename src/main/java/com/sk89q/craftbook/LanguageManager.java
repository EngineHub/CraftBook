package com.sk89q.craftbook;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import net.minecraft.server.v1_6_R1.LocaleLanguage;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_6_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.RegexUtil;

/**
 * @author Me4502
 */
public class LanguageManager {

    HashMap<String, HashMap<String, String>> languageMap = new HashMap<String, HashMap<String, String>>();

    public LanguageManager() {

        checkForLanguages();
    }

    public void checkForLanguages() {

        List<String> languages = CraftBookPlugin.inst().getConfiguration().languages;
        for (String language : languages) {
            language = language.trim();
            HashMap<String, String> languageData = null;
            File f = new File(CraftBookPlugin.inst().getDataFolder(), language + ".txt");
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
                String line;
                languageData = new HashMap<String, String>();
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.length() == 0 || line.startsWith("#"))
                        continue;
                    String[] split = RegexUtil.COLON_PATTERN.split(line, 2);
                    if (split.length < 2)
                        continue;
                    languageData.put(split[0], split[1]);
                }
            } catch (IOException e) {
                CraftBookPlugin.inst().getLogger().log(Level.SEVERE, "[CraftBook] could not find file: " + CraftBookPlugin.inst().getDataFolder().getName() + File.pathSeparator + language + ".txt");
            } finally {
                if(br != null)
                    try {
                        br.close();
                    } catch (IOException ignored) {
                    }
            }
            languageMap.put(language, languageData);
        }
    }

    public String getString(String message) {

        HashMap<String, String> languageData = languageMap.get(CraftBookPlugin.inst().getConfiguration().language);
        if (languageData == null) return "Missing Language File!";
        String translated = languageData.get(ChatColor.stripColor(message));
        if (translated == null) return message;
        return translated;
    }

    public String getString(String message, String language) {

        if(language == null)
            language = CraftBookPlugin.inst().getConfiguration().language;
        HashMap<String, String> languageData = languageMap.get(language);
        if (languageData == null) return getString(message);
        String translated = languageData.get(ChatColor.stripColor(message));
        if (translated == null || translated.length() == 0) {
            languageData = languageMap.get(CraftBookPlugin.inst().getConfiguration().language);
            if(languageData == null) return message;
            translated = languageData.get(ChatColor.stripColor(message));
            if (translated == null) return message;
            else return translated;
        }
        return translated;
    }

    public String getPlayersLanguage(Player p) {

        try {
            Field d = LocaleLanguage.class.getDeclaredField("e"); //TODO Verify when possible.
            d.setAccessible(true);
            return (String) d.get(((CraftPlayer) p).getHandle().getLocalizedName());
        } catch (Throwable e) {
            return CraftBookPlugin.inst().getConfiguration().language;
        }
    }

    public Set<String> getLanguages() {

        return languageMap.keySet();
    }
}