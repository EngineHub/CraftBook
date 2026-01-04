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

import org.bukkit.Material;
import org.bukkit.block.Jukebox;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.enginehub.craftbook.bukkit.events.SourcedBlockRedstoneEvent;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.mechanics.RedstoneJukebox;
import org.enginehub.craftbook.util.EventUtil;

/**
 * This mechanism allows a jukebox to be started and stopped via redstone.
 */
public class BukkitRedstoneJukebox extends RedstoneJukebox implements Listener {

    public BukkitRedstoneJukebox(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @EventHandler
    public void onRedstonePower(SourcedBlockRedstoneEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        // Only listen for Jukeboxes.
        if (event.isMinor() || event.getBlock().getType() != Material.JUKEBOX) {
            return;
        }

        Jukebox jukebox = (Jukebox) event.getBlock().getState(false);

        if (jukebox.getRecord().getType() == Material.AIR) {
            // We only care if the jukebox has a record.
            return;
        }

        if (!event.isOn()) {
            jukebox.stopPlaying();
        } else {
            jukebox.setRecord(jukebox.getRecord());
            jukebox.update();
        }
    }
}
