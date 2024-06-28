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

import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.mechanics.variables.VariableKey;
import org.enginehub.craftbook.mechanics.variables.VariableManager;
import org.enginehub.craftbook.mechanics.variables.exception.VariableException;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.ProtectionUtil;
import org.enginehub.craftbook.util.SignUtil;
import org.enginehub.craftbook.util.events.SignClickEvent;
import org.enginehub.craftbook.util.persistence.OwnedSignHelper;

public class Marquee extends AbstractCraftBookMechanic {

    public Marquee(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSignClick(SignClickEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (!EventUtil.passesFilter(event)) {
            return;
        }

        ChangedSign sign = event.getSign();
        String line1 = PlainTextComponentSerializer.plainText().serialize(sign.getLine(1));
        if (!line1.equals("[Marquee]")) {
            return;
        }

        CraftBookPlayer lplayer = event.getWrappedPlayer();
        if (!lplayer.hasPermission("craftbook.marquee.use")) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                lplayer.printError(TranslatableComponent.of("craftbook.mechanisms.use-permission", TextComponent.of(getMechanicType().getName())));
            }
            return;
        }

        if (!ProtectionUtil.canUse(event.getPlayer(), event.getClickedBlock().getLocation(), event.getBlockFace(), event.getAction())) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                lplayer.printError(TranslatableComponent.of("craftbook.mechanisms.protection-blocked", TextComponent.of(getMechanicType().getName())));
            }
            return;
        }

        VariableKey variableKey;
        try {
            // Pass in the potentially stored owner, and use as default if enabled and available
            String line3 = PlainTextComponentSerializer.plainText().serialize(sign.getLine(3));
            String line2 = PlainTextComponentSerializer.plainText().serialize(sign.getLine(2));
            variableKey = VariableKey.of(line3, line2, OwnedSignHelper.getOwner(sign.getSign()));
        } catch (VariableException e) {
            lplayer.printError(e.getRichMessage());
            event.setCancelled(true);
            return;
        }

        String var = VariableManager.instance.getVariable(variableKey);
        if (var == null) {
            lplayer.printError(TranslatableComponent.of(
                "craftbook.variables.unknown-variable",
                TextComponent.of(variableKey.toString())
            ));
        } else {
            lplayer.printInfo(TextComponent.of(var));
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        if (!event.getLine(1).equalsIgnoreCase("[marquee]")) {
            return;
        }

        CraftBookPlayer lplayer = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if (!lplayer.hasPermission("craftbook.marquee.create")) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                lplayer.printError(TranslatableComponent.of(
                    "craftbook.mechanisms.create-permission",
                    TextComponent.of(getMechanicType().getName())
                ));
            }
            SignUtil.cancelSignChange(event);
            return;
        }

        VariableKey variableKey;
        try {
            // Pass in the actor, and use the global "default to global" option to handle global defaultness
            variableKey = VariableKey.of(event.getLine(3), event.getLine(2), lplayer);
        } catch (VariableException e) {
            lplayer.printError(e.getRichMessage());
            SignUtil.cancelSignChange(event);
            return;
        }

        if (!variableKey.hasPermission(lplayer, "get")) {
            lplayer.printError(TranslatableComponent.of(
                "craftbook.variables.no-permission",
                TextComponent.of(variableKey.toString())
            ));
            SignUtil.cancelSignChange(event);
            return;
        }

        String var = VariableManager.instance.getVariable(variableKey);
        if (var == null) {
            lplayer.printError(TranslatableComponent.of(
                "craftbook.variables.unknown-variable",
                TextComponent.of(variableKey.toString())
            ));
            SignUtil.cancelSignChange(event);
            return;
        }

        event.setLine(1, "[Marquee]");
        lplayer.printInfo(TranslatableComponent.of("craftbook.marquee.create"));

        // Set the owner of the sign once the sign has been created.
        Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), () -> {
            if (!SignUtil.isSign(event.getBlock())) {
                return;
            }

            // Ensure this is still a marquee sign and wasn't cancelled.
            Sign sign = (Sign) event.getBlock().getState(false);
            if (!sign.getSide(event.getSide()).getLine(1).equals("[Marquee]")) {
                return;
            }

            OwnedSignHelper.setOwner(sign, lplayer.getUniqueId());
        });
    }
}
