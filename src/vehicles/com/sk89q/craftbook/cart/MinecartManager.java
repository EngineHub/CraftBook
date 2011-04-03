// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.craftbook.cart;

import java.util.*;
import java.util.Map.Entry;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.vehicle.*;
import org.bukkit.util.*;

import static com.sk89q.craftbook.cart.CartUtils.pickDirector;

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.bukkit.*;
import com.sk89q.craftbook.util.*;
import com.sk89q.worldedit.blocks.*;

public class MinecartManager {
    public MinecartManager(VehiclesPlugin plugin) {
        this.plugin = plugin;
        reloadConfiguration(plugin.getLocalConfiguration());
    }

    private VehiclesPlugin plugin;
    private Map<Material,CartMechanism> mechanisms;
    private Map<String,CartMechanism> otherMechanisms;

    public void reloadConfiguration(VehiclesConfiguration cfg) {
        mechanisms = new EnumMap<Material,CartMechanism>(Material.class);
        mechanisms.put(cfg.matBoostMax, new CartBooster(100));
        mechanisms.put(cfg.matBoost25x, new CartBooster(1.25));
        mechanisms.put(cfg.matSlow20x,  new CartBooster(0.8));
        mechanisms.put(cfg.matSlow50x,  new CartBooster(0.5));
        mechanisms.put(cfg.matReverse,  new CartReverser());
        mechanisms.put(cfg.matSorter,   new CartSorter());
        mechanisms.put(cfg.matStation,  new CartStation());
        otherMechanisms = new HashMap<String,CartMechanism>();
        otherMechanisms.put("Print",    new CartMessage(plugin));
    }

    public void handleMinecartBlockChange(VehicleMoveEvent event) {
        Block to = event.getTo().getBlock();

        CartMechanism matchedMech = mechanisms.get(to.getFace(BlockFace.DOWN).getType());
        if (matchedMech != null) {
            matchedMech.impact((Minecart)event.getVehicle(), to, event.getFrom().getBlock());
            return;
        }
        for (Iterator<Entry<String,CartMechanism>> it = otherMechanisms.entrySet().iterator(); it.hasNext();) {
            Entry<String,CartMechanism> nextMech = it.next();
            Block director = pickDirector(to.getFace(BlockFace.DOWN, 1), nextMech.getKey());
            if (director == null) return;
            matchedMech = nextMech.getValue();
            matchedMech.impact((Minecart)event.getVehicle(), to, event.getFrom().getBlock());
        }
    }
}
