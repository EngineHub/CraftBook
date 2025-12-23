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

package org.enginehub.craftbook.bukkit.mechanics.signcopier;

import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicCommandRegistrar;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.mechanics.signcopier.SignCopier;
import org.enginehub.craftbook.mechanics.signcopier.SignEditCommands;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.ProtectionUtil;
import org.enginehub.craftbook.util.events.SignClickEvent;

import java.util.List;

public class BukkitSignCopier extends SignCopier implements Listener {
    public BukkitSignCopier(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @Override
    public void enable() {
        MechanicCommandRegistrar registrar = CraftBookPlugin.inst().getCommandManager().getMechanicRegistrar();
        registrar.registerTopLevelWithSubCommands(
            "signedit",
            List.of("edsign", "signcopy"),
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

        BaseItemStack heldItem = player.getItemInHand(event.getHand() == EquipmentSlot.HAND ? HandSide.MAIN_HAND : HandSide.OFF_HAND);

        if (heldItem.getType() != item) {
            return;
        }

        if (!player.hasPermission("craftbook.signcopier.use")) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                player.printError(TranslatableComponent.of("craftbook.mechanisms.use-permission", TextComponent.of(getMechanicType().getName())));
            }
            return;
        }

        if (!ProtectionUtil.canBreak(event.getPlayer(), block)) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                player.printError(TranslatableComponent.of("craftbook.mechanisms.protection-blocked", TextComponent.of(getMechanicType().getName())));
            }
            return;
        }

        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Sign sign = event.getSign().getSign();
            Side side = event.getSide();

            signs.put(player.getUniqueId(), createSignDataFromSign(sign.getSide(side)));

            player.printInfo(TranslatableComponent.of("craftbook.signcopier.copy"));
            event.setCancelled(true);
        } else if (event.getAction().equals(Action.LEFT_CLICK_BLOCK) && signs.containsKey(player.getUniqueId())) {
            Sign sign = event.getSign().getSign();
            Side side = event.getSide();
            SignSide signSide = sign.getSide(side);

            SignData<DyeColor> signData = (SignData<DyeColor>) signs.get(player.getUniqueId());

            // Validate that the sign can be placed here and notify plugins.
            SignChangeEvent sev = new SignChangeEvent(block, event.getPlayer(), signData.lines(), side);
            Bukkit.getPluginManager().callEvent(sev);

            if (!sev.isCancelled() || !CraftBook.getInstance().getPlatform().getConfiguration().obeyPluginProtections) {
                var outputLines = sev.lines();
                for (int i = 0; i < outputLines.size(); i++) {
                    signSide.line(i, outputLines.get(i));
                }
                if (copyColor && signData.color() != null) {
                    signSide.setColor(signData.color());
                }
                if (copyGlowing) {
                    signSide.setGlowingText(signData.glowing());
                }
                sign.update();

                player.printInfo(TranslatableComponent.of("craftbook.signcopier.paste"));
            } else {
                player.printError(TranslatableComponent.of("craftbook.signcopier.denied"));
            }

            event.setCancelled(true);
        }
    }

    /**
     * Stores data about the copied sign.
     */
    public static SignData<DyeColor> createSignDataFromSign(SignSide sign) {
        return new SignData<>(sign.lines(), sign.isGlowingText(), sign.getColor());
    }
}
