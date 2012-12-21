package com.sk89q.craftbook;

import com.sk89q.craftbook.bukkit.BaseBukkitPlugin;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import net.minecraft.server.v1_4_6.LocaleLanguage;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_4_6.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * @author Me4502
 */
public class LanguageManager {

    private static final Pattern COLON_PATTERN = Pattern.compile(":", Pattern.LITERAL);
    final BaseBukkitPlugin plugin;

    HashMap<String, HashMap<String, String>> languageMap = new HashMap<String, HashMap<String, String>>();

    public LanguageManager(BaseBukkitPlugin plugin) {

        this.plugin = plugin;
        checkForLanguages();
    }

    public void checkForLanguages() {

        List<String> languages = CraftBookPlugin.getInstance().getLocalConfiguration().languages;
        for (String language : languages) {
            language = language.trim();
            HashMap<String, String> languageData = new HashMap<String, String>();
            File f = new File(plugin.getDataFolder(), language + ".txt");
            try {
                BufferedReader br = new BufferedReader(new FileReader(f));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] split = COLON_PATTERN.split(line);
                    if (split.length != 2) {
                        continue;
                    }
                    languageData.put(split[0], split[1]);
                }
                br.close();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "[CraftBook] could not find file: " + plugin.getDataFolder()
                        .getName() +
                        File.pathSeparator + language + ".txt");
            }
            languageMap.put(language, languageData);
        }
    }

    @Deprecated
    public String getString(String message) {

        HashMap<String, String> languageData = languageMap.get(CraftBookPlugin.getInstance().getLocalConfiguration().language);
        if (languageData == null)
            return "Missing Language File!";
        String translated = languageData.get(ChatColor.stripColor(message));
        if (translated == null) return message;
        return translated;
    }

    public String getString(String message, String language) {

        HashMap<String, String> languageData = languageMap.get(language);
        if (languageData == null)
            return getString(message);
        String translated = languageData.get(ChatColor.stripColor(message));
        if (translated == null) {
            languageData = languageMap.get(CraftBookPlugin.getInstance().getLocalConfiguration().language);
            translated = languageData.get(ChatColor.stripColor(message));
            if(translated == null)
                return message;
            else
                return translated;
        }
        return translated;
    }

    public String getPlayersLanguage(Player p) {

        try {
            Field d = LocaleLanguage.class.getDeclaredField("d");
            d.setAccessible(true);
            return (String) d.get(((CraftPlayer) p).getHandle().getLocale());
        } catch (Throwable e) {
            return CraftBookPlugin.getInstance().getLocalConfiguration().language;
        }
    }

    public Set<String> getLanguages() {

        return languageMap.keySet();
    }
}