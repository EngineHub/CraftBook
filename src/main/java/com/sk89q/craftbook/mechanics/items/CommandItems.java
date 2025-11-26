package com.sk89q.craftbook.mechanics.items;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.mechanics.items.CommandItemAction.ActionRunStage;
import com.sk89q.craftbook.mechanics.items.CommandItemDefinition.CommandType;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.LoadPriority;
import com.sk89q.craftbook.util.ParsingUtil;
import com.sk89q.craftbook.util.ProtectionUtil;
import com.sk89q.craftbook.util.Tuple2;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
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
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;

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
import java.util.UUID;

public class CommandItems extends AbstractCraftBookMechanic {

    public static CommandItems INSTANCE;

    private YAMLProcessor config;

    private Set<CommandItemDefinition> definitions;

    private Map<Tuple2<String, String>, Integer> cooldownPeriods;
    private Map<UUID, List<ItemStack>> deathPersistItems = Maps.newHashMap();

    private boolean doChat = false;

    public CommandItemDefinition getDefinitionByName(String name) {

        for(CommandItemDefinition def : definitions)
            if(def.name.equalsIgnoreCase(name))
                return def;

        return null;
    }

    @Override
    public void disable () {
        for (Entry<UUID, List<ItemStack>> deathPersistEntry : deathPersistItems.entrySet()) {
            Map<String, List<String>> items = (Map<String, List<String>>) CraftBookPlugin.inst().getPersistentStorage().get("command-items.death-items");
            List<String> its = items.get(deathPersistEntry.getKey().toString());
            if (its == null) its = new ArrayList<>();
            for (ItemStack stack : deathPersistEntry.getValue()) {
                its.add(ItemSyntax.getStringFromItem(stack));
            }
            items.put(deathPersistEntry.getKey().toString(), its);
            CraftBookPlugin.inst().getPersistentStorage().set("command-items.death-items", items);
        }

        definitions = null;
        cooldownPeriods = null;
        config = null;
        INSTANCE = null;
    }

    @Override
    public boolean enable() {

        INSTANCE = this;

        definitions = new HashSet<>();
        cooldownPeriods = new HashMap<>();

        CraftBookPlugin.inst().createDefaultConfiguration(new File(CraftBookPlugin.inst().getDataFolder(), "command-items.yml"), "command-items.yml");
        config = new YAMLProcessor(new File(CraftBookPlugin.inst().getDataFolder(), "command-items.yml"), false, YAMLFormat.EXTENDED);

        try {
            config.load();
        } catch (IOException e) {
            CraftBookPlugin.logger().severe("Corrupt CommandItems command-items.yml File! Make sure that the correct syntax has been used, and that there are no tabs!");
            CraftBookBukkitUtil.printStacktrace(e);
            return false;
        }

        int amount = 0;

        for(String key : config.getKeys("command-items")) {

            CommandItemDefinition comdef = CommandItemDefinition.load(config, "command-items." + key);
            if(addDefinition(comdef)) {
                CraftBookPlugin.logDebugMessage("Added CommandItem: " + key, "command-items.initialize");
                amount++;

                if (comdef.type == CommandType.SUPERUSER && !"true".equals(System.getProperty("craftbook.ignore-superuser-warning"))) {
                    CraftBookPlugin.logger().warning("Type `SUPERUSER` in use for CommandItem " + comdef.name + ". This is not recommended due to safety and performance issues with Spigot and the way permission plugins work. This will be removed in the future. Try to migrate to `CONSOLE`. Set property craftbook.ignore-superuser-warning to true to hide this message.");
                }
            } else
                CraftBookPlugin.logger().warning("Failed to add CommandItem: " + key);
        }

        if(amount == 0) return false;

        config.save();

        CraftBookPlugin.logger().info("Successfully added " + amount + " CommandItems!");

        if(definitions.size() > 0) {
            Bukkit.getScheduler().runTaskTimer(CraftBookPlugin.inst(), () -> {
                Iterator<Entry<Tuple2<String, String>, Integer>> iterator = cooldownPeriods.entrySet().iterator();

                while(iterator.hasNext()) {

                    Entry<Tuple2<String, String>, Integer> entry = iterator.next();
                    if(entry.getValue() > 1)
                        cooldownPeriods.put(entry.getKey(), entry.getValue() - 1);
                    else
                        iterator.remove();
                }
            }, 0, 20);
            Bukkit.getScheduler().runTaskTimer(CraftBookPlugin.inst(), () -> {
                for(Player player : Bukkit.getOnlinePlayers()) {
                    if(player.getInventory().getItemInMainHand().getType() != Material.AIR)
                        performCommandItems(player.getInventory().getItemInMainHand(), player, null);
                    if(player.getInventory().getItemInOffHand().getType() != Material.AIR)
                        performCommandItems(player.getInventory().getItemInOffHand(), player, null);
                    for(ItemStack stack : player.getInventory().getArmorContents())
                        if(stack != null && stack.getType() != Material.AIR)
                            performCommandItems(stack, player, null);
                }
            }, 10, 10);
        }

        if(!CraftBookPlugin.inst().getPersistentStorage().has("command-items.death-items")) {
            CraftBookPlugin.inst().getPersistentStorage().set("command-items.death-items", new HashMap<String, List<String>>());
        } else {
            Map<String, List<String>> items = (Map<String, List<String>>) CraftBookPlugin.inst().getPersistentStorage().get("command-items.death-items");
            for (Entry<String, List<String>> entry : items.entrySet()) {
                UUID uuid = UUID.fromString(entry.getKey());
                List<ItemStack> its = Lists.newArrayList();
                for (String item : entry.getValue()) {
                    its.add(ItemSyntax.getItem(item));
                }
                deathPersistItems.put(uuid, its);
            }

            items.clear();
            CraftBookPlugin.inst().getPersistentStorage().set("command-items.death-items", items);
        }

        doChat = definitions.stream().anyMatch(def -> def.clickType == ClickType.PLAYER_CHAT);

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

        if(event.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR)
            return;

        performCommandItems(event.getPlayer().getInventory().getItemInMainHand(), event.getPlayer(), event);
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onEntityDamageEntity(final EntityDamageByEntityEvent event) {

        Player p;
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
    public void onProjectileLaunch(final ProjectileLaunchEvent event) {
        if(!(event.getEntity().getShooter() instanceof Player))
            return;

        if(((Player) event.getEntity().getShooter()).getItemInHand() == null)
            return;

        final ItemStack item = ((Player) event.getEntity().getShooter()).getItemInHand();
        final Player shooter = (Player) event.getEntity().getShooter();

        Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), () -> performCommandItems(item, shooter, event), 5L);
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onProjectileHit(final ProjectileHitEvent event) {

        if(!(event.getEntity().getShooter() instanceof Player))
            return;

        if(((Player) event.getEntity().getShooter()).getItemInHand() == null)
            return;

        final ItemStack item = ((Player) event.getEntity().getShooter()).getItemInHand();
        final Player shooter = (Player) event.getEntity().getShooter();

        Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), () -> performCommandItems(item, shooter, event), 5L);
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onBlockPlace(final BlockPlaceEvent event) {

        if(event.getItemInHand() == null)
            return;

        performCommandItems(event.getItemInHand(), event.getPlayer(), event);
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
    public void onItemPickup(final EntityPickupItemEvent event) {

        if (event.getItem() == null)
            return;

        if (event.getEntity() instanceof Player) {
            performCommandItems(event.getItem().getItemStack(), (Player) event.getEntity(), event);
        }
    }

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
                    List<ItemStack> items = deathPersistItems.get(event.getEntity().getUniqueId());
                    if (items == null) items = Lists.newArrayList();
                    items.add(stack);
                    deathPersistItems.put(event.getEntity().getUniqueId(), items);
                    stackIt.remove();
                    break;
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

        if(!doChat || event.getPlayer().getItemInHand() == null)
            return;

        Bukkit.getScheduler().runTask(CraftBookPlugin.inst(),
                () -> performCommandItems(event.getPlayer().getItemInHand(), event.getPlayer(), event));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {

        if(!deathPersistItems.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }

        List<ItemStack> its = deathPersistItems.get(event.getPlayer().getUniqueId());
        for(ItemStack it : its) {
            event.getPlayer().getInventory().addItem(it);
        }
        deathPersistItems.remove(event.getPlayer().getUniqueId());
    }

    public void performCommandItems(ItemStack item, final Player player, final Event event) {
        if (event != null && !EventUtil.passesFilter(event))
            return;

        CraftBookPlayer lplayer = CraftBookPlugin.inst().wrapPlayer(player);

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

                if(event instanceof Cancellable && comdef.cancelAction)
                    ((Cancellable) event).setCancelled(true);

                if(cooldownPeriods.containsKey(new Tuple2<>(lplayer.getName(), comdef.name))) {
                    if(def.clickType != ClickType.PASSIVE && !def.cooldownMessage.isEmpty())
                        lplayer.printError(lplayer.translate(def.cooldownMessage).replace("%time%", String.valueOf(cooldownPeriods.get(
                                new Tuple2<>(lplayer.getName(), comdef.name)))));
                    break current;
                }

                for (CommandItemAction action : comdef.actions) {
                    if (action.stage == ActionRunStage.BEFORE) {
                        if (!action.runAction(comdef, event, player)) {
                            break current;
                        }
                    }
                }

                if (!player.hasPermission("craftbook.mech.commanditems.bypassconsumables") && !player.getGameMode().equals(GameMode.CREATIVE)) {
                    for (ItemStack stack : def.consumables) {

                        boolean found = false;

                        int amount = 0;

                        for (ItemStack tStack : player.getInventory().getContents()) {
                            if (ItemUtil.areItemsIdentical(stack, tStack)) {

                                amount += tStack.getAmount();

                                if (amount >= stack.getAmount()) {
                                    found = true;
                                    break;
                                }
                            }
                        }

                        if (!found) {
                            if (!def.missingConsumableMessage.isEmpty()) {
                                lplayer.printError(lplayer.translate(def.missingConsumableMessage).replace("%item%", stack.getAmount() + " " + stack.getType().name()));
                            }
                            break current;
                        }
                    }

                    for (ItemStack stack : def.consumables) {

                        boolean found = false;

                        int amount = stack.getAmount();

                        for (int i = 0; i < player.getInventory().getContents().length; i++) {
                            ItemStack tStack = player.getInventory().getContents()[i];
                            if (ItemUtil.areItemsIdentical(stack, tStack)) {
                                ItemStack toRemove = tStack.clone();
                                if (toRemove.getAmount() > amount) {

                                    toRemove.setAmount(toRemove.getAmount() - amount);
                                    player.getInventory().setItem(i, toRemove);
                                    amount = 0;
                                } else {
                                    amount -= toRemove.getAmount();
                                    player.getInventory().setItem(i, null);
                                }
                                if (amount <= 0) {
                                    found = true;
                                    break;
                                }
                            }
                        }

                        if (!found) {
                            lplayer.printError("mech.command-items.out-of-sync");
                            break current;
                        }
                    }

                    if (def.consumeSelf) {
                        if (event instanceof PlayerInteractEvent && ((PlayerInteractEvent) event).getHand() == EquipmentSlot.OFF_HAND
                                || event instanceof BlockPlaceEvent && ((BlockPlaceEvent) event).getHand() == EquipmentSlot.OFF_HAND) {
                            if (player.getInventory().getItemInOffHand().getAmount() > 1) {
                                player.getInventory().getItemInOffHand().setAmount(player.getInventory().getItemInOffHand().getAmount() - 1);
                            } else {
                                player.getInventory().setItemInOffHand(null);
                            }
                        } else if (event instanceof EntityPickupItemEvent) {
                            ((EntityPickupItemEvent) event).getItem().remove();
                            ((EntityPickupItemEvent) event).setCancelled(true);
                        } else {
                            if (player.getInventory().getItemInMainHand().getAmount() > 1) {
                                player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                            } else {
                                player.getInventory().setItemInMainHand(null);
                            }
                        }
                    }

                    player.updateInventory();
                }


                for(String command : comdef.commands)
                    doCommand(command, event, comdef, player);

                for(CommandItemAction action : comdef.actions)
                    if(action.stage == ActionRunStage.AFTER)
                        action.runAction(comdef, event, player);

                if(comdef.cooldown > 0 && !lplayer.hasPermission("craftbook.mech.commanditems.bypasscooldown"))
                    cooldownPeriods.put(new Tuple2<>(lplayer.getName(), comdef.name), comdef.cooldown);

                if(comdef.delayedCommands.length > 0)
                    Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), () -> {
                        for(String command : comdef.delayedCommands)
                            doCommand(command, event, comdef, player);
                    }, comdef.delay);
            }
        }
        }
    }

    public static void doCommand(String command, Event event, CommandItemDefinition comdef, Player player) {

        if(command == null || command.trim().isEmpty())
            return;

        command = parseLine(command, event, player);

        if(comdef.type == CommandType.CONSOLE)
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        else if (comdef.type == CommandType.PLAYER) {
            if (comdef.fakeCommand) {
                ProtectionUtil.canSendCommand(player, command);
            } else {
                Bukkit.dispatchCommand(player, command);
            }
        } else if (comdef.type == CommandType.SUPERUSER) {
            PermissionAttachment att = player.addAttachment(CraftBookPlugin.inst());
            att.setPermission("*", true);
            boolean wasOp = player.isOp();
            if(!wasOp)
                player.setOp(true);
            try {
                if (comdef.fakeCommand) {
                    ProtectionUtil.canSendCommand(player, command);
                } else {
                    Bukkit.dispatchCommand(player, command);
                }
            } finally {
                att.remove();
                if(!wasOp)
                    player.setOp(false);
            }
        }
    }

    static String parseLine(String command, Event event, Player player) {
        if(command == null) return null;

        if(event instanceof EntityDamageByEntityEvent) {
            command = StringUtils.replace(command, "@d.x", String.valueOf(((EntityDamageByEntityEvent) event).getEntity().getLocation().getX()));
            command = StringUtils.replace(command, "@d.y", String.valueOf(((EntityDamageByEntityEvent) event).getEntity().getLocation().getY()));
            command = StringUtils.replace(command, "@d.z", String.valueOf(((EntityDamageByEntityEvent) event).getEntity().getLocation().getZ()));
            command = StringUtils.replace(command, "@d.bx", String.valueOf(((EntityDamageByEntityEvent) event).getEntity().getLocation().getBlockX()));
            command = StringUtils.replace(command, "@d.by", String.valueOf(((EntityDamageByEntityEvent) event).getEntity().getLocation().getBlockY()));
            command = StringUtils.replace(command, "@d.bz", String.valueOf(((EntityDamageByEntityEvent) event).getEntity().getLocation().getBlockZ()));
            command = StringUtils.replace(command, "@d.w", ((EntityDamageByEntityEvent) event).getEntity().getLocation().getWorld().getName());
            command = StringUtils.replace(command, "@d.l", ((EntityDamageByEntityEvent) event).getEntity().getLocation().toString());
            command = StringUtils.replace(command, "@d.u", ((EntityDamageByEntityEvent) event).getEntity().getUniqueId().toString());
            if(((EntityDamageByEntityEvent) event).getEntity() instanceof Player) {
                command = StringUtils.replace(command, "@d.i", CraftBookPlugin.inst().getUUIDMappings().getCBID(((EntityDamageByEntityEvent) event).getEntity().getUniqueId()));
                command = StringUtils.replace(command, "@d", ((EntityDamageByEntityEvent) event).getEntity().getName());
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
            command = StringUtils.replace(command, "@d.w", ((PlayerInteractEntityEvent) event).getRightClicked().getLocation().getWorld().getName());
            command = StringUtils.replace(command, "@d.l", ((PlayerInteractEntityEvent) event).getRightClicked().getLocation().toString());
            command = StringUtils.replace(command, "@d.u", ((PlayerInteractEntityEvent) event).getRightClicked().getUniqueId().toString());
            if(((PlayerInteractEntityEvent) event).getRightClicked() instanceof Player) {
                command = StringUtils.replace(command, "@d.i", CraftBookPlugin.inst().getUUIDMappings().getCBID(((PlayerInteractEntityEvent) event).getRightClicked().getUniqueId()));
                command = StringUtils.replace(command, "@d", ((PlayerInteractEntityEvent) event).getRightClicked().getName());
            } else
                command = StringUtils.replace(command, "@d", ((PlayerInteractEntityEvent) event).getRightClicked().getType().name());
        }
        if(event instanceof BlockEvent) {
            command = StringUtils.replace(command, "@b.x", String.valueOf(((BlockEvent) event).getBlock().getX()));
            command = StringUtils.replace(command, "@b.y", String.valueOf(((BlockEvent) event).getBlock().getY()));
            command = StringUtils.replace(command, "@b.z", String.valueOf(((BlockEvent) event).getBlock().getZ()));
            command = StringUtils.replace(command, "@b.w", ((BlockEvent) event).getBlock().getLocation().getWorld().getName());
            command = StringUtils.replace(command, "@b.l", ((BlockEvent) event).getBlock().getLocation().toString());
            command = StringUtils.replace(command, "@b.d", String.valueOf(((BlockEvent) event).getBlock().getData()));
            command = StringUtils.replace(command, "@b.t", ((BlockEvent) event).getBlock().getType().getKey().toString());
            command = StringUtils.replace(command, "@b", ((BlockEvent) event).getBlock().getType().name());
        }
        if(event instanceof PlayerInteractEvent && ((PlayerInteractEvent) event).getClickedBlock() != null) {
            command = StringUtils.replace(command, "@b.x", String.valueOf(((PlayerInteractEvent) event).getClickedBlock().getX()));
            command = StringUtils.replace(command, "@b.y", String.valueOf(((PlayerInteractEvent) event).getClickedBlock().getY()));
            command = StringUtils.replace(command, "@b.z", String.valueOf(((PlayerInteractEvent) event).getClickedBlock().getZ()));
            command = StringUtils.replace(command, "@b.w", ((PlayerInteractEvent) event).getClickedBlock().getWorld().getName());
            command = StringUtils.replace(command, "@b.l", ((PlayerInteractEvent) event).getClickedBlock().getLocation().toString());
            command = StringUtils.replace(command, "@b.d", String.valueOf(((PlayerInteractEvent) event).getClickedBlock().getData()));
            command = StringUtils.replace(command, "@b.t", ((PlayerInteractEvent) event).getClickedBlock().getType().getKey().toString());
            command = StringUtils.replace(command, "@b", ((PlayerInteractEvent) event).getClickedBlock().getType().name());
        }
        if(event instanceof EntityEvent && ((EntityEvent) event).getEntityType() != null && command.contains("@e")) {
            command = StringUtils.replace(command, "@e.x", String.valueOf(((EntityEvent) event).getEntity().getLocation().getX()));
            command = StringUtils.replace(command, "@e.y", String.valueOf(((EntityEvent) event).getEntity().getLocation().getY()));
            command = StringUtils.replace(command, "@e.z", String.valueOf(((EntityEvent) event).getEntity().getLocation().getZ()));
            command = StringUtils.replace(command, "@e.bx", String.valueOf(((EntityEvent) event).getEntity().getLocation().getBlockX()));
            command = StringUtils.replace(command, "@e.by", String.valueOf(((EntityEvent) event).getEntity().getLocation().getBlockY()));
            command = StringUtils.replace(command, "@e.bz", String.valueOf(((EntityEvent) event).getEntity().getLocation().getBlockZ()));
            command = StringUtils.replace(command, "@e.w", ((EntityEvent) event).getEntity().getLocation().getWorld().getName());
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
