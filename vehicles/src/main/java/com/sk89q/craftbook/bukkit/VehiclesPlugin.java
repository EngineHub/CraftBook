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

package com.sk89q.craftbook.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.InsufficientPermissionsException;
import com.sk89q.craftbook.LanguageManager;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.SourcedBlockRedstoneEvent;
import com.sk89q.craftbook.VehiclesConfiguration;
import com.sk89q.craftbook.bukkit.Metrics.Graph;
import com.sk89q.craftbook.cart.CartMechanism;
import com.sk89q.craftbook.cart.MinecartManager;
import com.sk89q.worldedit.blocks.ItemID;

/**
 * Plugin for CraftBook's redstone additions.
 *
 * @author sk89q
 */
public class VehiclesPlugin extends BaseBukkitPlugin {

    private VehiclesConfiguration config;
    private MinecartManager cartman;

    @Override
    public void onEnable() {

        super.onEnable();

        createDefaultConfiguration("config.yml", false);

        // config has to be loaded before the listeners are built because they cache stuff
        config = new VehiclesConfiguration(getConfig(), getDataFolder());
        saveConfig();

        cartman = new MinecartManager(this);

        // Register events
        registerEvents();

        languageManager = new LanguageManager(this);

        try {
            Metrics metrics = new Metrics(this);

            Graph graph = metrics.createGraph("Language");
            for (String lan : languageManager.getLanguages()) {
                graph.addPlotter(new Metrics.Plotter(lan) {

                    @Override
                    public int getValue() {

                        return 1;
                    }
                });
            }

            metrics.start();
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void registerEvents() {

        getServer().getPluginManager().registerEvents(new CraftBookVehicleListener(this), this);
        getServer().getPluginManager().registerEvents(new CraftBookVehicleBlockListener(this), this);
    }

    @Override
    public VehiclesConfiguration getLocalConfiguration() {

        return config;
    }


    /**
     * Preprocesses event data coming directly from bukkit and passes it off to
     * appropriate logic in MinecartManager.
     */
    class CraftBookVehicleListener implements Listener {

        VehiclesPlugin plugin;

        public CraftBookVehicleListener(VehiclesPlugin plugin) {

            this.plugin = plugin;
        }

        /**
         * Called when a vehicle hits an entity
         */
        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {

            VehiclesConfiguration config = getLocalConfiguration();
            Vehicle vehicle = event.getVehicle();
            Entity entity = event.getEntity();

            if (entity instanceof Player) return;
            if (!config.boatRemoveEntities && !config.minecartRemoveEntities && !config.minecartEnterOnImpact) return;

            if (config.minecartEnterOnImpact && vehicle instanceof Minecart) {
                if (!vehicle.isEmpty()) return;
                if (!(event.getEntity() instanceof LivingEntity)) return;
                vehicle.setPassenger(event.getEntity());

                return;
            }

            if (config.boatRemoveEntities && vehicle instanceof Boat) {
                if (!config.boatRemoveEntitiesOtherBoats &&
                        entity instanceof Boat) return;

                entity.remove();

                return;
            }

            if (config.minecartRemoveEntities && vehicle instanceof Minecart) {
                if (!config.minecartRemoveEntitiesOtherCarts &&
                        entity instanceof Minecart) return;

                entity.remove();
            }
        }

        /**
         * Called when a vehicle is created.
         */
        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onVehicleCreate(VehicleCreateEvent event) {

            Vehicle vehicle = event.getVehicle();

            // Ignore events not relating to minecrarts.
            if (!(vehicle instanceof Minecart)) return;

            // Modify the vehicle properties according to config.
            VehiclesConfiguration config = getLocalConfiguration();
            Minecart minecart = (Minecart) vehicle;
            minecart.setSlowWhenEmpty(config.minecartSlowWhenEmpty);
            minecart.setMaxSpeed(minecart.getMaxSpeed() * config.minecartMaxSpeedModifier);
        }

        /**
         * Called when a vehicle is exited
         */

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onVehicleEnter(VehicleEnterEvent event) {

            Vehicle vehicle = event.getVehicle();

            if (!(vehicle instanceof Minecart)) return;

            cartman.enter(event);
        }

        /**
         * Called when a vehicle is exited
         */

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onVehicleExit(VehicleExitEvent event) {

            Vehicle vehicle = event.getVehicle();

            if (!(vehicle instanceof Minecart)) return;

            VehiclesConfiguration config = getLocalConfiguration();
            if (config.minecartRemoveOnExit) {
                vehicle.remove();
            } else if (config.minecartDecayWhenEmpty) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Decay((Minecart) vehicle),
                        config.minecartDecayTime);
            }
        }

        /**
         * Called when an vehicle moves.
         */
        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onVehicleMove(VehicleMoveEvent event) {
            // Ignore events not relating to minecarts.
            if (!(event.getVehicle() instanceof Minecart)) return;

            cartman.impact(event);
        }

        /**
         * Called when a vehicle is destroied.
         */
        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onVehicleDestroy(VehicleDestroyEvent event) {

            if (!(event.getVehicle() instanceof Boat)) return;

            VehiclesConfiguration config = getLocalConfiguration();

            if (config.boatNoCrash && event.getAttacker() == null) {
                event.getVehicle().setVelocity(new Vector(0, 0, 0));
                event.setCancelled(true);
            } else if (config.boatBreakReturn && event.getAttacker() == null) {
                Boat boat = (Boat) event.getVehicle();
                boat.getLocation().getWorld().dropItemNaturally(boat.getLocation(), new ItemStack(ItemID.WOOD_BOAT));
                boat.remove();
                event.setCancelled(true);
            }
        }
    }


    class CraftBookVehicleBlockListener implements Listener {

        VehiclesPlugin plugin;

        public CraftBookVehicleBlockListener(VehiclesPlugin plugin) {

            this.plugin = plugin;
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onBlockRedstoneChange(BlockRedstoneEvent event) {
            // ignore events that are only changes in current strength
            if (event.getOldCurrent() > 0 == event.getNewCurrent() > 0) return;

            // remember that bukkit only gives us redstone events for wires and things that already respond to
            // redstone, which is entirely unhelpful.
            // So: issue four actual events per bukkit event.
            for (BlockFace bf : CartMechanism.powerSupplyOptions) {
                cartman.impact(new SourcedBlockRedstoneEvent(event, event.getBlock().getRelative(bf)));
            }
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onChunkLoad(ChunkLoadEvent event) {

            if (config.minecartDecayWhenEmpty) {
                for (Entity ent : event.getChunk().getEntities()) {
                    if (ent == null || ent.isDead()) {
                        continue;
                    }
                    if (!(ent instanceof Minecart)) {
                        continue;
                    }
                    if (!ent.isEmpty()) {
                        continue;
                    }
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Decay((Minecart) ent),
                            config.minecartDecayTime);
                }
            }
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onSignChange(SignChangeEvent event) {

            Block block = event.getBlock();
            String[] lines = event.getLines();
            LocalPlayer player = wrap(event.getPlayer());

            try {
                if (lines[1].equalsIgnoreCase("[deposit]") || lines[1].equalsIgnoreCase("[collect]")) {
                    player.checkPermission("craftbook.vehicles.deposit");
                } else if (lines[1].equalsIgnoreCase("[dispenser]")) {
                    player.checkPermission("craftbook.vehicles.dispenser");
                } else if (lines[1].equalsIgnoreCase("[eject]")) {
                    player.checkPermission("craftbook.vehicles.eject");
                } else if (lines[1].equalsIgnoreCase("[print]")) {
                    player.checkPermission("craftbook.vehicles.print");
                } else if (lines[1].equalsIgnoreCase("[reverse]")) {
                    player.checkPermission("craftbook.vehicles.reverse");
                } else if (lines[1].equalsIgnoreCase("[sort]")) {
                    player.checkPermission("craftbook.vehicles.sort");
                } else if (lines[1].equalsIgnoreCase("[station]")) {
                    player.checkPermission("craftbook.vehicles.station");
                } else if (lines[1].equalsIgnoreCase("[teleport]")) {
                    player.checkPermission("craftbook.vehicles.teleport");
                }
            } catch (InsufficientPermissionsException e) {
                player.printError("vehicles.create-permission");
                block.breakNaturally();
                event.setCancelled(true);
            }
        }
    }

    @Override
    public void reloadConfiguration() {
        config = new VehiclesConfiguration(getConfig(), getDataFolder());
        saveConfig();
    }

    class Decay implements Runnable {

        Minecart cart;

        public Decay(Minecart cart) {

            this.cart = cart;
        }

        @Override
        public void run() {

            if (cart.isEmpty()) {
                cart.setDamage(41);
            }
        }

    }
}