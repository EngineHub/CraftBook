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

import org.bukkit.Material;
import org.bukkit.block.Jukebox;
import org.bukkit.event.EventHandler;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.util.events.SourcedBlockRedstoneEvent;
import com.sk89q.util.yaml.YAMLProcessor;

public class RedstoneJukebox extends AbstractCraftBookMechanic {

    @EventHandler
    public void onRedstonePower(SourcedBlockRedstoneEvent event) {

        if(event.isMinor()) return;
        if(event.getBlock().getType() != Material.JUKEBOX) return; //Only listen for Jukeboxes.
        Jukebox juke = (org.bukkit.block.Jukebox) event.getBlock().getState();
        if(!event.isOn()) {
            //FIXME byte data = juke.getRawData();
            //juke.setPlaying(Material.AIR);
            //event.getBlock().setTypeIdAndData(BlockID.JUKEBOX, data, false);
        } else
            juke.setPlaying(juke.getPlaying());
        juke.update();
    }

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {
    }
}