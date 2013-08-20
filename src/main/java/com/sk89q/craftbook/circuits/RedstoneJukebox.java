package com.sk89q.craftbook.circuits;

import org.bukkit.Material;
import org.bukkit.block.Jukebox;
import org.bukkit.event.EventHandler;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.SourcedBlockRedstoneEvent;
import com.sk89q.worldedit.blocks.BlockID;

public class RedstoneJukebox extends AbstractCraftBookMechanic {

    @EventHandler
    public void onRedstonePower(SourcedBlockRedstoneEvent event) {

        if(event.isMinor()) return;
        if(event.getBlock().getTypeId() != BlockID.JUKEBOX) return; //Only listen for Jukeboxes.
        Jukebox juke = (org.bukkit.block.Jukebox) event.getBlock().getState();
        if(juke.isPlaying() == event.isOn()) return; //It's already on.
        if(juke.getData().getData() == 0) return; //It's empty.
        if(!event.isOn())
            juke.setPlaying(null);
        else
            juke.setPlaying(Material.getMaterial(2555 + juke.getData().getData()));
    }
}