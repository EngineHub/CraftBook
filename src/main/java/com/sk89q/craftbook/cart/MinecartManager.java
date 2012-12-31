package com.sk89q.craftbook.cart;

import com.sk89q.craftbook.bukkit.BukkitConfiguration;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.craftbook.util.exceptions.InvalidMechanismException;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Minecart;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import java.util.HashMap;
import java.util.Map;

public class MinecartManager {

    public MinecartManager() {

        reloadConfiguration();
    }

    private Map<ItemInfo, CartMechanism> mechanisms;

    /**
     * Reloads or sets the configuration of the {@link MinecartManager}.
     *
     * @param cfg - The {@link VehiclesConfiguration} to read
     */
    public void reloadConfiguration() {

        BukkitConfiguration config = CraftBookPlugin.inst().getConfiguration();

        mechanisms = new HashMap<ItemInfo, CartMechanism>();
        if (config.matBoostMax.getId() > 0) mechanisms.put(config.matBoostMax, new CartBooster(100));
        if (config.matBoost25x.getId() > 0) mechanisms.put(config.matBoost25x, new CartBooster(1.25));
        if (config.matSlow20x.getId() > 0) mechanisms.put(config.matSlow20x, new CartBooster(0.8));
        if (config.matSlow50x.getId() > 0) mechanisms.put(config.matSlow50x, new CartBooster(0.5));
        if (config.matReverse.getId() > 0) mechanisms.put(config.matReverse, new CartReverser());
        if (config.matSorter.getId() > 0) mechanisms.put(config.matSorter, new CartSorter());
        if (config.matStation.getId() > 0) mechanisms.put(config.matStation, new CartStation());
        if (config.matEjector.getId() > 0) mechanisms.put(config.matEjector, new CartEjector());
        if (config.matDeposit.getId() > 0) mechanisms.put(config.matDeposit, new CartDeposit());
        if (config.matTeleport.getId() > 0) mechanisms.put(config.matTeleport, new CartTeleporter());
        if (config.matLift.getId() > 0) mechanisms.put(config.matLift, new CartLift());
        if (config.matDispenser.getId() > 0) mechanisms.put(config.matDispenser, new CartDispenser());
        if (config.matMessager.getId() > 0) mechanisms.put(config.matMessager, new CartMessenger());
        for (Map.Entry<ItemInfo, CartMechanism> ent : mechanisms.entrySet()) {
            ent.getValue().setMaterial(ent.getKey());
        }
    }

    /**
     * This method is used to retrieve all {@link CartMechanism}s handled by this manager.
     *
     * @return - A {@link Map} sorted by {@link ItemInfo} keys of all {@link CartMechanism}s
     *         handled by this manager
     */
    public Map<ItemInfo, CartMechanism> getMechanisms() {

        return mechanisms;
    }

    public void impact(VehicleMoveEvent event) {

        try {
            CartMechanismBlocks cmb = CartMechanismBlocks.findByRail(event.getTo().getBlock());
            cmb.setFromBlock(event.getFrom().getBlock()); // WAI
            CartMechanism thingy = mechanisms.get(new ItemInfo(cmb.base.getTypeId(), cmb.base.getData()));
            if (thingy != null) {
                Location from = event.getFrom();
                Location to = event.getTo();
                boolean crossesBlockBoundary = from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY()
                        && from.getBlockZ() == to.getBlockZ();
                thingy.impact((Minecart) event.getVehicle(), cmb, crossesBlockBoundary);
            }
        } catch (InvalidMechanismException ignored) {
            /* okay, so there's nothing interesting to see here. carry on then, eh? */
        }
    }

    public void enter(VehicleEnterEvent event) {

        try {
            Block block = event.getVehicle().getLocation().getBlock();
            CartMechanismBlocks cmb = CartMechanismBlocks.findByRail(block);
            cmb.setFromBlock(block); // WAI
            CartMechanism thingy = mechanisms.get(new ItemInfo(cmb.base.getTypeId(), cmb.base.getData()));
            if (thingy != null) {
                Location to = event.getVehicle().getLocation();
                Location from = event.getEntered().getLocation();
                boolean crossesBlockBoundary = from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY()
                        && from.getBlockZ() == to.getBlockZ();
                thingy.enter((Minecart) event.getVehicle(), event.getEntered(), cmb, crossesBlockBoundary);
            }
        } catch (InvalidMechanismException ignored) {
            /* okay, so there's nothing interesting to see here. carry on then, eh? */
        }
    }

    public void impact(BlockRedstoneEvent event) {

        CraftBookPlugin.server().getScheduler().scheduleSyncDelayedTask(CraftBookPlugin.inst(),
                new DelayedImpact(event));
    }

    /**
     * Bukkit reports redstone events before updating the status of the relevant blocks... which had the rather odd
     * effect of causing only input wires
     * from the north causing the responses intended. Scheduling the impact check one tick later dodges the whole issue.
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
                CartMechanism thingy = mechanisms.get(new ItemInfo(cmb.base.getTypeId(), cmb.base.getData()));
                if (thingy != null) {
                    thingy.impact(CartMechanism.getCart(cmb.rail), cmb, false);
                }
            } catch (InvalidMechanismException ignored) {
                /* okay, so there's nothing interesting to see here. carry on then, eh? */
            }
        }
    }
}
