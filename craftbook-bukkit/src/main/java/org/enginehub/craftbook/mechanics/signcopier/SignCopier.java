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

package com.sk89q.craftbook.mechanics.signcopier;

import com.google.common.collect.Lists;
import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.CraftBook;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.mechanic.MechanicCommandRegistrar;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ProtectionUtil;
import com.sk89q.craftbook.util.events.SignClickEvent;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;

import java.util.HashMap;
import java.util.Map;

public class SignCopier extends AbstractCraftBookMechanic {

    public static Map<String, String[]> signs;

    @Override
    public boolean enable() {
        signs = new HashMap<>();

        MechanicCommandRegistrar registrar = CraftBookPlugin.inst().getCommandManager().getMechanicRegistrar();
        registrar.registerTopLevelWithSubCommands(
                "signedit",
                Lists.newArrayList("edsign", "signcopy"),
                "CraftBook SignCopier Commands",
                SignEditCommands::register
        );

        return true;
    }

    @Override
    public void disable() {

        MechanicCommandRegistrar registrar = CraftBookPlugin.inst().getCommandManager().getMechanicRegistrar();
        registrar.unregisterTopLevel("signedit");
        registrar.unregisterTopLevel("edsign");
        registrar.unregisterTopLevel("signcopy");

        signs = null;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRightClick(SignClickEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        CraftBookPlayer player = event.getWrappedPlayer();

        if (player.getItemInHand(HandSide.MAIN_HAND).getType() != item) return;

        if (!player.hasPermission("craftbook.mech.signcopy.use")) {
            if(CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages)
                player.printError("mech.use-permission");
            return;
        }

        if(!ProtectionUtil.canBuild(event.getPlayer(), event.getClickedBlock().getLocation(), false)) {
            if(CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages)
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

    private ItemType item;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

        config.setComment("item", "The item the Sign Copy mechanic uses.");
        item = BukkitAdapter.asItemType(ItemSyntax.getItem(config.getString("item", ItemTypes.INK_SAC.getId())).getType());
    }
}