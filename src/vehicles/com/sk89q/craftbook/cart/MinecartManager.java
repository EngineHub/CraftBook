package com.sk89q.craftbook.cart;

import java.util.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.block.*;
import org.bukkit.event.vehicle.*;
import com.sk89q.craftbook.*;
import com.sk89q.craftbook.bukkit.*;

public class MinecartManager {
    public MinecartManager(VehiclesPlugin plugin) {
        this.plugin = plugin;
        reloadConfiguration(plugin.getLocalConfiguration());
    }
    
    private VehiclesPlugin plugin;
    private Map<Material,CartMechanism> mechanisms;
    
    public void reloadConfiguration(VehiclesConfiguration cfg) {
        mechanisms = new EnumMap<Material,CartMechanism>(Material.class);
        mechanisms.put(cfg.matBoostMax, new CartBooster(100));
        mechanisms.put(cfg.matBoost25x, new CartBooster(1.25));
        mechanisms.put(cfg.matSlow20x,  new CartBooster(0.8));
        mechanisms.put(cfg.matSlow50x,  new CartBooster(0.5));
        mechanisms.put(cfg.matReverse,  new CartReverser());
        mechanisms.put(cfg.matSorter,   new CartSorter());
        mechanisms.put(cfg.matStation,  new CartStation());
        for (Map.Entry<Material,CartMechanism> ent : mechanisms.entrySet())
            ent.getValue().setMaterial(ent.getKey());
    }
    
    public void impact(BlockRedstoneEvent event) {
        try {
            CartMechanismBlocks cmb = CartMechanismBlocks.findByRail(event.getBlock());
            CartMechanism thingy = mechanisms.get(cmb.base.getType());
            if (thingy != null)
                thingy.impact(CartMechanism.getCart(cmb.rail), cmb);
        } catch (InvalidMechanismException e) {
            /* okay, so there's nothing interesting to see here.  carry on then, eh? */
            return;
        }
    }
    
    public void impact(VehicleMoveEvent event) {
        try {
            CartMechanismBlocks cmb = CartMechanismBlocks.findByRail(event.getTo().getBlock());
            CartMechanism thingy = mechanisms.get(cmb.base.getType());
            if (thingy != null)
                thingy.impact((Minecart)event.getVehicle(), cmb);
        } catch (InvalidMechanismException e) {
            /* okay, so there's nothing interesting to see here.  carry on then, eh? */
            return;
        }
    }
}
