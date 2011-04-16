package com.sk89q.craftbook.cart;

import java.util.*;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.vehicle.*;

import static com.sk89q.craftbook.cart.CartUtils.*;

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
        mechanisms.put(cfg.matEject,    new CartEjector());
        mechanisms.put(cfg.matTeleport, new CartTeleporter());
    }
    
    public void handleMinecartBlockChange(VehicleMoveEvent event) {
        Block to = event.getTo().getBlock();

        if (to.isBlockPowered()) return;

        CartMechanism thingy = mechanisms.get(to.getFace(BlockFace.DOWN).getType());
        if (thingy != null) thingy.impact((Minecart)event.getVehicle(), to, event.getFrom().getBlock());
    }

    public void handleMinecartEnter(VehicleEnterEvent event) {
        Entity entity = event.getEntered();
        Vehicle cart = event.getVehicle();

        //this is for the autolaunch station
        Block base = cart.getLocation().getBlock().getFace(BlockFace.DOWN);
        if (base.getType() == plugin.getLocalConfiguration().matStation) {
            Block director = pickDirector(base, "station");
            if (director == null) return;
            (new CartStation()).launch(((Minecart) cart), director);
        }
    }
}
