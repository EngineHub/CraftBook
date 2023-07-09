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
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.exception.MechanicInitializationException;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.ICUtil;
import org.enginehub.craftbook.util.ProtectionUtil;
import org.enginehub.craftbook.util.SignUtil;
import org.enginehub.craftbook.util.events.SignClickEvent;

/**
 * Payment Mech, takes payment. (Requires Vault.)
 *
 * @author Me4502
 */
public class Payment extends AbstractCraftBookMechanic {

    @Override
    public void enable() throws MechanicInitializationException {
        if (CraftBookPlugin.plugins.getEconomy() == null) {
            CraftBook.LOGGER.warn("An economy plugin and Vault is required for the Payment mechanic!");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(SignClickEvent event) {

        if (!EventUtil.passesFilter(event)) return;

        if (!SignUtil.isWallSign(event.getClickedBlock())) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ChangedSign sign = event.getSign();

        String line1 = PlainTextComponentSerializer.plainText().serialize(sign.getLine(1));
        if (!line1.equals("[Pay]")) return;

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (!player.hasPermission("craftbook.mech.pay.use")) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages)
                player.printError("mech.use-permission");
            return;
        }

        if (!ProtectionUtil.canUse(event.getPlayer(), event.getClickedBlock().getLocation(), event.getBlockFace(), event.getAction())) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages)
                player.printError("area.use-permissions");
            return;
        }

        double money = Double.parseDouble(PlainTextComponentSerializer.plainText().serialize(sign.getLine(2)));
        String reciever = PlainTextComponentSerializer.plainText().serialize(sign.getLine(3));

        if (CraftBookPlugin.plugins.getEconomy().withdrawPlayer(event.getPlayer().getName(), money).transactionSuccess()) {
            if (CraftBookPlugin.plugins.getEconomy().depositPlayer(reciever, money).transactionSuccess()) {
                Block back = SignUtil.getBackBlock(event.getClickedBlock());
                BlockFace bface = SignUtil.getBack(event.getClickedBlock());
                Block redstoneItem = back.getRelative(bface);
                player.print(player.translate("mech.pay.success") + money + ' ' + CraftBookPlugin.plugins.getEconomy().getName());
                if (ICUtil.setState(redstoneItem, true, back))
                    CraftBookPlugin.inst().getServer().getScheduler().runTaskLater(CraftBookPlugin.inst(), new TurnOff(redstoneItem, back), 20L);
            } else {
                CraftBookPlugin.plugins.getEconomy().depositPlayer(event.getPlayer().getName(), money);
                player.printError("mech.pay.failed-to-pay");
            }
        } else {
            player.printError(player.translate("mech.pay.not-enough-money"));
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        if (!EventUtil.passesFilter(event)) return;

        if (!event.getLine(1).equalsIgnoreCase("[pay]")) return;
        CraftBookPlayer lplayer = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if (!lplayer.hasPermission("craftbook.mech.pay")) {
            lplayer.printError("mech.create-permission");
            SignUtil.cancelSignChange(event);
            return;
        }

        if (event.getLine(2).isEmpty())
            event.setLine(2, String.valueOf(5));
        if (event.getLine(3).isEmpty())
            event.setLine(3, lplayer.getName());

        event.setLine(1, "[Pay]");
        lplayer.print("mech.pay.create");
    }

    private static class TurnOff implements Runnable {

        final Block block;
        final Block source;

        TurnOff(Block block, Block source) {

            this.block = block;
            this.source = source;
        }

        @Override
        public void run() {

            ICUtil.setState(block, false, source);
        }
    }

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

    }
}