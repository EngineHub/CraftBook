package com.sk89q.craftbook.mech;

import java.util.HashMap;

import org.bukkit.Art;
import org.bukkit.entity.Painting;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;

import com.sk89q.craftbook.bukkit.MechanismsPlugin;

/**
 * 
 * @author Me4502
 *
 */
public class PaintingSwitch implements Listener {

    MechanismsPlugin plugin;
    HashMap<Painting, String> paintings = new HashMap<Painting, String>();
    HashMap<String, Painting> players = new HashMap<String, Painting>();

    public PaintingSwitch(MechanismsPlugin plugin) {

        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if(event.getRightClicked() instanceof Painting) {
            if (!plugin.getLocalConfiguration().paintingSettings.enabled) return;
            Painting paint = (Painting)event.getRightClicked();
            if(event.getPlayer().hasPermission("craftbook.mech.paintingswitch.use")) {
                if(paintings.get(paint) == null || plugin.getServer().getPlayer(paintings.get(paint)) == null) {
                    paintings.put(paint, event.getPlayer().getName());
                    players.put(event.getPlayer().getName(), paint);
                    event.getPlayer().sendMessage("You are now editing the painting!");
                    event.setCancelled(true);
                }
                else if(paintings.get(paint) != null && plugin.getServer().getPlayer(paintings.get(paint)) != null) {
                    event.getPlayer().sendMessage("The painting is already being edited by " + paintings.get(paint) + "!");
                    event.setCancelled(true);
                }
                else if(paintings.get(paint).equalsIgnoreCase(event.getPlayer().getName())) {
                    paintings.remove(paint);
                    players.remove(event.getPlayer().getName());
                    event.getPlayer().sendMessage("You are no longer editing the painting!");
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHeldItemChange(PlayerItemHeldEvent event) {

        if (!plugin.getLocalConfiguration().paintingSettings.enabled) return;
        if (!event.getPlayer().hasPermission("craftbook.mech.paintingswitch.use")) return;
        if(players.get(event.getPlayer().getName()) == null) return;
        boolean isForwards = false;
        if (event.getNewSlot() > event.getPreviousSlot()) isForwards = true;
        else if (event.getNewSlot() < event.getPreviousSlot()) isForwards = false;
        else return;
        if(event.getPreviousSlot() < 2 && event.getNewSlot() > 7) isForwards = false;
        else if(event.getPreviousSlot() > 7 && event.getNewSlot() < 2) isForwards = true;
        else return;
        Art[] art = Art.values();
        Painting paint = players.get(event.getPlayer().getName());
        paint.setArt(art[paint.getArt().getId() + (isForwards ? -1 : 1)]);
    }
}
