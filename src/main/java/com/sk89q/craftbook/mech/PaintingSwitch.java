package com.sk89q.craftbook.mech;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.Art;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.LocationUtil;

/**
 * @author Me4502
 */
public class PaintingSwitch implements Listener {

    CraftBookPlugin plugin = CraftBookPlugin.inst();
    Map<Painting, String> paintings = new WeakHashMap<Painting, String>();
    Map<String, WeakReference<Painting>> players = new HashMap<String, WeakReference<Painting>>();

    public boolean isBeingEdited(Painting paint) {

        String player = paintings.get(paint);
        if (player != null && players.get(player) != null) {
            Player p = plugin.getServer().getPlayerExact(player);
            return p != null && !p.isDead();
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {

        if (event.getRightClicked() instanceof Painting) {
            LocalPlayer player = plugin.wrapPlayer(event.getPlayer());
            if (!plugin.getConfiguration().paintingsEnabled) return;
            Painting paint = (Painting) event.getRightClicked();
            if (!plugin.canUse(event.getPlayer(), paint.getLocation(), null, Action.RIGHT_CLICK_BLOCK)) return;
            if (player.hasPermission("craftbook.mech.paintingswitch.use")) {
                if (!isBeingEdited(paint)) {
                    paintings.put(paint, player.getName());
                    players.put(player.getName(), new WeakReference<Painting>(paint));
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

        if (!plugin.getConfiguration().paintingsEnabled) return;
        LocalPlayer player = plugin.wrapPlayer(event.getPlayer());
        if (!player.hasPermission("craftbook.mech.paintingswitch.use")) return;
        if (players.get(player.getName()) == null || players.get(player.getName()).get() == null|| players.get(player.getName()).get().isDead() || !players.get(player.getName()).get().isValid())
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
        Painting paint = players.get(player.getName()).get();
        if(!LocationUtil.isWithinSphericalRadius(paint.getLocation(), event.getPlayer().getLocation(), 5)) {
            Painting p = players.remove(event.getPlayer().getName()).get();
            if (p != null) {
                player.printError("mech.painting.range");
                paintings.remove(p);
            }

            return;
        }
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
        players.put(player.getName(), new WeakReference<Painting>(paint));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        WeakReference<Painting> p = players.remove(event.getPlayer().getName());
        if (p != null) {
            paintings.remove(p.get());
        }
    }
}