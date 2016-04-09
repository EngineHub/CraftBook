package com.sk89q.craftbook.mechanics;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.*;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.blocks.BlockType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.material.Directional;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author Me4502
 */
public class Chair extends AbstractCraftBookMechanic {

    public Map<String, Tuple2<Entity, Block>> chairs;

    public void addChair(final Player player, Block block) {

        Entity ar = null;
        if(chairs.containsKey(player.getName()))
            ar = chairs.get(player.getName()).a;
        boolean isNew = false;

        if(ar == null || !ar.isValid() || ar.isDead() || !ar.getLocation().getBlock().equals(block)) {
            if(ar != null && !ar.getLocation().getBlock().equals(block))
                ar.remove();
            ar = block.getWorld().spawnArrow(BlockUtil.getBlockCentre(block).subtract(0, 0.5, 0), new Vector(0,-0.1,0), 0.01f, 0);
            isNew = true;
        }
        if (!chairs.containsKey(player.getName()))
            CraftBookPlugin.inst().wrapPlayer(player).print("mech.chairs.sit");
        // Attach the player to said arrow.
        final Entity far = ar;
        if(ar.isEmpty() && isNew) {
            Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), new Runnable() {
                @Override
                public void run () {
                    far.setPassenger(player);
                }
            });
        } else if (ar.isEmpty()) {
            removeChair(player);
            return;
        }

        ar.setTicksLived(1);

        chairs.put(player.getName(), new Tuple2<Entity, Block>(ar, block));
    }

    public void removeChair(final Player player) {

        CraftBookPlugin.inst().wrapPlayer(player).print("mech.chairs.stand");
        final Entity ent = chairs.get(player.getName()).a;
        final Block block = chairs.get(player.getName()).b;
        if(ent != null) {
            player.eject();
            player.teleport(block.getLocation().add(0, 1, 0));
            ent.remove();
            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new Runnable() {
                @Override
                public void run () {
                    player.teleport(block.getLocation().add(0, 1, 0));
                    player.setSneaking(false);
                }
            }, 1L);
        }
        chairs.remove(player.getName());
    }

    public boolean hasSign(Block block, List<Location> searched, Block original) {

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

    public Tuple2<Entity, Block> getChair(Player player) {

        return chairs.get(player.getName());
    }

    public boolean hasChair(Player player) {

        return chairs.containsKey(player.getName());
    }

    public boolean hasChair(Block player) {

        for(Tuple2<Entity, Block> tup : chairs.values())
            if(player.equals(tup.b))
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
    public void onRightClick(PlayerInteractEvent event) {

        if (!EventUtil.passesFilter(event) && event.getHand() != EquipmentSlot.HAND)
            return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null || !chairBlocks.contains(new ItemInfo(event.getClickedBlock())))
            return;

        LocalPlayer lplayer = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if (lplayer.isSneaking()) return;
        Player player = event.getPlayer();

        // Now everything looks good, continue;
        if (chairAllowHeldBlock || !lplayer.isHoldingBlock() && lplayer.getHeldItemInfo().getType() != Material.SIGN || lplayer.getHeldItemInfo().getType() == Material.AIR) {
            if (chairRequireSign && !hasSign(event.getClickedBlock(), new ArrayList<Location>(), event.getClickedBlock()))
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
                if (BlockType.canPassThrough(event.getClickedBlock().getRelative(0, -1, 0).getTypeId())) {

                    lplayer.printError("mech.chairs.floating");
                    return;
                } else if(!BlockType.canPassThrough(event.getClickedBlock().getRelative(0, 1, 0).getTypeId())) {

                    lplayer.printError("mech.chairs.obstructed");
                    return;
                }

                Location chairLoc = event.getClickedBlock().getLocation().add(0.5,0,0.5);

                if(chairFacing && event.getClickedBlock().getState().getData() instanceof Directional) {

                    BlockFace direction = ((Directional) event.getClickedBlock().getState().getData()).getFacing();

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
                player.getPlayer().teleport(chairLoc);
                addChair(player.getPlayer(), event.getClickedBlock());
                event.setCancelled(true);
            }
        }
    }

    public class ChairChecker implements Runnable {

        @Override
        public void run() {

            for (String pl : chairs.keySet()) {
                Player p = Bukkit.getPlayerExact(pl);
                if (p == null  || p.isDead()) {
                    chairs.remove(pl);
                    continue;
                }

                if (!chairBlocks.contains(new ItemInfo(getChair(p).b)) || !p.getWorld().equals(getChair(p).b.getWorld()) || LocationUtil.getDistanceSquared(p.getLocation(), getChair(p).b.getLocation()) > 1.5)
                    removeChair(p);
                else {
                    addChair(p, getChair(p).b); // For any new players.

                    if (chairHealth && p.getHealth() < p.getMaxHealth())
                        p.setHealth(Math.min(p.getHealth() + chairHealAmount, p.getMaxHealth()));
                    if (p.getExhaustion() > -20d) p.setExhaustion((float)(p.getExhaustion() - 0.1d));
                }
            }
        }
    }

    @Override
    public boolean enable () {

        chairs = new ConcurrentHashMap<String, Tuple2<Entity, Block>>();

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
        } catch(Throwable e) {
        }
    }

    boolean chairAllowHeldBlock;
    boolean chairHealth;
    double chairHealAmount;
    List<ItemInfo> chairBlocks;
    boolean chairFacing;
    boolean chairRequireSign;
    int chairMaxDistance;
    int chairMaxClickRadius;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "allow-holding-blocks", "Allow players to sit in chairs when holding blocks.");
        chairAllowHeldBlock = config.getBoolean(path + "allow-holding-blocks", false);

        config.setComment(path + "regen-health", "Regenerate health passively when sitting down.");
        chairHealth = config.getBoolean(path + "regen-health", true);

        config.setComment(path + "regen-health-amount", "The amount of health regenerated passively. (Can be decimal)");
        chairHealAmount = config.getDouble(path + "regen-health-amount", 1);

        config.setComment(path + "blocks", "A list of blocks that can be sat on.");
        chairBlocks = ItemInfo.parseListFromString(config.getStringList(path + "blocks", Arrays.asList("WOOD_STAIRS", "COBBLESTONE_STAIRS", "BRICK_STAIRS", "SMOOTH_STAIRS", "NETHER_BRICK_STAIRS", "SANDSTONE_STAIRS", "SPRUCE_WOOD_STAIRS", "BIRCH_WOOD_STAIRS", "JUNGLE_WOOD_STAIRS", "QUARTZ_STAIRS", "ACACIA_STAIRS")));

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