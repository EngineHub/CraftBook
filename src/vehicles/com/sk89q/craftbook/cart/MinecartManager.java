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
