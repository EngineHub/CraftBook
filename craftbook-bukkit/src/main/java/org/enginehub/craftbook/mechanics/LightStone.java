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

// $Id$
/*
 * CraftBook Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.ItemSyntax;
import org.enginehub.craftbook.util.ProtectionUtil;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.world.item.ItemTypes;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

// TODO Potentially turn into a WE tool
/**
 * This allows users to Right-click to check the light level.
 */
public class LightStone extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onRightClick(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() == null || block == null) {
            return;
        }

        if (!EventUtil.passesFilter(event)) {
            return;
        }

        if (!event.getPlayer().getInventory().getItem(event.getHand()).isSimilar(item)) {
            return;
        }

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if (!player.hasPermission("craftbook.lightstone.use")) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                player.printError("mech.use-permission");
            }
            return;
        }

        if (!ProtectionUtil.canUse(event.getPlayer(), event.getClickedBlock().getLocation(), event.getBlockFace(), event.getAction())) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                player.printError("area.use-permissions");
            }
            return;
        }

        block = event.getClickedBlock().getRelative(event.getBlockFace());
        Component component = TranslatableComponent
                .of("craftbook.lightstone.line",
                        getCurrentLine(block.getLightLevel()),
                        TextComponent.of(block.getLightLevel(), TextColor.YELLOW))
                .color(TextColor.YELLOW);
        if (actionBar) {
            player.printActionBar(component);
        } else {
            player.print(component);
        }
        event.setCancelled(true);
    }

    private Component getCurrentLine(byte data) {
        TextComponent.Builder line = TextComponent.builder();
        line.append("[", TextColor.YELLOW);

        TextComponent.Builder colorBuilder = TextComponent.builder();
        if (data > 8) {
            line.color(TextColor.GREEN);
        } else {
            line.color(TextColor.DARK_RED);
        }
        for (int i = 0; i < data; i++) {
            colorBuilder.append("|");
        }
        line.append(colorBuilder.build());

        if (data < 15) {
            TextComponent.Builder blackBuilder = TextComponent.builder();
            blackBuilder.color(TextColor.BLACK);
            for (int i = data; i < 15; i++) {
                blackBuilder.append("|");
            }
            line.append(blackBuilder.build());
        }

        line.append("]", TextColor.YELLOW);
        return line.build();
    }

    private ItemStack item;
    private boolean actionBar;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("item", "The item for the lightstone tool.");
        item = ItemSyntax.getItem(config.getString("item", ItemTypes.GLOWSTONE_DUST.getId()));

        config.setComment("use-action-bar", "Whether to use the action bar or the player's chat.");
        actionBar = config.getBoolean("use-action-bar", true);
    }
}