package com.sk89q.craftbook.mech;

import org.apache.tools.ant.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.BukkitPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ParsingUtil;
import com.sk89q.craftbook.util.ProtectionUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.events.SignClickEvent;
import com.sk89q.craftbook.util.events.SourcedBlockRedstoneEvent;

public class CommandSigns extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(!event.getLine(1).equalsIgnoreCase("[command]")) return;
        LocalPlayer lplayer = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if(!lplayer.hasPermission("craftbook.mech.command")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                lplayer.printError("mech.create-permission");
            SignUtil.cancelSign(event);
            return;
        }

        event.setLine(1, "[Command]");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(SignClickEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ChangedSign s = event.getSign();

        if(!s.getLine(1).equals("[Command]")) return;

        if(s.getLine(0).equals("EXPANSION")) return;

        LocalPlayer localPlayer = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (!localPlayer.hasPermission("craftbook.mech.command.use")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                localPlayer.printError("mech.use-permission");
            return;
        }

        if(!ProtectionUtil.canUse(event.getPlayer(), event.getClickedBlock().getLocation(), event.getBlockFace(), event.getAction())) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                localPlayer.printError("area.use-permissions");
            return;
        }

        runCommandSign(s, localPlayer);

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (!event.isOn() || event.isMinor() || !CraftBookPlugin.inst().getConfiguration().commandSignAllowRedstone || !SignUtil.isSign(event.getBlock()))
            return;

        ChangedSign s = BukkitUtil.toChangedSign(event.getBlock());

        if(!s.getLine(1).equals("[Command]")) return;

        if(s.getLine(0).equals("EXPANSION")) return;

        runCommandSign(s, null);
    }

    public void runCommandSign(ChangedSign sign, LocalPlayer player) {

        String command = StringUtils.replace(sign.getLine(2), "/", "") + sign.getLine(3);

        while(BlockUtil.areBlocksIdentical(BukkitUtil.toBlock(sign), BukkitUtil.toBlock(sign).getRelative(0, -1, 0))) {

            sign = BukkitUtil.toChangedSign(BukkitUtil.toBlock(sign).getRelative(0, -1, 0));
            if(!sign.getLine(1).equals("[Command]")) break;
            if(!sign.getLine(0).equals("EXPANSION")) break;

            command = command + sign.getLine(2) + sign.getLine(3);
        }

        if (player == null)
            if (command.contains("@p")) return; // We don't work with player commands.

        command = ParsingUtil.parseLine(command, player == null ? null : ((BukkitPlayer) player).getPlayer());

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }
}
