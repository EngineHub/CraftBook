package com.sk89q.craftbook.mech;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.bukkit.util.SuperUser;
import com.sk89q.craftbook.mech.CommandItems.CommandItemDefinition.CommandType;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.Tuple2;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;

public class CommandItems implements Listener {

    private YAMLProcessor config;

    private HashSet<CommandItemDefinition> definitions = new HashSet<CommandItemDefinition>();

    private HashMap<Tuple2<String, String>, Integer> cooldownPeriods = new HashMap<Tuple2<String, String>, Integer>();

    public CommandItems() {

        CraftBookPlugin.inst().createDefaultConfiguration(new File(CraftBookPlugin.inst().getDataFolder(), "command-items.yml"), "command-items.yml", false);
        config = new YAMLProcessor(new File(CraftBookPlugin.inst().getDataFolder(), "command-items.yml"), false, YAMLFormat.EXTENDED);
        load();
        if(definitions.size() > 0)
            Bukkit.getScheduler().runTaskTimer(CraftBookPlugin.inst(), new Runnable() {

                @Override
                public void run () {

                    Iterator<Entry<Tuple2<String, String>, Integer>> iterator = cooldownPeriods.entrySet().iterator();

                    while(iterator.hasNext()) {

                        Entry<Tuple2<String, String>, Integer> entry = iterator.next();
                        if(entry.getValue() > 1)
                            cooldownPeriods.put(entry.getKey(), entry.getValue() - 1);
                        else
                            iterator.remove();
                    }
                }
            }, 1, 20);
    }

    public void load() {

        try {
            config.load();
        } catch (IOException e) {
            BukkitUtil.printStacktrace(e);
            return;
        }

        for(String key : config.getKeys("command-items")) {

            CommandItemDefinition comdef = CommandItemDefinition.readDefinition(config, "command-items." + key);
            if(comdef == null) {
                CraftBookPlugin.logger().warning("Failed to add CommandItem: " + key);
                continue;
            }
            if(definitions.add(comdef))
                CraftBookPlugin.logger().info("Added CommandItem: " + key);
            else
                CraftBookPlugin.logger().warning("Failed to add CommandItem: " + key);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerInteract(final PlayerInteractEvent event) {

        if(event.getPlayer().getItemInHand() == null)
            return;

        CommandItemDefinition comdeft = null;
        for(CommandItemDefinition def : definitions) {
            if(ItemUtil.areItemsIdentical(def.stack, event.getPlayer().getItemInHand())) {
                comdeft = def;
                break;
            }
        }
        if(comdeft == null)
            return;

        final CommandItemDefinition comdef = comdeft;

        if(!event.getPlayer().hasPermission("craftbook.mech.commanditems") || comdef.permNode != null && !comdef.permNode.isEmpty() && !event.getPlayer().hasPermission(comdef.permNode)) {
            event.getPlayer().sendMessage(ChatColor.RED + "You don't have permissions to use this mechanic!");
            return;
        }

        if(cooldownPeriods.containsKey(new Tuple2<String, String>(event.getPlayer().getName(), comdef.name))) {
            event.getPlayer().sendMessage(ChatColor.RED + "You have to wait " + cooldownPeriods.get(new Tuple2<String, String>(event.getPlayer().getName(), comdef.name)) + " seconds to use this again!");
            return;
        }

        for(String command : comdef.commands) {

            command = command.replace("@p", event.getPlayer().getName());
            command = CraftBookPlugin.inst().parseVariables(command);
            if(comdef.type == CommandType.CONSOLE)
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            else if (comdef.type == CommandType.PLAYER)
                Bukkit.dispatchCommand(event.getPlayer(), command);
            else  if (comdef.type == CommandType.SUPERUSER)
                Bukkit.dispatchCommand(new SuperUser(event.getPlayer()), command);
        }

        if(comdef.cooldown > 0 && !event.getPlayer().hasPermission("craftbook.mech.commanditems.bypasscooldown"))
            cooldownPeriods.put(new Tuple2<String, String>(event.getPlayer().getName(), comdef.name), comdef.cooldown);

        if(comdef.delayedCommands.length > 0)
            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new Runnable() {

                @Override
                public void run () {
                    for(String command : comdef.delayedCommands) {

                        command = command.replace("@p", event.getPlayer().getName());
                        command = CraftBookPlugin.inst().parseVariables(command);
                        if(comdef.type == CommandType.CONSOLE)
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                        else if (comdef.type == CommandType.PLAYER)
                            Bukkit.dispatchCommand(event.getPlayer(), command);
                        else  if (comdef.type == CommandType.SUPERUSER)
                            Bukkit.dispatchCommand(new SuperUser(event.getPlayer()), command);
                    }
                }
            }, comdef.delay);

        event.setCancelled(true);
    }

    public static class CommandItemDefinition {

        private String name;
        private ItemStack stack;
        private String permNode;
        private CommandType type;

        private String[] commands;

        private String[] delayedCommands;
        private int delay;
        private int cooldown;

        private CommandItemDefinition(String name, ItemStack stack, CommandType type, String permNode, String[] commands, int delay, String[] delayedCommands, int cooldown) {

            this.name = name;
            this.stack = stack;
            this.type = type;
            this.permNode = permNode;
            this.commands = commands;
            this.delay = delay;
            this.delayedCommands = delayedCommands;
            this.cooldown = cooldown;
        }

        public static CommandItemDefinition readDefinition(YAMLProcessor config, String path) {

            String name = RegexUtil.PERIOD_PATTERN.split(path)[1];
            ItemStack stack = ItemUtil.getItem(config.getString(path + ".item"));
            List<String> commands = config.getStringList(path + ".commands", new ArrayList<String>());
            String permNode = config.getString(path + ".permission-node");
            CommandType type = CommandType.valueOf(config.getString(path + ".run-as").toUpperCase());
            int delay = config.getInt(path + ".delay");
            List<String> delayedCommands = new ArrayList<String>();
            if(delay > 0)
                delayedCommands = config.getStringList(path + ".delayed-commands", new ArrayList<String>());
            int cooldown = config.getInt(path + ".cooldown");

            return new CommandItemDefinition(name, stack, type, permNode, commands.toArray(new String[commands.size()]), delay, delayedCommands.toArray(new String[delayedCommands.size()]), cooldown);
        }

        public enum CommandType {

            PLAYER,CONSOLE,SUPERUSER;
        }
    }
}