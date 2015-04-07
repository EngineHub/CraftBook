package com.sk89q.craftbook.mechanics.items;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
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
import org.bukkit.event.entity.ProjectileHitEvent;
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
import com.sk89q.craftbook.mechanics.items.CommandItemAction.ActionRunStage;
import com.sk89q.craftbook.mechanics.items.CommandItemDefinition.CommandType;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.LoadPriority;
import com.sk89q.craftbook.util.ParsingUtil;
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
        config = new YAMLProcessor(new File(CraftBookPlugin.inst().getDataFolder(), "command-items.yml"), false, YAMLFormat.EXTENDED);

        try {
            config.load();
        } catch (IOException e) {
            CraftBookPlugin.logger().severe("Corrupt CommandItems command-items.yml File! Make sure that the correct syntax has been used, and that there are no tabs!");
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

        if(definitions.size() > 0) {
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
            Bukkit.getScheduler().runTaskTimer(CraftBookPlugin.inst(), new Runnable() {

                @Override
                public void run () {

                    for(Player player : Bukkit.getOnlinePlayers()) {
                        if(player.getItemInHand() != null)
                            performCommandItems(player.getItemInHand(), player, null);
                        for(ItemStack stack : player.getInventory().getArmorContents())
                            if(stack != null)
                                performCommandItems(stack, player, null);
                    }
                }
            }, 10, 10);
        }

        if(!CraftBookPlugin.inst().getPersistentStorage().has("command-items.death-items"))
            CraftBookPlugin.inst().getPersistentStorage().set("command-items.death-items", new HashMap<String, List<String>>());

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

    @EventHandler(priority=EventPriority.HIGH)
    public void onPlayerInteract(final PlayerInteractEvent event) {

        if(event.getItem() == null)
            return;

        if(event.getAction() == Action.PHYSICAL)
            return;

        performCommandItems(event.getItem(), event.getPlayer(), event);
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onPlayerHitEntity(final PlayerInteractEntityEvent event) {

        if(event.getPlayer().getItemInHand() == null)
            return;

        performCommandItems(event.getPlayer().getItemInHand(), event.getPlayer(), event);
    }

    @EventHandler(priority=EventPriority.HIGH)
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

    @EventHandler(priority=EventPriority.HIGH)
    public void onBlockBreak(final BlockBreakEvent event) {

        if(event.getPlayer().getItemInHand() == null)
            return;

        performCommandItems(event.getPlayer().getItemInHand(), event.getPlayer(), event);
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onProjectileHit(final ProjectileHitEvent event) {

        if(!(event.getEntity().getShooter() instanceof Player))
            return;

        if(((Player) event.getEntity().getShooter()).getItemInHand() == null)
            return;

        Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new Runnable() {
            @Override
            public void run () {
                performCommandItems(((Player) event.getEntity().getShooter()).getItemInHand(), (Player) event.getEntity().getShooter(), event);
            }
        }, 5L);
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onBlockPlace(final BlockPlaceEvent event) {

        if(event.getPlayer().getItemInHand() == null)
            return;

        performCommandItems(event.getPlayer().getItemInHand(), event.getPlayer(), event);
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onItemConsume(final PlayerItemConsumeEvent event) {

        if(event.getItem() == null)
            return;

        performCommandItems(event.getItem(), event.getPlayer(), event);
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onItemDrop(final PlayerDropItemEvent event) {

        if(event.getItemDrop() == null || !ItemUtil.isStackValid(event.getItemDrop().getItemStack()))
            return;

        performCommandItems(event.getItemDrop().getItemStack(), event.getPlayer(), event);
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onItemBreak(final PlayerItemBreakEvent event) {

        if(event.getBrokenItem() == null)
            return;

        performCommandItems(event.getBrokenItem(), event.getPlayer(), event);
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onItemPickup(final PlayerPickupItemEvent event) {

        if(event.getItem() == null)
            return;

        performCommandItems(event.getItem().getItemStack(), event.getPlayer(), event);
    }

    @SuppressWarnings("unchecked")
    @EventHandler(priority=EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {

        if (!EventUtil.passesFilter(event))
            return;

        Iterator<ItemStack> stackIt = event.getDrops().iterator();
        while(stackIt.hasNext()) {
            final ItemStack stack = stackIt.next();
            performCommandItems(stack, event.getEntity(), event);
            for(CommandItemDefinition def : definitions) {
                if(ItemUtil.areItemsIdentical(stack, def.getItem()) && def.keepOnDeath) {
                    stackIt.remove();
                    Map<String, List<String>> items = (Map<String, List<String>>) CraftBookPlugin.inst().getPersistentStorage().get("command-items.death-items");
                    List<String> its = items.get(event.getEntity().getName());
                    if(its == null) its = new ArrayList<String>();
                    its.add(ItemSyntax.getStringFromItem(stack));
                    items.put(event.getEntity().getName(), its);
                    CraftBookPlugin.inst().getPersistentStorage().set("command-items.death-items", items);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerItemClick(InventoryClickEvent event) {

        if(event.getCurrentItem() == null || !(event.getWhoClicked() instanceof Player))
            return;

        performCommandItems(event.getCurrentItem(), (Player) event.getWhoClicked(), event);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(AsyncPlayerChatEvent event) {

        if(event.getPlayer().getItemInHand() == null)
            return;

        performCommandItems(event.getPlayer().getItemInHand(), event.getPlayer(), event);
    }

    @SuppressWarnings("unchecked")
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {

        Map<String, List<String>> items = (Map<String, List<String>>) CraftBookPlugin.inst().getPersistentStorage().get("command-items.death-items");
        if(!items.containsKey(event.getPlayer().getName())) return;
        List<String> its = items.get(event.getPlayer().getName());
        for(String it : its)
            event.getPlayer().getInventory().addItem(ItemSyntax.getItem(it));
        items.remove(event.getPlayer().getName());
        CraftBookPlugin.inst().getPersistentStorage().set("command-items.death-items", items);
    }

    @SuppressWarnings("deprecation")
    public void performCommandItems(ItemStack item, final Player player, final Event event) {

        if (event != null && !EventUtil.passesFilter(event))
            return;

        LocalPlayer lplayer = CraftBookPlugin.inst().wrapPlayer(player);

        for(CommandItemDefinition def : definitions) {
            current: {
            if(ItemUtil.areItemsIdentical(def.stack, item)) {
                final CommandItemDefinition comdef = def;

                if(!comdef.clickType.doesPassType(event)) break current;

                if(!comdef.requireSneaking.doesPass(lplayer.isSneaking())) break current;

                if(!lplayer.hasPermission("craftbook.mech.commanditems") || comdef.permNode != null && !comdef.permNode.isEmpty() && !lplayer.hasPermission(comdef.permNode)) {
                    if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                        lplayer.printError("mech.use-permission");
                    break current;
                }

                if(cooldownPeriods.containsKey(new Tuple2<String, String>(lplayer.getName(), comdef.name))) {
                    if(def.clickType != ClickType.PASSIVE && !def.cooldownMessage.isEmpty())
                        lplayer.printError(lplayer.translate(def.cooldownMessage).replace("%time%", String.valueOf(cooldownPeriods.get(new Tuple2<String, String>(lplayer.getName(), comdef.name)))));
                    break current;
                }

                for(ItemStack stack : def.consumables) {

                    boolean found = false;

                    int amount = 0;

                    for(ItemStack tStack : player.getInventory().getContents()) {
                        if(ItemUtil.areItemsIdentical(stack, tStack)) {

                            amount += tStack.getAmount();

                            if(amount >=stack.getAmount()) {
                                found = true;
                                break;
                            }
                        }
                    }

                    if(!found && !def.missingConsumableMessage.isEmpty()) {
                        lplayer.printError(lplayer.translate(def.missingConsumableMessage).replace("%item%", stack.getAmount() + " " + stack.getType().name()));
                        break current;
                    }
                }

                for(ItemStack stack : def.consumables) {

                    boolean found = false;

                    int amount = stack.getAmount();

                    for(int i = 0; i < player.getInventory().getContents().length; i++) {
                        ItemStack tStack = player.getInventory().getContents()[i];
                        if(ItemUtil.areItemsIdentical(stack, tStack)) {
                            ItemStack toRemove = tStack.clone();
                            if(toRemove.getAmount() > amount) {

                                toRemove.setAmount(toRemove.getAmount() - amount);
                                player.getInventory().setItem(i, toRemove);
                                amount = 0;
                            } else {
                                amount -= toRemove.getAmount();
                                player.getInventory().setItem(i, null);
                            }
                            if(amount <= 0) {
                                found = true;
                                break;
                            }
                        }
                    }

                    if(!found) {
                        lplayer.printError("mech.command-items.out-of-sync");
                        break current;
                    }
                }

                if(def.consumeSelf) {
                    if(player.getItemInHand().getAmount() > 1)
                        player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
                    else
                        player.setItemInHand(null);
                }

                player.updateInventory();

                for(CommandItemAction action : comdef.actions)
                    if(action.stage == ActionRunStage.BEFORE)
                        if(!action.runAction(comdef, event, player))
                            break current;

                for(String command : comdef.commands)
                    doCommand(command, event, comdef, player);

                for(CommandItemAction action : comdef.actions)
                    if(action.stage == ActionRunStage.AFTER)
                        action.runAction(comdef, event, player);

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

        command = parseLine(command, event, player);

        if(comdef.type == CommandType.CONSOLE)
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        else if (comdef.type == CommandType.PLAYER)
            Bukkit.dispatchCommand(player, command);
        else  if (comdef.type == CommandType.SUPERUSER) {
            PermissionAttachment att = player.addAttachment(CraftBookPlugin.inst());
            att.setPermission("*", true);
            boolean wasOp = player.isOp();
            if(!wasOp)
                player.setOp(true);
            Bukkit.dispatchCommand(player, command);
            att.remove();
            if(!wasOp)
                player.setOp(wasOp);
        }
    }

    public String parseLine(String command, Event event, Player player) {

        if(command == null) return null;

        if(event instanceof EntityDamageByEntityEvent) {
            command = StringUtils.replace(command, "@d.x", String.valueOf(((EntityDamageByEntityEvent) event).getEntity().getLocation().getX()));
            command = StringUtils.replace(command, "@d.y", String.valueOf(((EntityDamageByEntityEvent) event).getEntity().getLocation().getY()));
            command = StringUtils.replace(command, "@d.z", String.valueOf(((EntityDamageByEntityEvent) event).getEntity().getLocation().getZ()));
            command = StringUtils.replace(command, "@d.bx", String.valueOf(((EntityDamageByEntityEvent) event).getEntity().getLocation().getBlockX()));
            command = StringUtils.replace(command, "@d.by", String.valueOf(((EntityDamageByEntityEvent) event).getEntity().getLocation().getBlockY()));
            command = StringUtils.replace(command, "@d.bz", String.valueOf(((EntityDamageByEntityEvent) event).getEntity().getLocation().getBlockZ()));
            command = StringUtils.replace(command, "@d.w", String.valueOf(((EntityDamageByEntityEvent) event).getEntity().getLocation().getWorld().getName()));
            command = StringUtils.replace(command, "@d.l", ((EntityDamageByEntityEvent) event).getEntity().getLocation().toString());
            command = StringUtils.replace(command, "@d.u", ((EntityDamageByEntityEvent) event).getEntity().getUniqueId().toString());
            if(((EntityDamageByEntityEvent) event).getEntity() instanceof Player) {
                command = StringUtils.replace(command, "@d.i", CraftBookPlugin.inst().getUUIDMappings().getCBID(((EntityDamageByEntityEvent) event).getEntity().getUniqueId()));
                command = StringUtils.replace(command, "@d", ((Player) ((EntityDamageByEntityEvent) event).getEntity()).getName());
            } else
                command = StringUtils.replace(command, "@d", ((EntityDamageByEntityEvent) event).getEntity().getType().name());
        }
        if(event instanceof PlayerInteractEntityEvent) {
            command = StringUtils.replace(command, "@d.x", String.valueOf(((PlayerInteractEntityEvent) event).getRightClicked().getLocation().getX()));
            command = StringUtils.replace(command, "@d.y", String.valueOf(((PlayerInteractEntityEvent) event).getRightClicked().getLocation().getY()));
            command = StringUtils.replace(command, "@d.z", String.valueOf(((PlayerInteractEntityEvent) event).getRightClicked().getLocation().getZ()));
            command = StringUtils.replace(command, "@d.bx", String.valueOf(((PlayerInteractEntityEvent) event).getRightClicked().getLocation().getBlockX()));
            command = StringUtils.replace(command, "@d.by", String.valueOf(((PlayerInteractEntityEvent) event).getRightClicked().getLocation().getBlockY()));
            command = StringUtils.replace(command, "@d.bz", String.valueOf(((PlayerInteractEntityEvent) event).getRightClicked().getLocation().getBlockZ()));
            command = StringUtils.replace(command, "@d.w", String.valueOf(((PlayerInteractEntityEvent) event).getRightClicked().getLocation().getWorld().getName()));
            command = StringUtils.replace(command, "@d.l", ((PlayerInteractEntityEvent) event).getRightClicked().getLocation().toString());
            command = StringUtils.replace(command, "@d.u", ((PlayerInteractEntityEvent) event).getRightClicked().getUniqueId().toString());
            if(((PlayerInteractEntityEvent) event).getRightClicked() instanceof Player) {
                command = StringUtils.replace(command, "@d.i", CraftBookPlugin.inst().getUUIDMappings().getCBID(((PlayerInteractEntityEvent) event).getRightClicked().getUniqueId()));
                command = StringUtils.replace(command, "@d", ((Player) ((PlayerInteractEntityEvent) event).getRightClicked()).getName());
            } else
                command = StringUtils.replace(command, "@d", ((PlayerInteractEntityEvent) event).getRightClicked().getType().name());
        }
        if(event instanceof BlockEvent && ((BlockEvent) event).getBlock() != null) {
            command = StringUtils.replace(command, "@b.x", String.valueOf(((BlockEvent) event).getBlock().getX()));
            command = StringUtils.replace(command, "@b.y", String.valueOf(((BlockEvent) event).getBlock().getY()));
            command = StringUtils.replace(command, "@b.z", String.valueOf(((BlockEvent) event).getBlock().getZ()));
            command = StringUtils.replace(command, "@b.w", ((BlockEvent) event).getBlock().getLocation().getWorld().getName());
            command = StringUtils.replace(command, "@b.l", ((BlockEvent) event).getBlock().getLocation().toString());
            command = StringUtils.replace(command, "@b", ((BlockEvent) event).getBlock().getType().name() + (((BlockEvent) event).getBlock().getData() == 0 ? "" : ":") + ((BlockEvent) event).getBlock().getData());
        }
        if(event instanceof PlayerInteractEvent && ((PlayerInteractEvent) event).getClickedBlock() != null) {
            command = StringUtils.replace(command, "@b.x", String.valueOf(((PlayerInteractEvent) event).getClickedBlock().getX()));
            command = StringUtils.replace(command, "@b.y", String.valueOf(((PlayerInteractEvent) event).getClickedBlock().getY()));
            command = StringUtils.replace(command, "@b.z", String.valueOf(((PlayerInteractEvent) event).getClickedBlock().getZ()));
            command = StringUtils.replace(command, "@b.w", String.valueOf(((PlayerInteractEvent) event).getClickedBlock().getWorld().getName()));
            command = StringUtils.replace(command, "@b.l", ((PlayerInteractEvent) event).getClickedBlock().getLocation().toString());
            command = StringUtils.replace(command, "@b", ((PlayerInteractEvent) event).getClickedBlock().getType().name() + (((PlayerInteractEvent) event).getClickedBlock().getData() == 0 ? "" : ":") + ((PlayerInteractEvent) event).getClickedBlock().getData());
        }
        if(event instanceof EntityEvent && ((EntityEvent) event).getEntityType() != null && command.contains("@e")) {
            command = StringUtils.replace(command, "@e.x", String.valueOf(((EntityEvent) event).getEntity().getLocation().getX()));
            command = StringUtils.replace(command, "@e.y", String.valueOf(((EntityEvent) event).getEntity().getLocation().getY()));
            command = StringUtils.replace(command, "@e.z", String.valueOf(((EntityEvent) event).getEntity().getLocation().getZ()));
            command = StringUtils.replace(command, "@e.bx", String.valueOf(((EntityEvent) event).getEntity().getLocation().getBlockX()));
            command = StringUtils.replace(command, "@e.by", String.valueOf(((EntityEvent) event).getEntity().getLocation().getBlockY()));
            command = StringUtils.replace(command, "@e.bz", String.valueOf(((EntityEvent) event).getEntity().getLocation().getBlockZ()));
            command = StringUtils.replace(command, "@e.w", String.valueOf(((EntityEvent) event).getEntity().getLocation().getWorld().getName()));
            command = StringUtils.replace(command, "@e.l", ((EntityEvent) event).getEntity().getLocation().toString());
            command = StringUtils.replace(command, "@e.u", ((EntityEvent) event).getEntity().getUniqueId().toString());
            command = StringUtils.replace(command, "@e", ((EntityEvent) event).getEntityType().getName());
        }
        if(event instanceof AsyncPlayerChatEvent && command.contains("@m"))
            command = StringUtils.replace(command, "@m", ((AsyncPlayerChatEvent) event).getMessage());

        command = ParsingUtil.parsePlayerTags(command, player);
        command = ParsingUtil.parseVariables(command, null);

        return command;
    }

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

    }

    @Override
    public LoadPriority getLoadPriority() {

        return LoadPriority.EARLY;
    }
}