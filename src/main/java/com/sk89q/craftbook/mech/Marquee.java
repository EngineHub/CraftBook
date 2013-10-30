package com.sk89q.craftbook.mech;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.commands.VariableCommands;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.events.SignClickEvent;

public class Marquee extends AbstractCraftBookMechanic {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSignClick(SignClickEvent event) {

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ChangedSign sign = event.getSign();
        if(!sign.getLine(1).equals("[Marquee]")) return;
        LocalPlayer lplayer = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if(!lplayer.hasPermission("craftbook.mech.marquee.use")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                lplayer.printError("mech.use-permission");
            return;
        }

        String var = CraftBookPlugin.inst().getVariable(sign.getLine(2), sign.getLine(3).isEmpty() ? "global" : sign.getLine(3));
        if(var == null || var.isEmpty()) var = "Missing Variable!";
        lplayer.print(var);

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        if(!event.getLine(1).equalsIgnoreCase("[marquee]")) return;
        LocalPlayer lplayer = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if(!lplayer.hasPermission("craftbook.mech.marquee")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                lplayer.printError("You don't have permission for this.");
            SignUtil.cancelSign(event);
            return;
        }

        String namespace = event.getLine(3).isEmpty() ? "global" : event.getLine(3);
        String variable = event.getLine(2);

        if(!VariableCommands.hasVariablePermission(event.getPlayer(), namespace, variable, "get")) {
            lplayer.printError("You don't have permission to use that variable!");
            SignUtil.cancelSign(event);
        }

        String var = CraftBookPlugin.inst().getVariable(variable, namespace);
        if(var == null || var.isEmpty()) {
            lplayer.printError("Missing Variable!");
            SignUtil.cancelSign(event);
        }

        event.setLine(1, "[Marquee]");
    }
}