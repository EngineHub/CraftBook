package com.sk89q.craftbook.mechanics;

import com.sk89q.craftbook.CraftBookPlayer;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ProtectionUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.events.SignClickEvent;
import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.inventory.meta.MapMeta;

public class MapChanger extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(!event.getLine(1).equalsIgnoreCase("[map]")) return;
        CraftBookPlayer lplayer = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if(!lplayer.hasPermission("craftbook.mech.map")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                lplayer.printError("You don't have permission for this.");
            SignUtil.cancelSign(event);
            return;
        }

        lplayer.print("mech.map.create");

        event.setLine(1, "[Map]");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignClick(SignClickEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ChangedSign sign = event.getSign();
        if(!sign.getLine(1).equalsIgnoreCase("[map]")) return;

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
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
        if (event.getPlayer().getInventory().getItemInMainHand() != null && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.MAP) {
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
            MapMeta meta = (MapMeta) event.getPlayer().getInventory().getItemInMainHand().getItemMeta();
            meta.setMapId(id);
            event.getPlayer().getInventory().getItemInMainHand().setItemMeta(meta);
        }
    }

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

    }
}