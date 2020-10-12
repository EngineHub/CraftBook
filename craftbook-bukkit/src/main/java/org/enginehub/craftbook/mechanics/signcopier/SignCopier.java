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

package org.enginehub.craftbook.mechanics.signcopier;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.item.ItemTypes;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.MechanicCommandRegistrar;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.ItemSyntax;
import org.enginehub.craftbook.util.ProtectionUtil;
import org.enginehub.craftbook.util.events.SignClickEvent;

import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

public class SignCopier extends AbstractCraftBookMechanic {

    private final Map<UUID, String[]> signs = Maps.newHashMap();

    @Override
    public void enable() {
        MechanicCommandRegistrar registrar = CraftBookPlugin.inst().getCommandManager().getMechanicRegistrar();
        registrar.registerTopLevelWithSubCommands(
            "signedit",
            Lists.newArrayList("edsign", "signcopy"),
            "CraftBook SignCopier Commands",
            (commandManager, registration) -> SignEditCommands.register(commandManager, registration, this)
        );
    }

    @Override
    public void disable() {
        MechanicCommandRegistrar registrar = CraftBookPlugin.inst().getCommandManager().getMechanicRegistrar();
        registrar.unregisterTopLevel("signedit");
        registrar.unregisterTopLevel("edsign");
        registrar.unregisterTopLevel("signcopy");

        signs.clear();
    }

    /**
     * Gets whether this user has copied a sign.
     *
     * @param uuid The user
     * @return If they have copied a sign
     */
    public boolean hasSign(UUID uuid) {
        return signs.containsKey(uuid);
    }

    /**
     * Get the lines of the copied sign for the user.
     *
     * @param uuid The user's UUID
     * @return The sign lines, or null
     */
    @Nullable
    public String[] getSignLines(UUID uuid) {
        return signs.get(uuid);
    }

    /**
     * Sets the line at the line number for the given user to the given line value.
     *
     * @param uuid The user
     * @param lineNumber The line number (0-3)
     * @param line The new line value
     */
    public void setSignLine(UUID uuid, int lineNumber, String line) {
        signs.get(uuid)[lineNumber] = line;
    }

    /**
     * Clears the copied sign of the user.
     *
     * @param uuid The user
     */
    public void clearSign(UUID uuid) {
        signs.remove(uuid);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRightClick(SignClickEvent event) {
        Block block = event.getClickedBlock();

        if ((event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK)
            || event.getHand() == null
            || block == null) {
            return;
        }

        if (!EventUtil.passesFilter(event)) {
            return;
        }

        CraftBookPlayer player = event.getWrappedPlayer();

        if (!event.getPlayer().getInventory().getItem(event.getHand()).isSimilar(item)) {
            return;
        }

        if (!player.hasPermission("craftbook.signcopier.use")) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                player.printError("mech.use-permission");
            }
            return;
        }

        if (ProtectionUtil.isBreakingPrevented(event.getPlayer(), block)) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                player.printError("area.use-permissions");
            }
            return;
        }

        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Sign sign = (Sign) PaperLib.getBlockState(block, false).getState();

            signs.put(player.getUniqueId(), sign.getLines());

            player.printInfo(TranslatableComponent.of("craftbook.signcopier.copy"));
            event.setCancelled(true);
        } else if (event.getAction().equals(Action.LEFT_CLICK_BLOCK) && signs.containsKey(player.getUniqueId())) {
            Sign sign = (Sign) PaperLib.getBlockState(block, false).getState();
            String[] lines = signs.get(player.getUniqueId());

            // Validate that the sign can be placed here and notify plugins.
            SignChangeEvent sev = new SignChangeEvent(block, event.getPlayer(), lines);
            Bukkit.getPluginManager().callEvent(sev);

            if (!sev.isCancelled() || !CraftBook.getInstance().getPlatform().getConfiguration().obeyPluginProtections) {
                for (int i = 0; i < lines.length; i++) {
                    sign.setLine(i, lines[i]);
                }
                sign.update();

                player.printInfo(TranslatableComponent.of("craftbook.signcopier.paste"));
            } else {
                player.printError(TranslatableComponent.of("craftbook.signcopier.denied"));
            }

            event.setCancelled(true);
        }
    }

    private ItemStack item;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("item", "The item for the sign copy tool.");
        item = ItemSyntax.getItem(config.getString("item", ItemTypes.INK_SAC.getId()));
    }
}
