package com.sk89q.craftbook.cart;

import java.util.*;
import java.util.Map.Entry;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.vehicle.*;
import org.bukkit.util.*;
import org.bukkit.inventory.ItemStack;

import static com.sk89q.craftbook.cart.CartUtils.*;

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
    private Map<String,CartMechanism> anyBlockMechanisms;
    
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

        anyBlockMechanisms = new HashMap<String,CartMechanism>();
        anyBlockMechanisms.put("print", new CartMessageEmitter(plugin));
    }
    
    public void handleMinecartBlockChange(VehicleMoveEvent event) {
        Block to = event.getTo().getBlock();

        if (to.isBlockPowered()) return;
        CartMechanism matchedMech = mechanisms.get(to.getFace(BlockFace.DOWN).getType());
        if (matchedMech != null) {
            matchedMech.impact((Minecart)event.getVehicle(), to, event.getFrom().getBlock());
        } else {
            if (!(to.getFace(BlockFace.DOWN, 1).getState() instanceof Sign)
                  && !(to.getFace(BlockFace.DOWN, 2).getState() instanceof Sign)) {
                return;
            }
            for (Iterator<Entry<String,CartMechanism>> it = anyBlockMechanisms.entrySet().iterator(); it.hasNext();) {
                Entry<String,CartMechanism> nextMech = it.next();
                Block director = pickDirector(to.getFace(BlockFace.DOWN, 1), nextMech.getKey(), -1);
                if (director == null) return;
                matchedMech = nextMech.getValue();
                matchedMech.impact((Minecart)event.getVehicle(), to, event.getFrom().getBlock());
            }
        }
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

    public void handleMinecartExit(VehicleExitEvent event) {
        Entity entity = event.getExited();
        Vehicle cart = event.getVehicle();

        if (event.isCancelled()) return;

        // destroy/drop on exit
        if (plugin.getLocalConfiguration().minecartDestroyOnExit) {
            if (!(plugin.getLocalConfiguration().minecartDestroyOnCreature)
                 && !(entity instanceof Player)) return;
            cart.remove();
            if (plugin.getLocalConfiguration().minecartDropOnExit) {
                cart.getWorld().dropItem(cart.getLocation(), new ItemStack(328, 1));
            }
        }
    }
}

