package com.sk89q.craftbook.mech;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.ProtectionUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.events.SignClickEvent;

public class MapChanger extends AbstractCraftBookMechanic {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        if(!event.getLine(1).equalsIgnoreCase("[map]")) return;
        LocalPlayer lplayer = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if(!lplayer.hasPermission("craftbook.mech.map")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                lplayer.printError("You don't have permission for this.");
            SignUtil.cancelSign(event);
            return;
        }

        lplayer.print("mech.map.create");

        event.setLine(1, "[Map]");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSignClick(SignClickEvent event) {

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ChangedSign sign = event.getSign();
        if(!sign.getLine(1).equalsIgnoreCase("[map]")) return;

        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if (!player.hasPermission("craftbook.mech.map.use")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.use-permission");
            return;
        }

        if(!ProtectionUtil.canUse(event.getPlayer(), event.getClickedBlock().getLocation(), event.getBlockFace(), event.getAction())) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("area.use-permissions");
            return;
        }
        if (event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getType() == Material.MAP) {
            byte id;
            try {
                id = Byte.parseByte(sign.getLine(2));
            } catch (Exception e) {
                id = -1;
            }
            if (id <= -1) {
                player.printError("mech.map.invalid");
                return;
            }
            event.getPlayer().getItemInHand().setDurability(id);
        }
    }
}