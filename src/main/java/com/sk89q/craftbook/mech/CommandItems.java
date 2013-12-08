package com.sk89q.craftbook.mech;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.LocalPlayer;
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

public class CommandItems extends AbstractCraftBookMechanic {

    public static CommandItems INSTANCE;

    private YAMLProcessor config;

    private Set<CommandItemDefinition> definitions;

    private Map<Tuple2<String, String>, Integer> cooldownPeriods;

    public CommandItemDefinition getDefinitionByName(String name) {

        for(CommandItemDefinition def : definitions)
            if(def.name.equalsIgnoreCase(name))
                return def;

        return null;
    }

    @Override
    public void disable () {
        definitions = null;
        cooldownPeriods = null;
        config = null;
        INSTANCE = null;
    }

    @Override
    public boolean enable() {

        INSTANCE = this;

        definitions = new HashSet<CommandItemDefinition>();
        cooldownPeriods = new HashMap<Tuple2<String, String>, Integer>();

        CraftBookPlugin.inst().createDefaultConfiguration(new File(CraftBookPlugin.inst().getDataFolder(), "command-items.yml"), "command-items.yml");
        if(!new File(CraftBookPlugin.inst().getDataFolder(), "command-items.yml").exists()) try {
            new File(CraftBookPlugin.inst().getDataFolder(), "command-items.yml").createNewFile(); //Just incase it wasn't packaged for some odd reason.
        } catch (IOException e1) {
        }
        config = new YAMLProcessor(new File(CraftBookPlugin.inst().getDataFolder(), "command-items.yml"), false, YAMLFormat.EXTENDED);

        try {
            config.load();
        } catch (IOException e) {
            BukkitUtil.printStacktrace(e);
            return false;
        }

        int amount = 0;

        for(String key : config.getKeys("command-items")) {

            CommandItemDefinition comdef = CommandItemDefinition.load(config, "command-items." + key);
            if(addDefinition(comdef)) {
                CraftBookPlugin.logDebugMessage("Added CommandItem: " + key, "command-items.initialize");
                amount++;
            } else
                CraftBookPlugin.logger().warning("Failed to add CommandItem: " + key);
        }

        if(amount == 0) return false;

        config.save();

        CraftBookPlugin.logger().info("Successfully added " + amount + " CommandItems!");

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

        if(CraftBookPlugin.inst().getPersistentStorage().get("command-items.death-items") == null)
            CraftBookPlugin.inst().getPersistentStorage().set("command-items.death-items", new HashMap<String, List<ItemStack>>());

        return true;
    }

    public boolean addDefinition(CommandItemDefinition def) {

        return definitions.add(def);
    }

    public void save() {

        config.addNode("command-items");

        for(CommandItemDefinition def : definitions) {
            config.addNode("command-items." + def.name);
            def.save(config, "command-items." + def.name);
        }

        config.save();

        disable();
        enable();
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

    @SuppressWarnings("unchecked")
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {

        Iterator<ItemStack> stackIt = event.getDrops().iterator();
        while(stackIt.hasNext()) {
            final ItemStack stack = stackIt.next();
            performCommandItems(stack, event.getEntity(), event);
            for(CommandItemDefinition def : definitions) {
                if(ItemUtil.areItemsIdentical(stack, def.getItem())) {
                    stackIt.remove();
                    Map<String, List<ItemStack>> items = (Map<String, List<ItemStack>>) CraftBookPlugin.inst().getPersistentStorage().get("command-items.death-items");
                    List<ItemStack> its = items.get(event.getEntity().getName());
                    if(its == null) its = new ArrayList<ItemStack>();
                    its.add(stack);
                    items.put(event.getEntity().getName(), its);
                    CraftBookPlugin.inst().getPersistentStorage().set("command-items.death-items", items);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerItemClick(InventoryClickEvent event) {

        if(event.getCurrentItem() == null || !(event.getWhoClicked() instanceof Player))
            return;

        performCommandItems(event.getCurrentItem(), (Player) event.getWhoClicked(), event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {

        if(event.getPlayer().getItemInHand() == null)
            return;

        performCommandItems(event.getPlayer().getItemInHand(), event.getPlayer(), event);
    }

    @SuppressWarnings("unchecked")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {

        Map<String, List<ItemStack>> items = (Map<String, List<ItemStack>>) CraftBookPlugin.inst().getPersistentStorage().get("command-items.death-items");
        if(!items.containsKey(event.getPlayer().getName())) return;
        List<ItemStack> its = items.get(event.getPlayer().getName());
        for(ItemStack it : its)
            event.getPlayer().getInventory().addItem(it);
        items.remove(event.getPlayer().getName());
        CraftBookPlugin.inst().getPersistentStorage().set("command-items.death-items", items);
    }

    @SuppressWarnings("deprecation")
    public void performCommandItems(ItemStack item, final Player player, final Event event) {

        LocalPlayer lplayer = CraftBookPlugin.inst().wrapPlayer(player);

        for(CommandItemDefinition def : definitions) {
            current: {
            if(ItemUtil.areItemsIdentical(def.stack, item)) {
                final CommandItemDefinition comdef = def;

                if(comdef.clickType != ClickType.ANY) {

                    //Mouse

                    if(comdef.clickType == ClickType.CLICK_RIGHT && (!(event instanceof PlayerInteractEvent) || !(((PlayerInteractEvent) event).getAction() == Action.RIGHT_CLICK_AIR || ((PlayerInteractEvent) event).getAction() == Action.RIGHT_CLICK_BLOCK)))
                        break current;

                    if(comdef.clickType == ClickType.CLICK_LEFT && (!(event instanceof PlayerInteractEvent) || !(((PlayerInteractEvent) event).getAction() == Action.LEFT_CLICK_AIR || ((PlayerInteractEvent) event).getAction() == Action.LEFT_CLICK_BLOCK)))
                        break current;

                    if(comdef.clickType == ClickType.CLICK_EITHER && (!(event instanceof PlayerInteractEvent) || !(((PlayerInteractEvent) event).getAction() == Action.LEFT_CLICK_AIR || ((PlayerInteractEvent) event).getAction() == Action.LEFT_CLICK_BLOCK || ((PlayerInteractEvent) event).getAction() == Action.RIGHT_CLICK_AIR || ((PlayerInteractEvent) event).getAction() == Action.RIGHT_CLICK_BLOCK)))
                        break current;

                    //Entity

                    if(comdef.clickType == ClickType.ENTITY_RIGHT && !(event instanceof PlayerInteractEntityEvent))
                        break current;

                    if(comdef.clickType == ClickType.ENTITY_LEFT && (!(event instanceof EntityDamageByEntityEvent) || !(((EntityDamageByEntityEvent) event).getDamager() instanceof Player)))
                        break current;

                    if(comdef.clickType == ClickType.ENTITY_EITHER && !(event instanceof PlayerInteractEntityEvent) && (!(event instanceof EntityDamageByEntityEvent) || !(((EntityDamageByEntityEvent) event).getDamager() instanceof Player)))
                        break current;

                    if(comdef.clickType == ClickType.ENTITY_ARROW && (!(event instanceof EntityDamageByEntityEvent) || !(((EntityDamageByEntityEvent) event).getDamager() instanceof Projectile)))
                        break current;

                    //Block

                    if(comdef.clickType == ClickType.BLOCK_BREAK && !(event instanceof BlockBreakEvent))
                        break current;

                    if(comdef.clickType == ClickType.BLOCK_PLACE && !(event instanceof BlockPlaceEvent))
                        break current;

                    if(comdef.clickType == ClickType.BLOCK_EITHER && !(event instanceof BlockPlaceEvent) && !(event instanceof BlockBreakEvent))
                        break current;

                    //Item

                    if(comdef.clickType == ClickType.ITEM_CONSUME && !(event instanceof PlayerItemConsumeEvent))
                        break current;

                    if(comdef.clickType == ClickType.ITEM_DROP && !(event instanceof PlayerDropItemEvent))
                        break current;

                    if(comdef.clickType == ClickType.ITEM_BREAK && !(event instanceof PlayerItemBreakEvent))
                        break current;

                    if(comdef.clickType == ClickType.ITEM_PICKUP && !(event instanceof PlayerPickupItemEvent))
                        break current;

                    if((comdef.clickType == ClickType.ITEM_CLICK_EITHER || comdef.clickType == ClickType.ITEM_CLICK_LEFT || comdef.clickType == ClickType.ITEM_CLICK_RIGHT) && !(event instanceof InventoryClickEvent))
                        break current;
                    else if (event instanceof InventoryClickEvent) {
                        if(comdef.clickType == ClickType.ITEM_CLICK_LEFT && !((InventoryClickEvent) event).getClick().isLeftClick())
                            break current;
                        else if(comdef.clickType == ClickType.ITEM_CLICK_RIGHT && !((InventoryClickEvent) event).getClick().isRightClick())
                            break current;
                    }

                    //Player

                    if(comdef.clickType == ClickType.PLAYER_DEATH && !(event instanceof PlayerDeathEvent))
                        break current;

                    if(comdef.clickType == ClickType.PLAYER_CHAT && !(event instanceof AsyncPlayerChatEvent))
                        break current;
                }

                if(comdef.requireSneaking == TernaryState.TRUE && !lplayer.isSneaking())
                    break current;
                if(comdef.requireSneaking == TernaryState.FALSE && lplayer.isSneaking())
                    break current;

                if(!lplayer.hasPermission("craftbook.mech.commanditems") || comdef.permNode != null && !comdef.permNode.isEmpty() && !lplayer.hasPermission(comdef.permNode)) {
                    if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                        lplayer.printError("mech.use-permission");
                    break current;
                }

                if(cooldownPeriods.containsKey(new Tuple2<String, String>(lplayer.getName(), comdef.name))) {
                    lplayer.printError(lplayer.translate("mech.command-items.wait") + " " + cooldownPeriods.get(new Tuple2<String, String>(lplayer.getName(), comdef.name)) + " " + lplayer.translate("mech.command-items.wait-seconds"));
                    break current;
                }

                for(ItemStack stack : def.consumables) {
                    if(!player.getInventory().containsAtLeast(stack, stack.getAmount())) {
                        lplayer.printError(lplayer.translate("mech.command-items.need") + " " + stack.getAmount() + " " + stack.getType().name() + " " + lplayer.translate("mech.command-items.need-use"));
                        break current;
                    }
                }
                if(def.consumables.length > 0) {
                    if(!player.getInventory().removeItem(def.consumables).isEmpty()) {
                        lplayer.printError("mech.command-items.out-of-sync");
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

                if(comdef.cooldown > 0 && !lplayer.hasPermission("craftbook.mech.commanditems.bypasscooldown"))
                    cooldownPeriods.put(new Tuple2<String, String>(lplayer.getName(), comdef.name), comdef.cooldown);

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

        if(event instanceof EntityDamageByEntityEvent)
            command = command.replace("@d", ((EntityDamageByEntityEvent) event).getEntity() instanceof Player ? ((Player) ((EntityDamageByEntityEvent) event).getEntity()).getName() : ((EntityDamageByEntityEvent) event).getEntityType().getName());
        if(event instanceof PlayerInteractEntityEvent)
            command = command.replace("@d", ((PlayerInteractEntityEvent) event).getRightClicked() instanceof Player ? ((Player) ((PlayerInteractEntityEvent) event).getRightClicked()).getName() : ((PlayerInteractEntityEvent) event).getRightClicked().getType().name());
        if(event instanceof BlockEvent && ((BlockEvent) event).getBlock() != null)
            command = command.replace("@b", ((BlockEvent) event).getBlock().getType().name() + (((BlockEvent) event).getBlock().getData() == 0 ? "" : ":") + ((BlockEvent) event).getBlock().getData());
        if(event instanceof EntityEvent && ((EntityEvent) event).getEntityType() != null && command.contains("@e"))
            command = command.replace("@e", ((EntityEvent) event).getEntityType().getName());
        if(event instanceof AsyncPlayerChatEvent && command.contains("@m"))
            command = command.replace("@m", ((AsyncPlayerChatEvent) event).getMessage());

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

        private boolean keepOnDeath;

        public ItemStack getItem() {

            return stack;
        }

        public CommandItemDefinition(String name, ItemStack stack, CommandType type, ClickType clickType, String permNode, String[] commands, int delay, String[] delayedCommands, int cooldown, boolean cancelAction, ItemStack[] consumables, boolean consumeSelf, TernaryState requireSneaking, boolean keepOnDeath) {

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
            this.keepOnDeath = keepOnDeath;
        }

        public static CommandItemDefinition load(YAMLProcessor config, String path) {

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

            boolean keepOnDeath = config.getBoolean(path + ".keep-on-death", false);

            return new CommandItemDefinition(name, stack, type, clickType, permNode, commands.toArray(new String[commands.size()]), delay, delayedCommands.toArray(new String[delayedCommands.size()]), cooldown, cancelAction, consumables.toArray(new ItemStack[consumables.size()]), consumeSelf, requireSneaking, keepOnDeath);
        }

        public void save(YAMLProcessor config, String path) {

            config.setProperty(path + ".item", ItemSyntax.getStringFromItem(getItem()));
            config.setProperty(path + ".commands", commands);
            config.setProperty(path + ".permission-node", permNode);
            config.setProperty(path + ".run-as", type.name());
            config.setProperty(path + ".click-type", clickType.name());
            config.setProperty(path + ".delay", delay);
            if(delay > 0)
                config.setProperty(path + ".delayed-commands", delayedCommands);
            config.setProperty(path + ".cooldown", cooldown);
            config.setProperty(path + ".cancel-action", cancelAction);

            List<String> consumables = new ArrayList<String>();
            for(ItemStack s : this.consumables)
                consumables.add(ItemSyntax.getStringFromItem(s));
            config.setProperty(path + ".consumed-items", consumables);
            config.setProperty(path + ".consume-self", consumeSelf);
            config.setProperty(path + ".require-sneaking-state", requireSneaking.name());
            config.setProperty(path + ".keep-on-death", keepOnDeath);
        }

        public enum CommandType {

            PLAYER,CONSOLE,SUPERUSER;
        }

        public enum ClickType {

            CLICK_LEFT,CLICK_RIGHT,CLICK_EITHER,ENTITY_RIGHT,ENTITY_LEFT,ENTITY_ARROW,ENTITY_EITHER,BLOCK_BREAK,BLOCK_PLACE,BLOCK_EITHER,ANY,ITEM_CONSUME,ITEM_DROP,ITEM_BREAK,ITEM_PICKUP,ITEM_CLICK_LEFT,ITEM_CLICK_RIGHT,ITEM_CLICK_EITHER,PLAYER_DEATH,PLAYER_CHAT;
        }
    }
}