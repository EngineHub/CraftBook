package com.sk89q.craftbook.common.variables;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.ParsingUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.Tuple2;
import com.sk89q.craftbook.util.UUIDFetcher;
import com.sk89q.craftbook.util.events.SelfTriggerPingEvent;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;

public class VariableManager extends AbstractCraftBookMechanic {

    private VariableConfiguration variableConfiguration;

    public static VariableManager instance;

    /**
     * Stores the variables used in VariableStore ((Variable, Namespace), Value).
     */
    protected HashMap<Tuple2<String, String>, String> variableStore;

    @Override
    public boolean enable() {

        instance = this;
        variableStore = new HashMap<Tuple2<String, String>, String>();
        CraftBookPlugin.logDebugMessage("Initializing Variables!", "startup.variables");

        try {
            File varFile = new File(CraftBookPlugin.inst().getDataFolder(), "variables.yml");
            if(!varFile.exists())
                varFile.createNewFile();
            variableConfiguration = new VariableConfiguration(new YAMLProcessor(varFile, true, YAMLFormat.EXTENDED), CraftBookPlugin.logger());
            variableConfiguration.load();
        } catch(Exception ignored){
            BukkitUtil.printStacktrace(ignored);
            return false;
        }

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

        return variableStore.containsKey(new Tuple2<String, String>(variable, namespace));
    }

    public String getVariable(String variable, String namespace) {

        return variableStore.get(new Tuple2<String, String>(variable, namespace));
    }

    public String setVariable(String variable, String namespace, String value) {

        return variableStore.put(new Tuple2<String, String>(variable, namespace), value);
    }

    public String removeVariable(String variable, String namespace) {

        return variableStore.remove(new Tuple2<String, String>(variable, namespace));
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
    public String getNamespace(String variable) {

        if(variable.contains("|") && RegexUtil.PIPE_PATTERN.split(variable).length >= 2) {
            String[] bits = RegexUtil.PIPE_PATTERN.split(variable);
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
    public String getVariableName(String variable) {

        if(variable.contains("|") && RegexUtil.PIPE_PATTERN.split(variable).length >= 2) {
            String[] bits = RegexUtil.PIPE_PATTERN.split(variable);
            return bits[1];
        } else {
            return variable;
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {

        if(CraftBookPlugin.inst().getConfiguration().variablesPlayerChatOverride && event.getPlayer().hasPermission("craftbook.variables.chat"))
            event.setMessage(ParsingUtil.parseVariables(event.getMessage(), event.getPlayer()));
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {

        if(CraftBookPlugin.inst().getConfiguration().variablesPlayerCommandOverride && event.getPlayer().hasPermission("craftbook.variables.commands"))
            event.setMessage(ParsingUtil.parseVariables(event.getMessage(), event.getPlayer()));
    }

    @EventHandler
    public void onConsoleCommandPreprocess(ServerCommandEvent event) {

        if(CraftBookPlugin.inst().getConfiguration().variablesCommandBlockOverride)
            event.setCommand(ParsingUtil.parseVariables(event.getCommand(), null));
    }

    @EventHandler
    public void onSelfTriggerPing(SelfTriggerPingEvent event) {

        if(!CraftBookPlugin.inst().getConfiguration().convertNamesToCBID) return;
        if(SignUtil.isSign(event.getBlock())) {

            ChangedSign sign = BukkitUtil.toChangedSign(event.getBlock());

            int i = 0;

            for(String line : sign.getLines()) {
                for(String var : ParsingUtil.getPossibleVariables(line)) {
                    String namespace = getNamespace(var);
                    if(namespace == null || namespace.isEmpty() || namespace.equals("global")) continue;
                    if(CraftBookPlugin.inst().getUUIDMappings().getUUID(namespace) != null) continue;
                    OfflinePlayer player = Bukkit.getOfflinePlayer(namespace);
                    if(player.hasPlayedBefore()) {
                        UUIDFetcher fetcher = new UUIDFetcher(Arrays.asList(namespace));
                        try {
                            UUID uuid = fetcher.call().get(player.getName());
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
}