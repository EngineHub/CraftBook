package com.sk89q.craftbook.core;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Me4502
 */
public class LanguageManager {

    private Map<String, YAMLProcessor> languageMap = new HashMap<>();

    public void init() {
        checkForLanguages();
    }

    public void close() {

    }

    private void checkForLanguages() {

        for (String language : CraftBookPlugin.inst().getConfiguration().languages) {
            language = language.trim();
            File f = new File(CraftBookPlugin.inst().getDataFolder(), language + ".yml");
            if(!f.exists())
                try {
                    f.createNewFile();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            YAMLProcessor lang = new YAMLProcessor(f, true, YAMLFormat.EXTENDED);

            try {
                lang.load();
            } catch (Throwable e) {
                CraftBookPlugin.inst().getLogger().severe("An error occured loading the languages file for: " + language + "! This language WILL NOT WORK UNTIL FIXED!");
                e.printStackTrace();
                continue;
            }

            lang.setWriteDefaults(true);

            for(Entry<String, String> s : defaultMessages.entrySet())
                lang.getString(s.getKey(), s.getValue());

            lang.save();

            languageMap.put(language.toLowerCase(), lang);
        }
    }

    public String getString(String message, String language) {

        //message = ChatColor.stripColor(message);
        if(language == null || !languageMap.containsKey(language.toLowerCase()))
            language = CraftBookPlugin.inst().getConfiguration().language;
        YAMLProcessor languageData = languageMap.get(language.toLowerCase());
        String def = defaultMessages.get(message);
        if(languageData == null) {
            if(!CraftBookPlugin.inst().getConfiguration().languageScanText || def != null) {
                return def == null ? message : def;
            } else {
                String trans = message;
                for(Entry<String, String> tran : defaultMessages.entrySet()) {
                    trans = StringUtils.replace(trans, tran.getKey(), tran.getValue());
                }
                return trans;
            }
        } else {
            String translated;
            if(def == null || languageData.getString(message) != null)
                translated = languageData.getString(message);
            else {
                translated = languageData.getString(message, def);
            }

            if(!CraftBookPlugin.inst().getConfiguration().languageScanText || translated != null) {
                if(translated != null)
                    return translated;
                else
                    return def == null ? message : def;
            } else {
                String trans = message;
                for(String tran : languageData.getMap().keySet()) {
                    String trand = defaultMessages.get(tran) != null ? languageData.getString(tran, defaultMessages.get(tran)) : languageData.getString(tran);
                    if(tran == null || trand == null) continue;
                    trans = StringUtils.replace(trans, tran, trand);
                }
                return trans;
            }
        }
    }

    public static String getPlayersLanguage(Player p) {
        return p.getLocale();
    }

    public Set<String> getLanguages() {

        return languageMap.keySet();
    }

    @SuppressWarnings("serial")
    public static final HashMap<String, String> defaultMessages = new HashMap<String, String>(32, 1.0f) {{
        put("area.permissions", "You don't have permissions to do that in this area!");
        put("area.use-permissions", "You don't have permissions to use that in this area!");
        put("area.break-permissions", "You don't have permissions to break that in this area!");


        put("variable.missing", "This variable is missing!");
        put("variable.use-permissions", "You don't have permission to use that variable!");


        put("mech.create-permission", "You don't have permission to create this mechanic.");
        put("mech.use-permission", "You don't have permission to use this mechanic.");
        put("mech.restock-permission", "You don't have permission to restock this mechanic.");
        put("mech.not-enough-blocks","Not enough blocks to trigger mechanic!");
        put("mech.group","You are not in the required group!");
        put("mech.restock","Mechanism Restocked!");

        put("mech.ammeter.ammeter", "Ammeter");

        put("mech.bounceblocks.create", "BounceBlock Created!");
        put("mech.bounceblocks.invalid-velocity", "You need to enter a valid velocity on the 3rd line!");

        put("mech.anchor.create","Chunk Anchor Created!");
        put("mech.anchor.already-anchored","This chunk is already anchored!");

        put("mech.area.create","Toggle Area Created!");
        put("mech.area.missing","The area or namespace does not exist.");

        put("mech.bookcase.fail-line", "Failed to fetch a line from the books file.");
        put("mech.bookcase.fail-file", "Failed to read the books file.");
        put("mech.bookcase.read-line", "You pick up a book...");

        put("mech.bridge.create","Bridge Created!");
        put("mech.bridge.toggle","Bridge Toggled!");
        put("mech.bridge.end-create","Bridge End Created!");
        put("mech.bridge.unusable","Material not usable for a bridge!");
        put("mech.bridge.material","Bridge must be made entirely out of the same material!");
        put("mech.bridge.other-sign","Bridge sign required on other side (or it was too far away).");

        put("mech.cauldron.create","Cauldron Created!");
        put("mech.cauldron.too-small","Cauldron is too small!");
        put("mech.cauldron.leaky","Cauldron has a leak!");
        put("mech.cauldron.no-lava","Cauldron lacks lava!");
        put("mech.cauldron.legacy-not-a-recipe","Hmm, this doesn't make anything...");
        put("mech.cauldron.legacy-not-in-group","Doesn't seem as if you have the ability...");
        put("mech.cauldron.legacy-create","In a poof of smoke, you've made");
        put("mech.cauldron.stir","You stir the cauldron but nothing happens.");
        put("mech.cauldron.permissions","You dont have permission to cook this recipe.");
        put("mech.cauldron.cook", "You have cooked the recipe:");

        put("mech.chairs.sit", "You are now sitting!");
        put("mech.chairs.stand", "You are no longer sitting!");
        put("mech.chairs.in-use", "This chair is in use!");
        put("mech.chairs.floating", "This chair has nothing below it!");
        put("mech.chairs.too-far", "This chair is too far away!");
        put("mech.chairs.obstructed", "This chair is obstructed!");

        put("mech.command.create","Command Sign Created!");

        put("mech.command-items.out-of-sync", "Inventory became out of sync during usage of command-items!");
        put("mech.command-items.wait", "You have to wait %time% seconds to use this again!");
        put("mech.command-items.need", "You need %item% to use this command!");

        put("mech.cook.create","Cooking Pot Created!");
        put("mech.cook.ouch","Ouch! That was hot!");
        put("mech.cook.add-fuel","You put fuel into the cooking pot, and watch as the fire roars!");

        put("mech.custom-crafting.recipe-permission", "You do not have permission to craft this recipe.");

        put("mech.door.create","Door Created!");
        put("mech.door.toggle","Door Toggled!");
        put("mech.door.other-sign","Door sign required on other side (or it was too far away).");
        put("mech.door.unusable","Material not usable for a door!");
        put("mech.door.material","Door must be made entirely out of the same material!");

        put("mech.gate.create","Gate Created!");
        put("mech.gate.toggle","Gate Toggled!");
        put("mech.gate.not-found","Failed to find a gate!");
        put("mech.gate.valid-item","Line 1 needs to be a valid block id.");
        put("mech.dgate.create","Small Gate Created!");

        put("mech.hiddenswitch.key","The key did not fit!");
        put("mech.hiddenswitch.toggle","You hear the muffled click of a switch!");

        put("mech.headdrops.click-message","This is the dismembered head of..");
        put("mech.headdrops.break-permission","You don't have permission to break heads!");

        put("mech.lift.target-sign-created","Elevator target sign created.");
        put("mech.lift.down-sign-created","Elevator down sign created.");
        put("mech.lift.up-sign-created","Elevator up sign created.");
        put("mech.lift.obstruct","Your destination is obstructed!");
        put("mech.lift.no-floor","There is no floor at your destination!");
        put("mech.lift.floor","Floor");
        put("mech.lift.up","You went up a floor!");
        put("mech.lift.down","You went down a floor!");
        put("mech.lift.leave", "You have left the elevator!");
        put("mech.lift.no-destination", "This lift has no destination.");
        put("mech.lift.no-depart", "Cannot depart from this lift (can only arrive).");
        put("mech.lift.busy", "Elevator Busy!");

        put("mech.lightstone.lightstone", "LightStone:");

        put("mech.lightswitch.create","Light Switch Created!");

        put("mech.map.create","Map Changer Created!");
        put("mech.map.invalid","Invalid Map ID!");

        put("mech.painting.editing","You are now editing this painting!");
        put("mech.painting.stop","You are no longer editing this painting!");
        put("mech.painting.used","This painting is already being edited by");
        put("mech.painting.range","You are too far away from the painting!");

        put("mech.pay.create","Pay Created!");
        put("mech.pay.success","Payment Successful! You paid: ");
        put("mech.pay.not-enough-money", "Payment Failed! You don't have enough money.");
        put("mech.pay.failed-to-pay", "Payment Failed! The money failed to be exchanged.");

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
        put("mech.teleport.invalidcoords", "The entered coordinates are invalid!");
        put("mech.teleport.obstruct","Your destination is obstructed!");

        put("mech.xp-storer.create", "XP Storer Created!");
        put("mech.xp-storer.bottle", "You need a bottle to perform this mechanic!");
        put("mech.xp-storer.success", "You package your experience into a bottle!");
        put("mech.xp-storer.not-enough-xp", "You do not have enough experience to fill a bottle!");


        put("circuits.pipes.create","Pipe created!");
        put("circuits.pipes.pipe-not-found", "Failed to find pipe!");


        put("vehicles.create-permission","You don't have permissions to create this vehicle mechanic!");


        put("worldedit.ic.unsupported","WorldEdit selection type currently unsupported for IC's!");
        put("worldedit.ic.notfound","WorldEdit not found!");
        put("worldedit.ic.noselection","No selection was found!");
    }};
}