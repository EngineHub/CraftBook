package com.sk89q.craftbook.mechanics;

import org.bukkit.Material;
import org.bukkit.block.Jukebox;
import org.bukkit.event.EventHandler;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.util.events.SourcedBlockRedstoneEvent;

public class RedstoneJukebox extends AbstractCraftBookMechanic {

    @EventHandler
    public void onRedstonePower(SourcedBlockRedstoneEvent event) {

        if(event.isMinor()) return;
        if(event.getBlock().getType() != Material.JUKEBOX) return; //Only listen for Jukeboxes.
        Jukebox juke = (org.bukkit.block.Jukebox) event.getBlock().getState();
        if(!event.isOn()) {
            //FIXME byte data = juke.getRawData();
            //juke.setPlaying(null);
            //event.getBlock().setTypeIdAndData(BlockID.JUKEBOX, data, false);
        } else
            juke.setPlaying(juke.getPlaying());
        juke.update();
    }
}