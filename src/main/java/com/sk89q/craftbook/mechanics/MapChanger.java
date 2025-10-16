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
import org.bukkit.inventory.ItemStack;
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

        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (item == null 
            || (item.getType() != Material.MAP && item.getType() != Material.FILLED_MAP)) {
            return;
        }

        final int mapId;
        try {
            mapId = Integer.parseInt(sign.getLine(2).trim());
        } catch (Exception e) {
            player.printError("mech.map.invalid");
            return;
        }

        if (item.getType() == Material.MAP) {
            ItemStack filled = new ItemStack(Material.FILLED_MAP, item.getAmount());
            MapMeta meta = (MapMeta) filled.getItemMeta();
            meta.setMapId(mapId);
            filled.setItemMeta(meta);
            event.getPlayer().getInventory().setItemInMainHand(filled);
        } else {
            MapMeta meta = (MapMeta) item.getItemMeta();
            meta.setMapId(mapId);
            item.setItemMeta(meta);
        }
    }

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

    }
}