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

package org.enginehub.craftbook.mechanics.minecart.blocks;

import com.google.common.collect.ImmutableList;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.mechanics.minecart.events.CartBlockImpactEvent;
import org.enginehub.craftbook.util.BlockParser;
import org.enginehub.craftbook.util.RedstoneUtil.Power;

import java.util.ArrayList;
import java.util.List;

public class CartMessenger extends CartBlockMechanism {

    @EventHandler
    public void onVehicleImpact(CartBlockImpactEvent event) {
        if (event.isMinor() || !event.getBlocks().matches(getBlock()) || event.getMinecart().getPassengers().isEmpty()) {
            return;
        }

        if (!event.getBlocks().hasSign() || Power.OFF == isActive(event.getBlocks())) {
            return;
        }

        // go
        if (event.getMinecart().getPassenger() instanceof Player p) {
            Side side = event.getBlocks().matches("print");
            if (side == null) {
                Sign bukkitSign = (Sign) event.getBlocks().sign().getState(false);
                for (Side testSide : Side.values()) {
                    if (bukkitSign.getSide(testSide).getLine(0).equalsIgnoreCase("[print]")) {
                        side = testSide;
                    }
                }
            }
            if (side == null) {
                return;
            }

            ChangedSign s = event.getBlocks().getChangedSign(side);

            ArrayList<String> messages = new ArrayList<>();

            boolean stack = false;

            String line1 = PlainTextComponentSerializer.plainText().serialize(s.getLine(1));
            if (!line1.isEmpty() && !line1.equalsIgnoreCase("[print]")) {
                messages.add(line1);
                stack = line1.endsWith("+") || line1.endsWith(" ");
            }
            String line2 = PlainTextComponentSerializer.plainText().serialize(s.getLine(2));
            if (!line2.isEmpty()) {
                if (stack) {
                    messages.set(messages.size() - 1, messages.get(messages.size() - 1) + line2);
                    stack = line2.endsWith("+") || line2.endsWith(" ");
                } else {
                    messages.add(line2);
                    stack = line2.endsWith("+") || line2.endsWith(" ");
                }
            }
            String line3 = PlainTextComponentSerializer.plainText().serialize(s.getLine(3));
            if (!line3.isEmpty()) {
                if (stack) {
                    messages.set(messages.size() - 1, messages.get(messages.size() - 1) + line3);
                } else {
                    messages.add(line3);
                }
            }

            for (String mes : messages) {
                if (stack) mes = mes.replace("+", "");
                p.sendMessage(mes);
            }
        }
    }

    @Override
    public List<String> getApplicableSigns() {
        return ImmutableList.copyOf(new String[] { "Print" });
    }

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("block", "Sets the block that is the base of the messager mechanic.");
        setBlock(BlockParser.getBlock(config.getString("block", BlockTypes.END_STONE.getId()), true));
    }
}
