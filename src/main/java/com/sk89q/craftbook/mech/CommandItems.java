package com.sk89q.craftbook.mech;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.mech.CommandItems.CommandItemDefinition.ClickType;
import com.sk89q.craftbook.mech.CommandItems.CommandItemDefinition.CommandType;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.ParsingUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.TernaryState;
import com.sk89q.craftbook.util.Tuple2;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;

public class CommandItems implements Listener {

    public static CommandItems INSTANCE;

    private YAMLProcessor config;

    private HashSet<CommandItemDefinition> definitions = new HashSet<CommandItemDefinition>();

    private HashMap<Tuple2<String, String>, Integer> cooldownPeriods = new HashMap<Tuple2<String, String>, Integer>();

    public CommandItemDefinition getDefinitionByName(String name) {

        for(CommandItemDefinition def : definitions)
            if(def.name.equalsIgnoreCase(name))
                return def;

        return null;
    }

    public CommandItems() {

        INSTANCE = this;
        CraftBookPlugin.inst().createDefaultConfiguration(new File(CraftBookPlugin.inst().getDataFolder(), "command-items.yml"), "command-items.yml");
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

        int amount = 0;

        for(String key : config.getKeys("command-items")) {

            CommandItemDefinition comdef = CommandItemDefinition.readDefinition(config, "command-items." + key);
            if(definitions.add(comdef)) {
                CraftBookPlugin.logDebugMessage("Added CommandItem: " + key, "command-items.initialize");
                amount++;
            } else
                CraftBookPlugin.logger().warning("Failed to add CommandItem: " + key);
        }

        CraftBookPlugin.logger().info("Successfully added " + amount + " CommandItems!");
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerInteract(final PlayerInteractEvent event) {

        if(event.getItem() == null)
            return;

        if(event.getAction() == Action.PHYSICAL)
            return;

        performCommandItems(event.getItem(), event.getPlayer(), event);
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerHitEntity(final PlayerInteractEntityEvent event) {

        if(event.getPlayer().getItemInHand() == null)
            return;

        performCommandItems(event.getPlayer().getItemInHand(), event.getPlayer(), event);
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onEntityDamageEntity(final EntityDamageByEntityEvent event) {

        Player p = null;
        if(event.getDamager() instanceof Projectile) {
            if(!(((Projectile) event.getDamager()).getShooter() instanceof Player))
                return;
            p = (Player) ((Projectile) event.getDamager()).getShooter();
        } else {
            if(!(event.getDamager() instanceof Player))
                return;
            p = (Player) event.getDamager();
        }

        if(p.getItemInHand() == null)
            return;

        performCommandItems(p.getItemInHand(), p, event);
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onBlockBreak(final BlockBreakEvent event) {

        if(event.getPlayer().getItemInHand() == null)
            return;

        performCommandItems(event.getPlayer().getItemInHand(), event.getPlayer(), event);
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onBlockPlace(final BlockPlaceEvent event) {

        if(event.getPlayer().getItemInHand() == null)
            return;

        performCommandItems(event.getPlayer().getItemInHand(), event.getPlayer(), event);
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onItemConsume(final PlayerItemConsumeEvent event) {

        if(event.getItem() == null)
            return;

        performCommandItems(event.getItem(), event.getPlayer(), event);
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onItemDrop(final PlayerDropItemEvent event) {

        if(event.getItemDrop() == null || !ItemUtil.isStackValid(event.getItemDrop().getItemStack()))
            return;

        performCommandItems(event.getItemDrop().getItemStack(), event.getPlayer(), event);
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onItemBreak(final PlayerItemBreakEvent event) {

        if(event.getBrokenItem() == null)
            return;

        performCommandItems(event.getBrokenItem(), event.getPlayer(), event);
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onItemPickup(final PlayerPickupItemEvent event) {

        if(event.getItem() == null)
            return;

        performCommandItems(event.getItem().getItemStack(), event.getPlayer(), event);
    }

    @SuppressWarnings("deprecation")
    public void performCommandItems(ItemStack item, final Player player, final Event event) {

        for(CommandItemDefinition def : definitions) {
            current: {
            if(ItemUtil.areItemsIdentical(def.stack, item)) {
                final CommandItemDefinition comdef = def;

                if(comdef.clickType != ClickType.ANY) {
                    if(comdef.clickType == ClickType.CLICK_RIGHT || comdef.clickType == ClickType.CLICK_LEFT || comdef.clickType == ClickType.CLICK_EITHER) {

                        if(!(event instanceof PlayerInteractEvent))
                            break current;

                        if(comdef.clickType == ClickType.CLICK_RIGHT && !(((PlayerInteractEvent) event).getAction() == Action.RIGHT_CLICK_AIR || ((PlayerInteractEvent) event).getAction() == Action.RIGHT_CLICK_BLOCK))
                            break current;

                        if(comdef.clickType == ClickType.CLICK_LEFT && !(((PlayerInteractEvent) event).getAction() == Action.LEFT_CLICK_AIR || ((PlayerInteractEvent) event).getAction() == Action.LEFT_CLICK_BLOCK))
                            break current;
                    } else if (comdef.clickType == ClickType.ENTITY_RIGHT || comdef.clickType == ClickType.ENTITY_LEFT || comdef.clickType == ClickType.ENTITY_ARROW || comdef.clickType == ClickType.ENTITY_EITHER) {

                        if(!(event instanceof PlayerInteractEntityEvent) && !(event instanceof EntityDamageByEntityEvent))
                            break current;

                        if(comdef.clickType == ClickType.ENTITY_RIGHT && !(event instanceof PlayerInteractEntityEvent))
                            break current;

                        if(comdef.clickType == ClickType.ENTITY_LEFT && (!(event instanceof EntityDamageByEntityEvent) || !(((EntityDamageByEntityEvent) event).getDamager() instanceof Player)))
                            break current;

                        if(comdef.clickType == ClickType.ENTITY_ARROW && (!(event instanceof EntityDamageByEntityEvent) || !(((EntityDamageByEntityEvent) event).getDamager() instanceof Projectile)))
                            break current;
                    } else if (comdef.clickType == ClickType.BLOCK_BREAK || comdef.clickType == ClickType.BLOCK_PLACE || comdef.clickType == ClickType.BLOCK_EITHER) {

                        if(!(event instanceof BlockPlaceEvent) && !(event instanceof BlockBreakEvent))
                            break current;

                        if(comdef.clickType == ClickType.BLOCK_BREAK && !(event instanceof BlockBreakEvent))
                            break current;

                        if(comdef.clickType == ClickType.BLOCK_PLACE && !(event instanceof BlockPlaceEvent))
                            break current;
                    } else if (comdef.clickType == ClickType.ITEM_CONSUME || comdef.clickType == ClickType.ITEM_DROP || comdef.clickType == ClickType.ITEM_BREAK || comdef.clickType == ClickType.ITEM_PICKUP) {

                        if(comdef.clickType == ClickType.ITEM_CONSUME && !(event instanceof PlayerItemConsumeEvent))
                            break current;

                        if(comdef.clickType == ClickType.ITEM_DROP && !(event instanceof PlayerDropItemEvent))
                            break current;

                        if(comdef.clickType == ClickType.ITEM_BREAK && !(event instanceof PlayerItemBreakEvent))
                            break current;

                        if(comdef.clickType == ClickType.ITEM_PICKUP && !(event instanceof PlayerPickupItemEvent))
                            break current;
                    }
                }

                if(comdef.requireSneaking == TernaryState.TRUE && !player.isSneaking())
                    break current;
                if(comdef.requireSneaking == TernaryState.FALSE && player.isSneaking())
                    break current;

                if(!player.hasPermission("craftbook.mech.commanditems") || comdef.permNode != null && !comdef.permNode.isEmpty() && !player.hasPermission(comdef.permNode)) {
                    player.sendMessage(ChatColor.RED + "You don't have permissions to use this mechanic!");
                    break current;
                }

                if(cooldownPeriods.containsKey(new Tuple2<String, String>(player.getName(), comdef.name))) {
                    player.sendMessage(ChatColor.RED + "You have to wait " + cooldownPeriods.get(new Tuple2<String, String>(player.getName(), comdef.name)) + " seconds to use this again!");
                    break current;
                }

                for(ItemStack stack : def.consumables) {
                    if(!player.getInventory().containsAtLeast(stack, stack.getAmount())) {
                        player.sendMessage(ChatColor.RED + "You need " + stack.getAmount() + " of " + stack.getType().name() + " to use this command!");
                        break current;
                    }
                }
                if(def.consumables.length > 0) {
                    if(!player.getInventory().removeItem(def.consumables).isEmpty()) {
                        player.sendMessage(ChatColor.RED + "Inventory became out of sync during usage of command-items!");
                        break current;
                    } else
                        player.updateInventory();
                }
                if(def.consumeSelf) {
                    if(player.getItemInHand().getAmount() > 1)
                        player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
                    else
                        player.setItemInHand(null);
                    player.updateInventory();
                }

                for(String command : comdef.commands)
                    doCommand(command, event, comdef, player);

                if(comdef.cooldown > 0 && !player.hasPermission("craftbook.mech.commanditems.bypasscooldown"))
                    cooldownPeriods.put(new Tuple2<String, String>(player.getName(), comdef.name), comdef.cooldown);

                if(comdef.delayedCommands.length > 0)
                    Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new Runnable() {

                        @Override
                        public void run () {
                            for(String command : comdef.delayedCommands)
                                doCommand(command, event, comdef, player);
                        }
                    }, comdef.delay);

                if(event instanceof Cancellable && comdef.cancelAction)
                    ((Cancellable) event).setCancelled(true);
            }
        }
        }
    }

    public void doCommand(String command, Event event, CommandItemDefinition comdef, Player player) {

        if(command == null || command.trim().isEmpty())
            return;

        if(command.contains("@d") && !(event instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) event).getEntity() instanceof Player) && !(event instanceof PlayerInteractEntityEvent && ((PlayerInteractEntityEvent) event).getRightClicked() instanceof Player))
            return;

        if(event instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) event).getEntity() instanceof Player)
            command = command.replace("@d", ((Player) ((EntityDamageByEntityEvent) event).getEntity()).getName());
        if(event instanceof PlayerInteractEntityEvent && ((PlayerInteractEntityEvent) event).getRightClicked() instanceof Player)
            command = command.replace("@d", ((Player) ((PlayerInteractEntityEvent) event).getRightClicked()).getName());
        if(event instanceof BlockEvent && ((BlockEvent) event).getBlock() != null)
            command = command.replace("@b", ((BlockEvent) event).getBlock().getTypeId() + ((BlockEvent) event).getBlock().getData() == 0 ? "" : ":" + ((BlockEvent) event).getBlock().getData());
        if(event instanceof EntityEvent && ((EntityEvent) event).getEntityType() != null && command.contains("@e"))
            command = command.replace("@e", ((EntityEvent) event).getEntityType().getName());

        command = ParsingUtil.parseLine(command, player);
        if(comdef.type == CommandType.CONSOLE)
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        else if (comdef.type == CommandType.PLAYER)
            Bukkit.dispatchCommand(player, command);
        else  if (comdef.type == CommandType.SUPERUSER) {
            PermissionAttachment att = player.addAttachment(CraftBookPlugin.inst());
            att.setPermission("*", true);
            boolean wasOp = player.isOp();
            player.setOp(true);
            Bukkit.dispatchCommand(player, command);
            att.remove();
            player.setOp(wasOp);
        }
    }

    public static class CommandItemDefinition {

        private String name;
        private ItemStack stack;
        private String permNode;
        private CommandType type;
        private ClickType clickType;

        private String[] commands;

        private String[] delayedCommands;
        private int delay;
        private int cooldown;
        private boolean cancelAction;

        private ItemStack[] consumables;
        private boolean consumeSelf;

        private TernaryState requireSneaking;

        public ItemStack getItem() {

            return stack;
        }

        private CommandItemDefinition(String name, ItemStack stack, CommandType type, ClickType clickType, String permNode, String[] commands, int delay, String[] delayedCommands, int cooldown, boolean cancelAction, ItemStack[] consumables, boolean consumeSelf, TernaryState requireSneaking) {

            this.name = name;
            this.stack = stack;
            this.type = type;
            this.permNode = permNode;
            this.commands = commands;
            this.delay = delay;
            this.delayedCommands = delayedCommands;
            this.cooldown = cooldown;
            this.clickType = clickType;
            this.cancelAction = cancelAction;
            this.consumables = consumables;
            this.consumeSelf = consumeSelf;
            this.requireSneaking = requireSneaking;
        }

        public static CommandItemDefinition readDefinition(YAMLProcessor config, String path) {

            String name = RegexUtil.PERIOD_PATTERN.split(path)[1];
            ItemStack stack = ItemSyntax.getItem(config.getString(path + ".item"));
            List<String> commands = config.getStringList(path + ".commands", new ArrayList<String>());
            String permNode = config.getString(path + ".permission-node", "");
            CommandType type = CommandType.valueOf(config.getString(path + ".run-as", "PLAYER").toUpperCase(Locale.ENGLISH));
            ClickType clickType = ClickType.valueOf(config.getString(path + ".click-type", "CLICK_RIGHT").toUpperCase(Locale.ENGLISH));
            int delay = config.getInt(path + ".delay", 0);
            List<String> delayedCommands = new ArrayList<String>();
            if(delay > 0)
                delayedCommands = config.getStringList(path + ".delayed-commands", new ArrayList<String>());
            int cooldown = config.getInt(path + ".cooldown", 0);
            boolean cancelAction = config.getBoolean(path + ".cancel-action", true);

            List<ItemStack> consumables = new ArrayList<ItemStack>();

            try {
                for(String s : config.getStringList(path + ".consumed-items", new ArrayList<String>()))
                    consumables.add(ItemUtil.makeItemValid(ItemSyntax.getItem(s)));
            } catch(Exception ignored){}

            boolean consumeSelf = config.getBoolean(path + ".consume-self", false);
            TernaryState requireSneaking = TernaryState.getFromString(config.getString(path + ".require-sneaking-state", "either"));

            return new CommandItemDefinition(name, stack, type, clickType, permNode, commands.toArray(new String[commands.size()]), delay, delayedCommands.toArray(new String[delayedCommands.size()]), cooldown, cancelAction, consumables.toArray(new ItemStack[consumables.size()]), consumeSelf, requireSneaking);
        }

        public enum CommandType {

            PLAYER,CONSOLE,SUPERUSER;
        }

        public enum ClickType {

            CLICK_LEFT,CLICK_RIGHT,CLICK_EITHER,ENTITY_RIGHT,ENTITY_LEFT,ENTITY_ARROW,ENTITY_EITHER,BLOCK_BREAK,BLOCK_PLACE,BLOCK_EITHER,ANY,ITEM_CONSUME,ITEM_DROP,ITEM_BREAK,ITEM_PICKUP;
        }
    }
}