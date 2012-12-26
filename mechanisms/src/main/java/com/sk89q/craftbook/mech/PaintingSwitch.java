package com.sk89q.craftbook.mech;

import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import org.bukkit.Art;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;

/**
 * @author Me4502
 */
public class PaintingSwitch implements Listener {

    MechanismsPlugin plugin;
    HashMap<Painting, String> paintings = new HashMap<Painting, String>();
    HashMap<String, Painting> players = new HashMap<String, Painting>();

    public PaintingSwitch(MechanismsPlugin plugin) {

        this.plugin = plugin;
    }

    public boolean isBeingEdited(Painting paint) {

        String player = paintings.get(paint);
        if (player != null && players.get(player) != null) {
            Player p = plugin.getServer().getPlayer(player);
            return p != null && !p.isDead();
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {

        if (event.getRightClicked() instanceof Painting) {
            LocalPlayer player = plugin.wrap(event.getPlayer());
            if (!plugin.getLocalConfiguration().paintingSettings.enabled) return;
            Painting paint = (Painting) event.getRightClicked();
            if (!plugin.canUseInArea(paint.getLocation(), event.getPlayer())) return;
            if (player.hasPermission("craftbook.mech.paintingswitch.use")) {
                if (!isBeingEdited(paint)) {
                    paintings.put(paint, player.getName());
                    players.put(player.getName(), paint);
                    player.print("mech.painting.editing");
                } else if (paintings.get(paint).equalsIgnoreCase(player.getName())) {
                    paintings.remove(paint);
                    players.remove(player.getName());
                    player.print("mech.painting.stop");
                } else if (isBeingEdited(paint)) {
                    player.print(player.translate("mech.painting.used") + " " + paintings.get(paint));
                } else {
                    return;
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHeldItemChange(PlayerItemHeldEvent event) {

        if (!plugin.getLocalConfiguration().paintingSettings.enabled) return;
        LocalPlayer player = plugin.wrap(event.getPlayer());
        if (!player.hasPermission("craftbook.mech.paintingswitch.use")) return;
        if (players.get(player.getName()) == null || players.get(player.getName()).isDead() || !players.get(player
                .getName()).isValid())
            return;
        boolean isForwards;
        if (event.getNewSlot() > event.getPreviousSlot()) {
            isForwards = true;
        } else if (event.getNewSlot() < event.getPreviousSlot()) {
            isForwards = false;
        } else return;
        if (event.getPreviousSlot() == 0 && event.getNewSlot() == 8) {
            isForwards = false;
        } else if (event.getPreviousSlot() == 8 && event.getNewSlot() == 0) {
            isForwards = true;
        }
        Art[] art = Art.values().clone();
        Painting paint = players.get(player.getName());
        int newID = paint.getArt().getId() + (isForwards ? 1 : -1);
        if (newID < 0) {
            newID = art.length - 1;
        } else if (newID > art.length - 1) {
            newID = 0;
        }
        while (!paint.setArt(art[newID])) {
            if (newID > 0 && !isForwards) {
                newID--;
            } else if (newID < art.length - 1 && isForwards) {
                newID++;
            } else {
                break;
            }
        }
        paintings.put(paint, player.getName());
        players.put(player.getName(), paint);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        Painting p = players.remove(event.getPlayer().getName());
        if (p != null) {
            paintings.remove(p);
        }
    }
}