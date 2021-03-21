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

package org.enginehub.craftbook.mechanics.minecart.blocks;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.mechanics.minecart.events.CartBlockImpactEvent;
import org.enginehub.craftbook.util.BlockParser;
import org.enginehub.craftbook.util.RedstoneUtil.Power;

import java.util.ArrayList;

public class CartMessenger extends CartBlockMechanism {

    @EventHandler
    public void onVehicleImpact(CartBlockImpactEvent event) {

        // validate
        if (event.isMinor()) return;
        if (!event.getBlocks().matches(getBlock())) return;

        // care?
        if (event.getMinecart().getPassenger() == null) return;
        if (!event.getBlocks().hasSign()) return;

        // enabled?
        if (Power.OFF == isActive(event.getBlocks())) return;

        // go
        if (event.getMinecart().getPassenger() instanceof Player) {
            Player p = (Player) event.getMinecart().getPassenger();
            ChangedSign s = event.getBlocks().getSign();
            if (!s.getLine(0).equalsIgnoreCase("[print]") && !s.getLine(1).equalsIgnoreCase("[print]"))
                return;

            ArrayList<String> messages = new ArrayList<>();

            boolean stack = false;

            if (s.getLine(1) != null && !s.getLine(1).isEmpty() && !s.getLine(1).equalsIgnoreCase("[print]")) {
                messages.add(s.getLine(1));
                stack = s.getLine(1).endsWith("+") || s.getLine(1).endsWith(" ");
            }
            if (s.getLine(2) != null && !s.getLine(2).isEmpty()) {
                if (stack) {
                    messages.set(messages.size() - 1, messages.get(messages.size() - 1) + s.getLine(2));
                    stack = s.getLine(2).endsWith("+") || s.getLine(2).endsWith(" ");
                } else {
                    messages.add(s.getLine(2));
                    stack = s.getLine(2).endsWith("+") || s.getLine(2).endsWith(" ");
                }
            }
            if (s.getLine(3) != null && !s.getLine(3).isEmpty()) {
                if (stack) {
                    messages.set(messages.size() - 1, messages.get(messages.size() - 1) + s.getLine(3));
                } else {
                    messages.add(s.getLine(3));
                }
            }

            for (String mes : messages) {
                if (stack) mes = StringUtils.replace(mes, "+", "");
                p.sendMessage(mes);
            }
        }
    }

    @Override
    public String getName() {

        return "Messager";
    }

    @Override
    public String[] getApplicableSigns() {

        return new String[] { "Print" };
    }

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

        config.setComment("block", "Sets the block that is the base of the messager mechanic.");
        material = BlockParser.getBlock(config.getString("block", BlockTypes.END_STONE.getId()), true);
    }
}