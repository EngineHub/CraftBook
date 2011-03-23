package com.sk89q.craftbook.util;

import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.Event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;

/**
 * Bukkit's event refactors can be... disruptive, to put it lightly.
 * 
 * Removing BlockRightClickEvent was one of the dumbest things I've ever seen done.
 * 
 * @author hash
 * 
 */
public class EventProcessor {
    public static BlockRightClickEvent toTrigger(PlayerInteractEvent original) {
        return new BlockRightClickEvent(
                Type.CUSTOM_EVENT,      // I don't think this is even legal.
                original.getClickedBlock(),
                BlockFace.SELF,
                original.getItem(),
                original.getPlayer()
        );
    }
}
