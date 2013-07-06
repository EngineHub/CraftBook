package com.sk89q.craftbook;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.minecraft.server.v1_6_R1.LocaleLanguage;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_6_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;

/**
 * @author Me4502
 */
public class LanguageManager {

    HashMap<String, YAMLProcessor> languageMap = new HashMap<String, YAMLProcessor>();

    public LanguageManager() {
    }

    public void init() {
        checkForLanguages();
    }

    public void close() {

        for(YAMLProcessor proc : languageMap.values()) {
            proc.save();
        }
    }

    public void checkForLanguages() {

        List<String> languages = CraftBookPlugin.inst().getConfiguration().languages;
        for (String language : languages) {
            language = language.trim();
            File f = new File(CraftBookPlugin.inst().getDataFolder(), language + ".yml");
            YAMLProcessor lang = new YAMLProcessor(f, true, YAMLFormat.EXTENDED);

            try {
                lang.load();
            } catch (IOException e) {
                e.printStackTrace();
            }

            lang.setWriteDefaults(true);
            languageMap.put(language, lang);
        }
    }

    public String getString(String message, String language) {

        if(language == null)
            language = CraftBookPlugin.inst().getConfiguration().language;
        YAMLProcessor languageData = languageMap.get(language);
        if (languageData == null) {
            languageData = languageMap.get(CraftBookPlugin.inst().getConfiguration().language);
            if (languageData == null) return "Missing Language File!";
            String translated = languageData.getString(ChatColor.stripColor(message), defaultMessages.get(ChatColor.stripColor(message)));
            if (translated == null) return message;
            return translated;
        }
        String translated = languageData.getString(ChatColor.stripColor(message), defaultMessages.get(ChatColor.stripColor(message)));
        if (translated == null || translated.length() == 0) {
            languageData = languageMap.get(CraftBookPlugin.inst().getConfiguration().language);
            if (languageData == null) return "Missing Language File!";
            translated = languageData.getString(ChatColor.stripColor(message), defaultMessages.get(ChatColor.stripColor(message)));
            if (translated == null) return message;
            return translated;
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

    @SuppressWarnings("serial")
    public static HashMap<String, String> defaultMessages = new HashMap<String, String>() {{
        put("area.permissions", "You don't have permissions to use that in this area!");


        put("mech.use-permission", "You don't have permission to use this mechanic.");
        put("mech.restock-permission", "You don't have permission to restock this mechanic.");
        put("mech.not-enough-blocks","Not enough blocks to trigger mechanic!");
        put("mech.group","You are not in the required group!");
        put("mech.restock","Mechanism Restocked!");

        put("mech.ammeter.ammeter", "Ammeter");

        put("mech.anchor.create","Chunk Anchor Created!");
        put("mech.anchor.already-anchored","This chunk is already anchored!");

        put("mech.bookcase.fail-line", "Failed to fetch a line from the books file.");
        put("mech.bookcase.fail-file", "Failed to read the books file.");

        put("mech.bridge.create","Bridge Created!");
        put("mech.bridge.toggle","Bridge Toggled!");
        put("mech.bridge.end-create","Bridge End Created!");
        put("mech.bridge.unusable","Material not usable for a bridge!");
        put("mech.bridge.material","Bridge must be made entirely out of the same material!");
        put("mech.bridge.other-sign","Bridge sign required on other side (or it was too far away).");

        put("mech.cauldron.too-small","Cauldron is too small!");
        put("mech.cauldron.leaky","Cauldron has a leak!");
        put("mech.cauldron.no-lava","Cauldron lacks lava!");
        put("mech.cauldron.legacy-not-a-recipe","Hmm, this doesn't make anything...");
        put("mech.cauldron.legacy-not-in-group","Doesn't seem as if you have the ability...");
        put("mech.cauldron.legacy-create","In a poof of smoke, you've made");
        put("mech.cauldron.stir","You stir the cauldron but nothing happens.");

        put("mech.chairs.sit", "You are now sitting!");
        put("mech.chairs.stand", "You are no longer sitting!");
        put("mech.chairs.in-use", "This chair is in use!");
        put("mech.chairs.floating", "This chair has nothing below it!");

        put("mech.command.create","Command Sign Created!");

        put("mech.cook.create","Cooking Pot Created!");
        put("mech.cook.ouch","Ouch! That was hot!");
        put("mech.cook.add-fuel","You put fuel into the cooking pot, and watch as the fire roars!");

        put("mech.door.create","Door Created!");
        put("mech.door.toggle","Door Toggled!");
        put("mech.door.other-sign","Door sign required on other side (or it was too far away).");
        put("mech.door.unusable","Material not usable for a door!");
        put("mech.door.material","Door must be made entirely out of the same material!");

        put("mech.gate.create","Gate Created!");
        put("mech.gate.toggle","Gate Toggled!");
        put("mech.gate.not-found","Failed to find a gate!");
        put("mech.dgate.create","Small Gate Created!");

        put("mech.hiddenswitch.key","The key did not fit!");
        put("mech.hiddenswitch.toggle","You hear the muffled click of a switch!");

        put("mech.lift.target-sign-created","Elevator target sign created.");
        put("mech.lift.down-sign-created","Elevator down sign created.");
        put("mech.lift.up-sign-created","Elevator up sign created.");
        put("mech.lift.obstruct","Your destination is obstructed!");
        put("mech.lift.no-floor","There is no floor at your destination!");
        put("mech.lift.floor","Floor");
        put("mech.lift.up","You went up a floor!");
        put("mech.lift.down","You went down a floor!");

        put("mech.lightswitch.create","Light Switch Created!");

        put("mech.painting.editing","You are now editing this painting!");
        put("mech.painting.stop","You are no longer editing this painting!");
        put("mech.painting.used","This painting is already being edited by");
        put("mech.painting.range","You are too far away from the painting!");

        put("mech.pay.create","Pay Created!");

        put("mech.pistons.crush.created","Piston Crush Mechanic Created!");
        put("mech.pistons.supersticky.created","Piston Super-Sticky Mechanic Created!");
        put("mech.pistons.bounce.created","Piston Bounce Mechanic Created!");
        put("mech.pistons.superpush.created","Piston Super-Push Mechanic Created!");

        put("mech.signcopy.copy","You have copied the sign!");
        put("mech.signcopy.paste","You have pasted the sign!");

        put("mech.teleport.create","Teleporter Created!");
        put("mech.teleport.alert","You Teleported!");
        put("mech.teleport.range","Out of Range!");
        put("mech.teleport.sign","There is no Sign at your Destination!");
        put("mech.teleport.arriveonly","You can only arrive at this teleporter!");


        put("circuits.pipes.create","Pipe created!");


        put("vehicles.create-permission","You don't have permissions to create this vehicle mechanic!");


        put("worldedit.ic.unsupported","WorldEdit selection type currently unsupported for IC's!");
        put("worldedit.ic.notfound","WorldEdit not found!");
        put("worldedit.ic.noselection","No selection was found!");
    }};
}