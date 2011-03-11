package com.sk89q.craftbook.cart;

import java.util.*;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.vehicle.*;
import org.bukkit.util.*;

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
    
    public void reloadConfiguration(VehiclesConfiguration cfg) {
        mechanisms = new EnumMap<Material,CartMechanism>(Material.class);
        mechanisms.put(cfg.matBoostMax, new CartBooster(100));
        
    }
    
    public void handleMinecartBlockChange(VehicleMoveEvent event) {
        Block to = event.getTo().getBlock();
        
        CartMechanism thingy = mechanisms.get(to.getFace(BlockFace.DOWN).getType());
        if (thingy != null) thingy.impact((Minecart)event.getVehicle(), to);
        
        
        
        
        
        
        
        
        
//        if (underType == getConfig().matBoostMax) {
//                
//        } else if (underType == getConfig().matBoost25x) {
//                minecart.setVelocity(minecart.getVelocity().multiply(1.25));
//        } else if (underType == getConfig().matSlow20x) {
//                minecart.setVelocity(minecart.getVelocity().multiply(0.8));
//        } else if (underType == getConfig().matSlow50x) {
//                minecart.setVelocity(minecart.getVelocity().multiply(0.5));
//        } else if (underType == getConfig().matReverse) {
//                minecart.setVelocity((minecart.getVelocity().multiply(-1)));
//        } else if (underType == getConfig().matStation) {
//                //TODO
//        } else if (underType == getConfig().matSorter) {
//        }
}
}
