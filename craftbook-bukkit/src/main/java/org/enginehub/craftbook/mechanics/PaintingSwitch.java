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

package org.enginehub.craftbook.mechanics;

import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.LocationUtil;
import org.enginehub.craftbook.util.ProtectionUtil;
import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Me4502
 */
public class PaintingSwitch extends AbstractCraftBookMechanic {

    private Map<Painting, UUID> paintings = new HashMap<>();
    private Map<UUID, Painting> players = new HashMap<>();

    public boolean isBeingEdited(Painting paint) {

        UUID player = paintings.get(paint);
        if (player != null && players.get(player) != null) {
            Player p = CraftBookPlugin.inst().getServer().getPlayer(player);
            return p != null && LocationUtil.isWithinSphericalRadius(paint.getLocation(), p.getLocation(), 5);
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (event.getHand() == EquipmentSlot.HAND && event.getRightClicked() instanceof Painting) {
            CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
            Painting paint = (Painting) event.getRightClicked();

            if(!player.hasPermission("craftbook.mech.paintingswitch.use")) {
                if(CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages)
                    player.printError("mech.use-permissions");
                return;
            }

            if(!ProtectionUtil.canBuild(event.getPlayer(), paint.getLocation(), true)) {
                if(CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages)
                    player.printError("area.use-permissions");
                return;
            }

            if (!isBeingEdited(paint)) {
                paintings.put(paint, player.getUniqueId());
                players.put(player.getUniqueId(), paint);
                player.print("mech.painting.editing");
            } else if (paintings.get(paint).equals(player.getUniqueId())) {
                paintings.remove(paint);
                players.remove(player.getUniqueId());
                player.print("mech.painting.stop");
            } else if (isBeingEdited(paint)) {
                player.print(player.translate("mech.painting.used") + ' ' + paintings.get(paint));
            } else {
                return;
            }
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onHeldItemChange(PlayerItemHeldEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (players.get(player.getUniqueId()) == null)
            return;

        if (!player.hasPermission("craftbook.mech.paintingswitch.use")) return;

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
        Painting paint = players.get(player.getUniqueId());
        if(!LocationUtil.isWithinSphericalRadius(paint.getLocation(), event.getPlayer().getLocation(), 5)) {
            player.printError("mech.painting.range");
            Painting p = players.remove(event.getPlayer().getUniqueId());
            if (p != null) {
                paintings.remove(p);
            }

            return;
        }
        int newID = paint.getArt().ordinal() + (isForwards ? 1 : -1);
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
        paintings.put(paint, player.getUniqueId());
        players.put(player.getUniqueId(), paint);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        Painting p = players.remove(event.getPlayer().getUniqueId());
        if (p != null)
            paintings.remove(p);
    }

    @EventHandler
    public void onHangingEntityDestroy(HangingBreakByEntityEvent event) {

        if(event.getEntity() instanceof Painting) {
            UUID uuid = paintings.remove(event.getEntity());

            if(uuid != null) {
                CraftBookPlugin.inst().wrapPlayer(Bukkit.getPlayer(uuid)).print("mech.painting.stop");
                players.remove(uuid);
            }
        }
    }

    @Override
    public void disable () {
        paintings.clear();
        players.clear();
    }

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

    }
}