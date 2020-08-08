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

package com.sk89q.craftbook.mechanics;

import com.sk89q.craftbook.util.EventUtil;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Jukebox;
import org.bukkit.event.EventHandler;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.util.events.SourcedBlockRedstoneEvent;

/**
 * This mechanism allows a jukebox to be started and stopped via redstone.
 */
public class RedstoneJukebox extends AbstractCraftBookMechanic {

    @EventHandler
    public void onRedstonePower(SourcedBlockRedstoneEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        // Only listen for Jukeboxes.
        if (event.isMinor() || event.getBlock().getType() != Material.JUKEBOX) {
            return;
        }

        Jukebox jukebox = (Jukebox) event.getBlock().getState();

        if (jukebox.getRecord().getType() == Material.AIR) {
            // We only care if the jukebox has a record.
            return;
        }

        if (!event.isOn()) {
            event.getBlock().getWorld().playEffect(event.getBlock().getLocation(), Effect.RECORD_PLAY, Material.AIR);
        } else {
            jukebox.setRecord(jukebox.getRecord());
            jukebox.update();
        }
    }

}
