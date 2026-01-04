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

package org.enginehub.craftbook.bukkit.mechanics;

import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.BukkitChangedSign;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.bukkit.events.SignClickEvent;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.mechanics.LightSwitch;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.ProtectionUtil;
import org.enginehub.craftbook.util.SignUtil;

/**
 * Handler for Light switches. Toggles all torches in the area from being redstone to normal
 * torches. This is done
 * every time a sign with [|] or [I]
 * is right clicked by a player.
 */
public class BukkitLightSwitch extends LightSwitch implements Listener {

    public BukkitLightSwitch(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        String signLine1 = PlainTextComponentSerializer.plainText().serialize(event.line(1));
        if (!signLine1.equalsIgnoreCase("[i]") && !signLine1.equalsIgnoreCase("[|]")) {
            return;
        }

        CraftBookPlayer lplayer = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if (!lplayer.hasPermission("craftbook.lightswitch.create")) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                lplayer.printError(TranslatableComponent.of(
                    "craftbook.mechanisms.create-permission",
                    TextComponent.of(getMechanicType().getName())
                ));
            }
            SignUtil.cancelSignChange(event);
            return;
        }

        event.line(1, Component.text("[I]"));
        lplayer.printInfo(TranslatableComponent.of("craftbook.lightswitch.create"));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(SignClickEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || !EventUtil.passesFilter(event)) {
            return;
        }

        BukkitChangedSign sign = event.getSign();

        String line1 = PlainTextComponentSerializer.plainText().serialize(sign.getLine(1));
        if (!line1.equals("[I]")) {
            return;
        }

        CraftBookPlayer player = event.getWrappedPlayer();
        if (!player.hasPermission("craftbook.lightswitch.use")) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                player.printError(TranslatableComponent.of("craftbook.mechanisms.use-permission", TextComponent.of(getMechanicType().getName())));
            }
            return;
        }

        if (!ProtectionUtil.canUse(event.getPlayer(), event.getClickedBlock().getLocation(), event.getBlockFace(), event.getAction())) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                player.printError(TranslatableComponent.of("craftbook.mechanisms.protection-blocked", TextComponent.of(getMechanicType().getName())));
            }
            return;
        }

        toggleLights(sign);
        event.setCancelled(true);
    }

    /**
     * Toggle lights in the immediate area.
     *
     * @param sign The sign to toggle based on
     */
    private void toggleLights(BukkitChangedSign sign) {
        int radius;
        int maximum;

        try {
            String line2 = PlainTextComponentSerializer.plainText().serialize(sign.getLine(2));
            radius = Math.min(Integer.parseInt(line2), maxRange);
        } catch (Exception ignored) {
            radius = Math.min(10, maxRange);
        }

        try {
            String line3 = PlainTextComponentSerializer.plainText().serialize(sign.getLine(3));
            maximum = Math.min(Integer.parseInt(line3), maxLights);
        } catch (Exception ignored) {
            maximum = Math.min(maxLights, 20);
        }

        int wx = sign.getX();
        int wy = sign.getY();
        int wz = sign.getZ();
        Material aboveID = sign.getBlock().getRelative(0, 1, 0).getType();

        if (aboveID == Material.WALL_TORCH || aboveID == Material.REDSTONE_WALL_TORCH) {
            // Check if block above is a redstone torch.
            // Used to get what to change torches to.
            boolean on = aboveID != Material.WALL_TORCH;

            int changed = 0;
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        if (x == 0 && y == 0 && z == 0) {
                            // Skip the sign itself
                            continue;
                        }

                        Block relBlock = sign.getBlock().getWorld().getBlockAt(x + wx, y + wy, z + wz);
                        Material id = relBlock.getType();

                        if (id == Material.TORCH || id == Material.WALL_TORCH || id == Material.REDSTONE_TORCH || id == Material.REDSTONE_WALL_TORCH) {
                            // Limit the maximum number of changed lights
                            if (changed >= maximum) {
                                return;
                            }

                            if (id == Material.WALL_TORCH || id == Material.REDSTONE_WALL_TORCH) {
                                Directional currentData = (Directional) relBlock.getBlockData();

                                Directional directional = (Directional) (on ? Material.WALL_TORCH : Material.REDSTONE_WALL_TORCH).createBlockData();
                                directional.setFacing(currentData.getFacing());
                                relBlock.setBlockData(directional, false);
                            } else {
                                relBlock.setType(on ? Material.TORCH : Material.REDSTONE_TORCH, false);
                            }
                            changed++;
                        }
                    }
                }
            }
        }
    }
}
