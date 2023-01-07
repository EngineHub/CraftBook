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
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.world.item.ItemTypes;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.AnaloguePowerable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.Powerable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.ItemSyntax;
import org.enginehub.craftbook.util.ProtectionUtil;

// TODO Potentially turn into a WE tool

/**
 * This allows users to Right-click to check the power level of redstone.
 */
public class Ammeter extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
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
        if (!player.hasPermission("craftbook.ammeter.use")) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                player.printError(TranslatableComponent.of("craftbook.mechanisms.use-permission", TextComponent.of(getMechanicType().getName())));
            }
            return;
        }

        if (!ProtectionUtil.canUse(event.getPlayer(), block.getLocation(), event.getBlockFace(), event.getAction())) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                player.printError(TranslatableComponent.of("craftbook.mechanisms.protection-blocked", TextComponent.of(getMechanicType().getName())));
            }
            return;
        }

        int data = getSpecialData(block);
        if (data >= 0) {
            Component component = TranslatableComponent
                .of("craftbook.ammeter.line", getCurrentLine(data), TextComponent.of(data, TextColor.YELLOW))
                .color(TextColor.YELLOW);
            if (actionBar) {
                player.printActionBar(component);
            } else {
                player.print(component);
            }
            event.setCancelled(true);
        }
    }

    private int getSpecialData(Block block) {
        Material type = block.getType();
        BlockData blockData = block.getBlockData();
        int current = -1;
        if (blockData instanceof Powerable powerable) {
            current = powerable.isPowered() ? 15 : 0;
        } else if (blockData instanceof AnaloguePowerable powerable) {
            current = powerable.getPower();
        } else if ((type == Material.REDSTONE_TORCH || type == Material.REDSTONE_WALL_TORCH) && blockData instanceof Lightable lightable) {
            current = lightable.isLit() ? 15 : 0;
        } else if (block.getType() == Material.REDSTONE_BLOCK) {
            current = 15;
        }

        return current;
    }

    private Component getCurrentLine(int data) {
        TextComponent.Builder line = TextComponent.builder();
        line.append("[", TextColor.YELLOW);

        TextComponent.Builder colorBuilder = TextComponent.builder();
        if (data > 10) {
            line.color(TextColor.DARK_GREEN);
        } else if (data > 5) {
            line.color(TextColor.GOLD);
        } else if (data > 0) {
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
        config.setComment("item", "The item for the ammeter tool.");
        item = ItemSyntax.getItem(config.getString("item", ItemTypes.COAL.getId()));

        config.setComment("use-action-bar", "Whether to use the action bar or the player's chat.");
        actionBar = config.getBoolean("use-action-bar", true);
    }
}
