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

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.mechanics.RedstoneGlowstone;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.events.SourcedBlockRedstoneEvent;

/**
 * This mechanism allow players to toggle GlowStone.
 */
public class BukkitRedstoneGlowstone extends RedstoneGlowstone implements Listener {

    public BukkitRedstoneGlowstone(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        if (event.isMinor()) {
            return;
        }

        Material type = event.getBlock().getType();

        // We only care about glowstone and the off block
        if (type != Material.GLOWSTONE
            && !offBlock.equalsFuzzy(BukkitAdapter.adapt(event.getBlock().getBlockData()))) {
            return;
        }

        boolean currentState = type == Material.GLOWSTONE;

        if (event.isOn() == currentState) {
            return;
        }

        setPowered(event.getBlock(), event.isOn());
    }

    private void setPowered(Block block, boolean state) {
        if (state) {
            block.setType(Material.GLOWSTONE);
        } else {
            block.setBlockData(BukkitAdapter.adapt(offBlock));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!preventBreaking || !EventUtil.passesFilter(event)) {
            return;
        }

        if (event.getBlock().getType() == Material.GLOWSTONE && (event.getBlock().isBlockIndirectlyPowered() || event.getBlock().isBlockPowered())) {
            event.setCancelled(true);
        }
    }
}
