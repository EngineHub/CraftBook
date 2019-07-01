package com.sk89q.craftbook.mechanics;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.BlockSyntax;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.ProtectionUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.blocks.Blocks;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.item.ItemTypes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * @author Me4502
 */
public class Chair extends AbstractCraftBookMechanic {

    private Map<String, ChairData> chairs;

    private static Entity fixArrow(Block block, Entity chairEntity) {
        if(chairEntity == null || !chairEntity.isValid() || chairEntity.isDead()) {
            chairEntity = block.getWorld().spawn(BlockUtil.getBlockCentre(block).subtract(0, 1.5, 0), ArmorStand.class);
            //block.getWorld().spawnArrow(BlockUtil.getBlockCentre(block).subtract(0, 0.5, 0), new Vector(0,-0.1,0), 0.01f, 0);
        }

        chairEntity.setTicksLived(1);
        chairEntity.setInvulnerable(true);
        chairEntity.setGravity(false);
        chairEntity.setSilent(true);

        if (chairEntity instanceof ArmorStand)
            ((ArmorStand) chairEntity).setVisible(false);

        return chairEntity;
    }

    private void addChair(final Player player, Block block, final Location chairLoc) {
        Entity ar = null;
        boolean hasUpdated = false;
        boolean isNew = false;
        if(chairs.containsKey(player.getName())) {
            ar = chairs.get(player.getName()).chairEntity;
            hasUpdated = true;
        } else {
            isNew = true;
        }

        ar = fixArrow(block, ar);
        if (chairLoc != null)
            ar.getLocation().setYaw(chairLoc.getYaw());

        if (hasUpdated && ar != chairs.get(player.getName()).chairEntity)
            isNew = true;

        if (!chairs.containsKey(player.getName()))
            CraftBookPlugin.inst().wrapPlayer(player).print("mech.chairs.sit");

        // Attach the player to said arrow.
        final Entity far = ar;
        if(ar.isEmpty() && isNew) {
            Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), () -> {
                if (chairLoc != null)
                    player.teleport(chairLoc);
                far.addPassenger(player);
            });
        } else if (ar.isEmpty()) {
            removeChair(player);
            return;
        }

        ChairData chairData;
        if (chairs.containsKey(player.getName())) {
            chairData = chairs.get(player.getName());
            chairData.chairEntity = ar;
        } else {
            chairData = new ChairData(ar, block, player.getLocation().clone());
        }

        chairs.put(player.getName(), chairData);
    }

    private void removeChair(final Player player) {
        CraftBookPlugin.inst().wrapPlayer(player).print("mech.chairs.stand");
        final ChairData chairData = chairs.get(player.getName());
        final Entity ent = chairData.chairEntity;
        if(ent != null) {
            ent.eject();
            player.eject();
            ent.remove();
            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), () -> {
                player.teleport(chairData.playerExitPoint);
                player.setSneaking(false);
            }, 5L);
        }
        chairs.remove(player.getName());
    }

    private boolean hasSign(Block block, List<Location> searched, Block original) {
        boolean found = false;

        for (BlockFace face : LocationUtil.getDirectFaces()) {
            Block otherBlock = block.getRelative(face);

            if(searched.contains(otherBlock.getLocation())) continue;
            searched.add(otherBlock.getLocation());

            if (found) break;

            if(block.getLocation().distanceSquared(original.getLocation()) > Math.pow(chairMaxDistance, 2)) continue;

            if (SignUtil.isSign(otherBlock) && SignUtil.getFront(otherBlock) == face) {
                found = true;
                break;
            }

            if (BlockUtil.areBlocksIdentical(block, otherBlock))
                found = hasSign(otherBlock, searched, original);
        }

        return found;
    }

    private ChairData getChair(Player player) {
        return chairs.get(player.getName());
    }

    private boolean hasChair(Player player) {
        return player != null && chairs.containsKey(player.getName());
    }

    private boolean hasChair(Block block) {
        for(ChairData data : chairs.values())
            if(block.equals(data.location))
                return true;

        return false;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if(!EventUtil.passesFilter(event)) return;

        if (hasChair(event.getBlock())) {
            event.setCancelled(true);
            CraftBookPlugin.inst().wrapPlayer(event.getPlayer()).printError("mech.chairs.in-use");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDisconnect(PlayerQuitEvent event) {
        if (hasChair(event.getPlayer())) {
            removeChair(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onKick(PlayerKickEvent event) {
        if (hasChair(event.getPlayer())) {
            removeChair(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(PlayerInteractEvent event) {
        if (!EventUtil.passesFilter(event) || event.getHand() != EquipmentSlot.HAND)
            return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null || !Blocks.containsFuzzy(chairBlocks, BukkitAdapter.adapt(event.getClickedBlock().getBlockData())))
            return;

        CraftBookPlayer lplayer = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if (lplayer.isSneaking()) return;
        Player player = event.getPlayer();

        // Now everything looks good, continue;
        if (chairAllowHeldBlock || !lplayer.isHoldingBlock() && lplayer.getItemInHand(HandSide.MAIN_HAND).getType() != ItemTypes.SIGN || lplayer.getItemInHand(HandSide.MAIN_HAND).getType() == ItemTypes.AIR) {
            if (chairRequireSign && !hasSign(event.getClickedBlock(), new ArrayList<>(), event.getClickedBlock()))
                return;
            if (!lplayer.hasPermission("craftbook.mech.chair.use")) {
                if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                    lplayer.printError("mech.use-permission");
                return;
            }
            if(!ProtectionUtil.canUse(event.getPlayer(), event.getClickedBlock().getLocation(), event.getBlockFace(), event.getAction())) {
                if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                    lplayer.printError("area.use-permissions");
                return;
            }

            if(event.getPlayer().getLocation().distanceSquared(event.getClickedBlock().getLocation().add(0.5, 0.5, 0.5)) > Math.pow(chairMaxClickRadius, 2)) {
                lplayer.printError("mech.chairs.too-far");
                return;
            }

            if (hasChair(player.getPlayer())) { // Stand
                removeChair(player.getPlayer());
            } else { // Sit
                if (hasChair(event.getClickedBlock())) {
                    lplayer.print("mech.chairs.in-use");
                    return;
                }
                if (!event.getClickedBlock().getRelative(0, -1, 0).getType().isSolid()) {
                    lplayer.printError("mech.chairs.floating");
                    return;
                } else if(event.getClickedBlock().getRelative(0, 1, 0).getType().isSolid()) {
                    lplayer.printError("mech.chairs.obstructed");
                    return;
                }

                Location chairLoc = event.getClickedBlock().getLocation().add(0.5,0,0.5);

                BlockData blockData = event.getClickedBlock().getBlockData();
                if(chairFacing && blockData instanceof Directional) {
                    BlockFace direction = ((Directional) blockData).getFacing().getOppositeFace();

                    double dx = direction.getModX();
                    double dy = direction.getModY();
                    double dz = direction.getModZ();

                    if (dx != 0) {
                        if (dx < 0)
                            chairLoc.setYaw((float) (1.5 * Math.PI));
                        else
                            chairLoc.setYaw((float) (0.5 * Math.PI));
                        chairLoc.setYaw((float)(chairLoc.getYaw() - Math.atan(dz / dx)));
                    } else if (dz < 0)
                        chairLoc.setYaw((float) Math.PI);

                    double dxz = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));

                    chairLoc.setPitch((float) -Math.atan(dy / dxz));

                    chairLoc.setYaw(-chairLoc.getYaw() * 180f / (float) Math.PI);
                    chairLoc.setPitch(chairLoc.getPitch() * 180f / (float) Math.PI);
                } else {
                    chairLoc.setPitch(player.getPlayer().getLocation().getPitch());
                    chairLoc.setYaw(player.getPlayer().getLocation().getYaw());
                }
                addChair(player.getPlayer(), event.getClickedBlock(), chairLoc);
                event.setCancelled(true);
            }
        }
    }

    private class ChairChecker implements Runnable {

        @Override
        public void run() {
            for (Map.Entry<String, ChairData> pl : chairs.entrySet()) {
                Player p = Bukkit.getPlayerExact(pl.getKey());
                if (p == null  || p.isDead() || !p.isValid()) {
                    ChairData data = chairs.remove(pl.getKey());
                    if (data != null && data.chairEntity != null) {
                        Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), () -> {
                            data.chairEntity.eject();
                            data.chairEntity.remove();
                        }, 5);
                    }
                    continue;
                }

                if (!Blocks.containsFuzzy(chairBlocks, BukkitAdapter.adapt(pl.getValue().location.getBlockData())) || !p.getWorld().equals(pl.getValue().location.getWorld()) || LocationUtil.getDistanceSquared(p.getLocation(), pl.getValue().location.getLocation()) > 2)
                    removeChair(p);
                else {
                    addChair(p, pl.getValue().location, null); // For any new players.

                    if (chairHealth && p.getHealth() < p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue())
                        p.setHealth(Math.min(p.getHealth() + chairHealAmount, p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
                    if (p.getExhaustion() > -20d) p.setExhaustion((float)(p.getExhaustion() - 0.1d));
                }
            }
        }
    }

    private static final class ChairData {
        private Entity chairEntity;
        private Block location;
        private Location playerExitPoint;

        ChairData(Entity entity, Block location, Location playerExitPoint) {
            this.chairEntity = entity;
            this.location = location;
            this.playerExitPoint = playerExitPoint;
        }
    }

    @Override
    public boolean enable () {

        chairs = new ConcurrentHashMap<>();

        Bukkit.getScheduler().runTaskTimer(CraftBookPlugin.inst(), new ChairChecker(), 20L, 20L);

        try {
            Class.forName("com.comphenix.protocol.events.PacketListener");
            ProtocolLibrary.getProtocolManager().getAsynchronousManager().registerAsyncHandler(new PacketAdapter(PacketAdapter.params(CraftBookPlugin.inst(), PacketType.Play.Client.STEER_VEHICLE).clientSide().listenerPriority(ListenerPriority.HIGHEST).options(ListenerOptions.INTERCEPT_INPUT_BUFFER)) {
                @Override
                public void onPacketReceiving(PacketEvent e) {
                    if (!e.isCancelled()) {
                        Player player = e.getPlayer();
                        if (e.getPacket().getBooleans().getValues().get(1))
                            if(hasChair(player))
                                removeChair(player);
                    }
                }
            }).syncStart();

            ProtocolLibrary.getProtocolManager().getAsynchronousManager().registerAsyncHandler(new PacketAdapter(PacketAdapter.params(CraftBookPlugin.inst(), PacketType.Play.Client.ENTITY_ACTION).clientSide().listenerPriority(ListenerPriority.HIGHEST).options(ListenerOptions.INTERCEPT_INPUT_BUFFER)) {
                @Override
                public void onPacketReceiving(PacketEvent e) {
                    if (!e.isCancelled()) {
                        Player player = e.getPlayer();
                        if(hasChair(player))
                            removeChair(player);
                    }
                }
            }).syncStart();

            ProtocolLibrary.getProtocolManager().getAsynchronousManager().registerAsyncHandler(new PacketAdapter(PacketAdapter.params(CraftBookPlugin.inst(), PacketType.Play.Client.POSITION, PacketType.Play.Client.POSITION_LOOK).clientSide().listenerPriority(ListenerPriority.HIGHEST).options(ListenerOptions.INTERCEPT_INPUT_BUFFER)) {
                @Override
                public void onPacketReceiving(PacketEvent e) {
                    if (!e.isCancelled()) {
                        for(double d : e.getPacket().getDoubles().getValues())
                            if(Double.isNaN(d)) {
                                e.setCancelled(true);
                                return;
                            }
                    }
                }
            }).syncStart();
        } catch(Throwable e) {
            CraftBookPlugin.inst().getLogger().warning("ProtocolLib is required for chairs! Disabling chairs!");
            return false;
        }

        return true;
    }

    @Override
    public void disable () {
        chairs = null;
        try {
            ProtocolLibrary.getProtocolManager().getAsynchronousManager().unregisterAsyncHandlers(CraftBookPlugin.inst());
        } catch(Throwable ignored) {
        }
    }

    private boolean chairAllowHeldBlock;
    private boolean chairHealth;
    private double chairHealAmount;
    private List<BaseBlock> chairBlocks;
    private boolean chairFacing;
    private boolean chairRequireSign;
    private int chairMaxDistance;
    private int chairMaxClickRadius;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "allow-holding-blocks", "Allow players to sit in chairs when holding blocks.");
        chairAllowHeldBlock = config.getBoolean(path + "allow-holding-blocks", false);

        config.setComment(path + "regen-health", "Regenerate health passively when sitting down.");
        chairHealth = config.getBoolean(path + "regen-health", true);

        config.setComment(path + "regen-health-amount", "The amount of health regenerated passively. (Can be decimal)");
        chairHealAmount = config.getDouble(path + "regen-health-amount", 1);

        config.setComment(path + "blocks", "A list of blocks that can be sat on.");
        chairBlocks =
                BlockSyntax.getBlocks(config.getStringList(path + "blocks", BlockCategories.STAIRS.getAll().stream().map(BlockType::getId).sorted(String::compareToIgnoreCase).collect(Collectors.toList())), true);

        config.setComment(path + "face-correct-direction", "When the player sits, automatically face them the direction of the chair. (If possible)");
        chairFacing = config.getBoolean(path + "face-correct-direction", true);

        config.setComment(path + "require-sign", "Require a sign to be attached to the chair in order to work!");
        chairRequireSign = config.getBoolean(path + "require-sign", false);

        config.setComment(path + "max-distance", "The maximum distance between the click point and the sign. (When require sign is on)");
        chairMaxDistance = config.getInt(path + "max-distance", 3);

        config.setComment(path + "max-click-radius", "The maximum distance the player can be from the sign.");
        chairMaxClickRadius = config.getInt(path + "max-click-radius", 5);
    }
}