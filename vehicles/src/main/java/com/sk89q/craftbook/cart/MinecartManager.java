package com.sk89q.craftbook.cart;

import com.sk89q.craftbook.InvalidMechanismException;
import com.sk89q.craftbook.VehiclesConfiguration;
import com.sk89q.craftbook.bukkit.VehiclesPlugin;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Minecart;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import java.util.HashMap;
import java.util.Map;

public class MinecartManager {

    public MinecartManager(VehiclesPlugin plugin) {

        this.plugin = plugin;
        reloadConfiguration(plugin.getLocalConfiguration());
    }

    private final VehiclesPlugin plugin;
    private Map<Integer, CartMechanism> mechanisms;

    public void reloadConfiguration(VehiclesConfiguration cfg) {

        mechanisms = new HashMap<Integer, CartMechanism>();
        if (cfg.matBoostMax > 0) mechanisms.put(cfg.matBoostMax, new CartBooster(100));
        if (cfg.matBoost25x > 0) mechanisms.put(cfg.matBoost25x, new CartBooster(1.25));
        if (cfg.matSlow20x > 0) mechanisms.put(cfg.matSlow20x, new CartBooster(0.8));
        if (cfg.matSlow50x > 0) mechanisms.put(cfg.matSlow50x, new CartBooster(0.5));
        if (cfg.matReverse > 0) mechanisms.put(cfg.matReverse, new CartReverser());
        if (cfg.matSorter > 0) mechanisms.put(cfg.matSorter, new CartSorter());
        if (cfg.matStation > 0) mechanisms.put(cfg.matStation, new CartStation());
        if (cfg.matEjector > 0) mechanisms.put(cfg.matEjector, new CartEjector());
        if (cfg.matDeposit > 0) mechanisms.put(cfg.matDeposit, new CartDeposit());
        if (cfg.matTeleport > 0) mechanisms.put(cfg.matTeleport, new CartTeleporter());
        if (cfg.matLift > 0) mechanisms.put(cfg.matLift, new CartLift());
        if (cfg.matDispenser > 0) mechanisms.put(cfg.matDispenser, new CartDispenser());
        if (cfg.matMessager > 0) mechanisms.put(cfg.matMessager, new CartMessager(plugin));
        for (Map.Entry<Integer, CartMechanism> ent : mechanisms.entrySet()) {
            ent.getValue().setMaterial(ent.getKey());
        }
    }

    public void impact(VehicleMoveEvent event) {

        try {
            CartMechanismBlocks cmb = CartMechanismBlocks.findByRail(event.getTo().getBlock());
            cmb.setFromBlock(event.getFrom().getBlock()); // WAI
            CartMechanism thingy = mechanisms.get(cmb.base.getTypeId());
            if (thingy != null) {
                Location from = event.getFrom();
                Location to = event.getTo();
                boolean crossesBlockBoundary =
                        from.getBlockX() == to.getBlockX()
                        && from.getBlockY() == to.getBlockY()
                        && from.getBlockZ() == to.getBlockZ();
                thingy.impact((Minecart) event.getVehicle(), cmb, crossesBlockBoundary);
            }
        } catch (InvalidMechanismException ignored) {
            /* okay, so there's nothing interesting to see here.  carry on then, eh? */
        }
    }

    public void enter(VehicleEnterEvent event) {

        try {
            Block block = event.getVehicle().getLocation().getBlock();
            CartMechanismBlocks cmb = CartMechanismBlocks.findByRail(block);
            cmb.setFromBlock(block); // WAI
            CartMechanism thingy = mechanisms.get(cmb.base.getTypeId());
            if (thingy != null) {
                Location to = event.getVehicle().getLocation();
                Location from = event.getEntered().getLocation();
                boolean crossesBlockBoundary =
                        from.getBlockX() == to.getBlockX()
                        && from.getBlockY() == to.getBlockY()
                        && from.getBlockZ() == to.getBlockZ();
                thingy.enter((Minecart) event.getVehicle(), event.getEntered(), cmb, crossesBlockBoundary);
            }
        } catch (InvalidMechanismException ignored) {
            /* okay, so there's nothing interesting to see here.  carry on then, eh? */
        }
    }

    public void impact(BlockRedstoneEvent event) {

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DelayedImpact(event));
    }

    /**
     * Bukkit reports redstone events before updating the status of the relevant
     * blocks... which had the rather odd effect of causing only input wires
     * from the north causing the responses intended. Scheduling the impact
     * check one tick later dodges the whole issue.
     */
    private class DelayedImpact implements Runnable {

        public DelayedImpact(BlockRedstoneEvent event) {

            huh = event.getBlock();
        }

        private final Block huh;

        @Override
        public void run() {

            try {
                CartMechanismBlocks cmb = CartMechanismBlocks.find(huh);
                CartMechanism thingy = mechanisms.get(cmb.base.getTypeId());
                if (thingy != null) {
                    thingy.impact(CartMechanism.getCart(cmb.rail), cmb, false);
                }
            } catch (InvalidMechanismException ignored) {
                /* okay, so there's nothing interesting to see here.  carry on then, eh? */
            }
        }
    }
}
