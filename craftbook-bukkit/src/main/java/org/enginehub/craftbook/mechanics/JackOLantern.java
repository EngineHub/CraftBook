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

package org.enginehub.craftbook.mechanics;

import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.events.SourcedBlockRedstoneEvent;

/**
 * This mechanism allow players to toggle Jack-o-Lanterns.
 */
public class JackOLantern extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        if (event.isMinor()) {
            return;
        }

        Material type = event.getBlock().getType();

        // We only care about carved / lit pumpkins.
        if (type != Material.CARVED_PUMPKIN && type != Material.JACK_O_LANTERN) {
            return;
        }

        boolean currentState = type == Material.JACK_O_LANTERN;

        // Only toggle the block if we're changing something.
        if (event.isOn() == currentState) {
            return;
        }

        setPowered(event.getBlock(), event.isOn());
    }

    private static void setPowered(Block block, boolean on) {
        BlockFace data = ((Directional) block.getBlockData()).getFacing();
        block.setType(on ? Material.JACK_O_LANTERN : Material.CARVED_PUMPKIN);
        Directional directional = (Directional) block.getBlockData();
        directional.setFacing(data);
        block.setBlockData(directional);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!preventBreaking || !EventUtil.passesFilter(event)) {
            return;
        }

        if (event.getBlock().getType() == Material.JACK_O_LANTERN && (event.getBlock().isBlockIndirectlyPowered() || event.getBlock().isBlockPowered())) {
            event.setCancelled(true);
        }
    }

    private boolean preventBreaking;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("prevent-breaking", "Whether powered Jack O Lanterns should be unbreakable.");
        preventBreaking = config.getBoolean("prevent-breaking", false);
    }
}
