/*
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package org.enginehub.craftbook.mechanics;

import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.bukkit.util.CraftBookBukkitUtil;
import org.enginehub.craftbook.util.BlockUtil;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.ParsingUtil;
import org.enginehub.craftbook.util.ProtectionUtil;
import org.enginehub.craftbook.util.SignUtil;
import org.enginehub.craftbook.util.events.SignClickEvent;
import org.enginehub.craftbook.util.events.SourcedBlockRedstoneEvent;

public class CommandSigns extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        if (!EventUtil.passesFilter(event)) return;

        if (!event.getLine(1).equalsIgnoreCase("[command]")) return;
        CraftBookPlayer lplayer = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if (!lplayer.hasPermission("craftbook.mech.command")) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages)
                lplayer.printError("mech.create-permission");
            SignUtil.cancelSignChange(event);
            return;
        }

        event.setLine(1, "[Command]");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(SignClickEvent event) {

        if (!EventUtil.passesFilter(event)) return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ChangedSign s = event.getSign();

        if (!s.getLine(1).equals("[Command]")) return;

        if (s.getLine(0).equals("EXPANSION")) return;

        CraftBookPlayer localPlayer = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (!localPlayer.hasPermission("craftbook.mech.command.use")) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages)
                localPlayer.printError("mech.use-permission");
            return;
        }

        if (!ProtectionUtil.canUse(event.getPlayer(), event.getClickedBlock().getLocation(), event.getBlockFace(), event.getAction())) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages)
                localPlayer.printError("area.use-permissions");
            return;
        }

        runCommandSign(s, localPlayer);

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {

        if (!event.isOn() || event.isMinor() || !allowRedstone || !SignUtil.isSign(event.getBlock()))
            return;

        if (!EventUtil.passesFilter(event)) return;

        ChangedSign s = CraftBookBukkitUtil.toChangedSign(event.getBlock());

        if (!s.getLine(1).equals("[Command]")) return;

        if (s.getLine(0).equals("EXPANSION")) return;

        runCommandSign(s, null);
    }

    public static void runCommandSign(ChangedSign sign, CraftBookPlayer player) {

        StringBuilder command = new StringBuilder(sign.getLine(2).replace("/", "") + sign.getLine(3));

        while (BlockUtil.areBlocksIdentical(sign.getBlock(), sign.getBlock().getRelative(0, -1, 0))) {

            sign = CraftBookBukkitUtil.toChangedSign(sign.getBlock().getRelative(0, -1, 0));
            if (!sign.getLine(1).equals("[Command]")) break;
            if (!sign.getLine(0).equals("EXPANSION")) break;

            command.append(sign.getLine(2)).append(sign.getLine(3));
        }

        if (player == null) {
            if (command.toString().contains("@p")) {
                return; // We don't work with player commands.
            }
        }

        command = new StringBuilder(ParsingUtil.parseLine(command.toString(), player));

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.toString());
    }

    private boolean allowRedstone;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

        config.setComment("allow-redstone", "Enable CommandSigns via redstone.");
        allowRedstone = config.getBoolean("allow-redstone", true);
    }
}
