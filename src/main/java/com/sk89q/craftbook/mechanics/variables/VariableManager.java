package com.sk89q.craftbook.mechanics.variables;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.util.*;
import com.sk89q.craftbook.util.events.SelfTriggerPingEvent;
import com.sk89q.squirrelid.Profile;
import com.sk89q.squirrelid.resolver.HttpRepositoryService;
import com.sk89q.squirrelid.resolver.ProfileService;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class VariableManager extends AbstractCraftBookMechanic {

    private VariableConfiguration variableConfiguration;

    public static VariableManager instance;

    /**
     * Stores the variables used in VariableStore ((Variable, Namespace), Value).
     */
    private HashMap<Tuple2<String, String>, String> variableStore;

    @Override
    public boolean enable() {

        instance = this;
        variableStore = new HashMap<>();
        CraftBookPlugin.logDebugMessage("Initializing Variables!", "startup.variables");

        try {
            File varFile = new File(CraftBookPlugin.inst().getDataFolder(), "variables.yml");
            if(!varFile.exists())
                varFile.createNewFile();
            variableConfiguration = new VariableConfiguration(new YAMLProcessor(varFile, true, YAMLFormat.EXTENDED), CraftBookPlugin.logger());
            variableConfiguration.load();
        } catch(Exception ignored){
            CraftBookBukkitUtil.printStacktrace(ignored);
            return false;
        }

        if(packetMessageOverride)
            new VariablePacketModifier();

        return true;
    }

    @Override
    public void disable() {

        if(variableConfiguration != null) {
            variableConfiguration.save();
            variableConfiguration = null;
        }
        variableStore.clear();
        instance = null;
    }

    public boolean hasVariable(String variable, String namespace) {

        return variableStore.containsKey(new Tuple2<>(variable, namespace));
    }

    public String getVariable(String variable, String namespace) {

        return variableStore.get(new Tuple2<>(variable, namespace));
    }

    public String setVariable(String variable, String namespace, String value) {

        return variableStore.put(new Tuple2<>(variable, namespace), value);
    }

    public String removeVariable(String variable, String namespace) {

        return variableStore.remove(new Tuple2<>(variable, namespace));
    }

    public HashMap<Tuple2<String, String>, String> getVariableStore() {

        return variableStore;
    }

    /**
     * Grabs the namespace off a variable. Returns global if none.
     * 
     * @param variable The variable
     * @return The namespace or global.
     */
    public static String getNamespace(String variable) {

        if(variable.contains("|")) {
            String[] bits = RegexUtil.PIPE_PATTERN.split(variable);
            if(bits.length < 2) return "global";
            return bits[0];
        } else {
            return "global";
        }
    }

    /**
     * Grabs the variable name off a variable.
     * 
     * @param variable The variable
     * @return The name.
     */
    public static String getVariableName(String variable) {

        if(variable.contains("|")) {
            String[] bits = RegexUtil.PIPE_PATTERN.split(variable);
            if(bits.length < 2) return variable;
            return bits[1];
        } else {
            return variable;
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {

        if(playerChatOverride && event.getPlayer().hasPermission("craftbook.variables.chat"))
            event.setMessage(ParsingUtil.parseVariables(event.getMessage(), event.getPlayer()));
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {

        if(playerCommandOverride && event.getPlayer().hasPermission("craftbook.variables.commands"))
            event.setMessage(ParsingUtil.parseVariables(event.getMessage(), event.getPlayer()));
    }

    @EventHandler
    public void onConsoleCommandPreprocess(ServerCommandEvent event) {

        if(consoleOverride)
            event.setCommand(ParsingUtil.parseVariables(event.getCommand(), null));
    }

    @EventHandler
    public void onSelfTriggerPing(SelfTriggerPingEvent event) {

        if(!CraftBookPlugin.inst().getConfiguration().convertNamesToCBID) return;
        if(SignUtil.isSign(event.getBlock())) {

            ChangedSign sign = CraftBookBukkitUtil.toChangedSign(event.getBlock());

            int i = 0;

            for(String line : sign.getLines()) {
                for(String var : ParsingUtil.getPossibleVariables(line)) {
                    String namespace = getNamespace(var);
                    if(namespace == null || namespace.isEmpty() || namespace.equals("global")) continue;
                    if(CraftBookPlugin.inst().getUUIDMappings().getUUID(namespace) != null) continue;
                    OfflinePlayer player = Bukkit.getOfflinePlayer(namespace);
                    if(player.hasPlayedBefore()) {
                        try {
                            ProfileService resolver = HttpRepositoryService.forMinecraft();
                            Profile profile = resolver.findByName(player.getName()); // May be null

                            UUID uuid = profile.getUniqueId();
                            line = StringUtils.replace(line, var, var.replace(namespace, CraftBookPlugin.inst().getUUIDMappings().getCBID(uuid)));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                sign.setLine(i++, line);
            }

            sign.update(false);
        }
    }

    boolean defaultToGlobal;
    private boolean consoleOverride;
    private boolean playerCommandOverride;
    private boolean playerChatOverride;
    private boolean packetMessageOverride;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "default-to-global", "When a variable is accessed via command, if no namespace is provided... It will default to global. If this is false, it will use the players name.");
        defaultToGlobal = config.getBoolean(path + "default-to-global", false);

        config.setComment(path + "enable-in-console", "Allows variables to work on the Console.");
        consoleOverride = config.getBoolean(path + "enable-in-console", false);

        config.setComment(path + "enable-in-player-commands", "Allows variables to work in any command a player performs.");
        playerCommandOverride = config.getBoolean(path + "enable-in-player-commands", false);

        config.setComment(path + "enable-in-player-chat", "Allow variables to work in player chat.");
        playerChatOverride = config.getBoolean(path + "enable-in-player-chat", false);

        config.setComment(path + "override-all-text", "Modify outgoing packets to replace variables in all text. (Requires ProtocolLib)");
        packetMessageOverride = config.getBoolean(path + "override-all-text", false);
    }

    @Override
    public LoadPriority getLoadPriority() {

        return LoadPriority.EARLY;
    }
}