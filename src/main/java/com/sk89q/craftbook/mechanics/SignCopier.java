package com.sk89q.craftbook.mechanics;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ProtectionUtil;
import com.sk89q.craftbook.util.events.SignClickEvent;

public class SignCopier extends AbstractCraftBookMechanic {

    public static Map<String, String[]> signs;

    @Override
    public boolean enable() {

        signs = new HashMap<String, String[]>();
        return true;
    }

    @Override
    public void disable() {

        signs = null;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRightClick(SignClickEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (!player.getHeldItemInfo().equals(CraftBookPlugin.inst().getConfiguration().signCopyItem)) return;

        if (!player.hasPermission("craftbook.mech.signcopy.use")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.use-permission");
            return;
        }

        if(!ProtectionUtil.canUse(event.getPlayer(), event.getClickedBlock().getLocation(), event.getBlockFace(), event.getAction())) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("area.use-permissions");
            return;
        }

        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            signs.put(player.getName(), ((Sign) event.getClickedBlock().getState()).getLines());
            player.print("mech.signcopy.copy");
            event.setCancelled(true);
        } else if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            if(signs.containsKey(player.getName())) {

                Sign s = (Sign) event.getClickedBlock().getState();
                String[] lines = signs.get(player.getName());

                SignChangeEvent sev = new SignChangeEvent(event.getClickedBlock(), event.getPlayer(), lines);
                Bukkit.getPluginManager().callEvent(sev);

                if(!sev.isCancelled()) {
                    for(int i = 0; i < lines.length; i++)
                        s.setLine(i, lines[i]);
                    s.update();
                }

                player.print("mech.signcopy.paste");
                event.setCancelled(true);
            }
        }
    }
}