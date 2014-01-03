package com.sk89q.craftbook.mech;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Button;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.Location;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockType;

/**
 * Teleporter Mechanism. Based off Elevator
 *
 * @author sk89q
 * @author hash
 * @author Me4502
 */
public class Teleporter extends AbstractCraftBookMechanic {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        if (!event.getLine(1).equalsIgnoreCase("[Teleporter]")) return;

        LocalPlayer localPlayer = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if(!localPlayer.hasPermission("craftbook.mech.teleporter")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                localPlayer.printError("mech.create-permission");
            SignUtil.cancelSign(event);
            return;
        }

        String[] pos = RegexUtil.COLON_PATTERN.split(event.getLine(2));
        if (pos.length <= 2) {
            localPlayer.printError("mech.teleport.invalidcoords");
            SignUtil.cancelSign(event);
            return;
        }

        localPlayer.print("mech.teleport.create");
        event.setLine(1, "[Teleporter]");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onRightClick(PlayerInteractEvent event) {

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        LocalPlayer localPlayer = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        Block trigger = null;

        if (SignUtil.isSign(event.getClickedBlock())) {
            ChangedSign s = BukkitUtil.toChangedSign(event.getClickedBlock());
            if (!s.getLine(1).equals("[Teleporter]")) return;
            String[] pos = RegexUtil.COLON_PATTERN.split(s.getLine(2));
            if (pos.length <= 2) {
                localPlayer.printError("mech.teleport.invalidcoords");
                return;
            }
            trigger = event.getClickedBlock();
        } else if (event.getClickedBlock().getType() == Material.STONE_BUTTON || event.getClickedBlock().getType() == Material.WOOD_BUTTON) {
            Button b = (Button) event.getClickedBlock().getState().getData();
            if(b == null || b.getAttachedFace() == null) return;
            Block sign = event.getClickedBlock().getRelative(b.getAttachedFace(), 2);
            if (SignUtil.isSign(sign)) {
                ChangedSign s = BukkitUtil.toChangedSign(sign);
                if (!s.getLine(1).equals("[Teleporter]")) return;
                String[] pos = RegexUtil.COLON_PATTERN.split(s.getLine(2));
                if (pos.length <= 2) {
                    localPlayer.printError("mech.teleport.invalidcoords");
                    return;
                }
                trigger = sign;
            }
        } else
            return;

        if(trigger == null) return;

        if (!localPlayer.hasPermission("craftbook.mech.teleporter.use")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                localPlayer.printError("mech.use-permission");
            return;
        }

        makeItSo(localPlayer, trigger);

        event.setCancelled(true);
    }

    private void makeItSo(LocalPlayer player, Block trigger) {
        // start with the block shifted vertically from the player
        // to the destination sign's height (plus one).
        // check if this looks at all like something we're interested in first

        double toX = 0;
        double toY = 0;
        double toZ = 0;

        if (SignUtil.isSign(trigger)) {
            ChangedSign s = BukkitUtil.toChangedSign(trigger);
            String[] pos = RegexUtil.COLON_PATTERN.split(s.getLine(2));
            if (pos.length > 2) {
                try {
                    toX = Double.parseDouble(pos[0]);
                    toY = Double.parseDouble(pos[1]);
                    toZ = Double.parseDouble(pos[2]);
                } catch (Exception e) {
                    player.printError("mech.teleport.arriveonly");
                    return;
                }
            } else {
                player.printError("mech.teleport.arriveonly");
                return;
            }
        }

        if (CraftBookPlugin.inst().getConfiguration().teleporterRequireSign) {
            Block location = trigger.getWorld().getBlockAt((int) toX, (int) toY, (int) toZ);
            if (SignUtil.isSign(location)) {
                if (!checkTeleportSign(player, location)) {
                    return;
                }
            } else if (location.getType() == Material.STONE_BUTTON || location.getType() == Material.WOOD_BUTTON) {
                Button b = (Button) location.getState().getData();
                Block sign = location.getRelative(b.getAttachedFace()).getRelative(b.getAttachedFace());
                if (!checkTeleportSign(player, sign)) {
                    return;
                }
            } else {
                player.printError("mech.teleport.sign");
                return;
            }
        }

        Block floor = trigger.getWorld().getBlockAt((int) Math.floor(toX), (int) (Math.floor(toY) + 1),
                (int) Math.floor(toZ));
        // well, unless that's already a ceiling.
        if (!BlockType.canPassThrough(floor.getTypeId()))
            floor = floor.getRelative(BlockFace.DOWN);

        // now iterate down until we find enough open space to stand in
        // or until we're 5 blocks away, which we consider too far.
        int foundFree = 0;
        for (int i = 0; i < 5; i++) {
            if (BlockType.canPassThrough(floor.getTypeId()))
                foundFree++;
            else
                break;
            if (floor.getY() == 0x0) break;
            floor = floor.getRelative(BlockFace.DOWN);
        }
        if (foundFree < 2) {
            player.printError("mech.teleport.obstruct");
            return;
        }

        // Teleport!
        Location subspaceRift = player.getPosition();
        subspaceRift = subspaceRift.setPosition(new Vector(floor.getX() + 0.5, floor.getY() + 1, floor.getZ() + 0.5));
        if (player.isInsideVehicle()) {
            subspaceRift = player.getVehicle().getLocation();
            subspaceRift = subspaceRift.setPosition(new Vector(floor.getX() + 0.5, floor.getY() + 2, floor.getZ() + 0.5));
            player.getVehicle().teleport(subspaceRift);
        }
        if (CraftBookPlugin.inst().getConfiguration().teleporterMaxRange > 0)
            if (subspaceRift.getPosition().distanceSq(player.getPosition().getPosition()) > CraftBookPlugin.inst().getConfiguration().teleporterMaxRange * CraftBookPlugin.inst().getConfiguration().teleporterMaxRange) {
                player.print("mech.teleport.range");
                return;
            }

        player.teleport(subspaceRift);

        player.print("mech.teleport.alert");
    }

    private boolean checkTeleportSign(LocalPlayer player, Block sign) {

        if (!SignUtil.isSign(sign)) {
            player.printError("mech.teleport.sign");
            return false;
        }

        ChangedSign s = BukkitUtil.toChangedSign(sign);
        if (!s.getLine(1).equals("[Teleporter]")) {
            player.printError("mech.teleport.sign");
            return false;
        }

        return true;
    }
}
