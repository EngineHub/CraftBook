/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
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

package com.sk89q.craftbook.mechanics;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.CraftBook;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.mechanics.variables.VariableKey;
import com.sk89q.craftbook.mechanics.variables.VariableManager;
import com.sk89q.craftbook.mechanics.variables.exception.VariableException;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ProtectionUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.events.SignClickEvent;
import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;

public class Marquee extends AbstractCraftBookMechanic {

    @Override
    public boolean enable() {
        return VariableManager.instance != null;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignClick(SignClickEvent event) {

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if(!EventUtil.passesFilter(event)) return;

        ChangedSign sign = event.getSign();
        if(!sign.getLine(1).equals("[Marquee]")) return;
        CraftBookPlayer lplayer = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if(!lplayer.hasPermission("craftbook.mech.marquee.use")) {
            if(CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages)
                lplayer.printError("mech.use-permission");
            return;
        }

        if(!ProtectionUtil.canUse(event.getPlayer(), event.getClickedBlock().getLocation(), event.getBlockFace(), event.getAction())) {
            if(CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages)
                lplayer.printError("area.use-permissions");
            return;
        }

        VariableKey variableKey;
        try {
            variableKey = VariableKey.of(sign.getLine(3), sign.getLine(2), null);
        } catch (VariableException e) {
            lplayer.printError(e.getRichMessage());
            e.printStackTrace();

            event.setCancelled(true);
            return;
        }
        String var = VariableManager.instance.getVariable(variableKey);
        if(var == null || var.isEmpty()) var = "variable.missing";
        lplayer.print(var);

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(!event.getLine(1).equalsIgnoreCase("[marquee]")) return;
        CraftBookPlayer lplayer = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if(!lplayer.hasPermission("craftbook.mech.marquee")) {
            if(CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages)
                lplayer.printError("mech.create-permission");
            SignUtil.cancelSign(event);
            return;
        }

        // Don't pass the actor as we want it to default to global.
        VariableKey variableKey = null;
        try {
            variableKey = VariableKey.of(event.getLine(3), event.getLine(2), null);
        } catch (VariableException e) {
            lplayer.printError(e.getRichMessage());
            SignUtil.cancelSign(event);
            return;
        }

        if(!variableKey.hasPermission(lplayer, "get")) {
            lplayer.printError("variable.use-permissions");
            SignUtil.cancelSign(event);
        }

        String var = VariableManager.instance.getVariable(variableKey);
        if(var == null || var.isEmpty()) {
            lplayer.printError("variable.missing");
            SignUtil.cancelSign(event);
        }

        event.setLine(1, "[Marquee]");
    }

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

    }
}