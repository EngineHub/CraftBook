// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

package com.sk89q.craftbook.bukkit;

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.bukkit.BukkitMetrics.Graph;
import com.sk89q.craftbook.bukkit.commands.VehicleCommands;
import com.sk89q.craftbook.cart.CartMechanism;
import com.sk89q.craftbook.cart.MinecartManager;
import com.sk89q.craftbook.util.GeneralUtil;
import com.sk89q.worldedit.blocks.ItemID;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.*;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.entity.PoweredMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.vehicle.*;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

/**
 * Plugin for CraftBook's redstone additions.
 *
 * @author sk89q
 */
public class VehiclesPlugin extends BaseBukkitPlugin {

    private static VehiclesPlugin instance;
    private VehiclesConfiguration config;
    private MinecartManager cartman;

    private Map<String, String> stationSelection;

    @Override
    public void onEnable() {

        instance = this;

        stationSelection = new HashMap<String, String>();

        super.onEnable();

        createDefaultConfiguration("config.yml", false);

        // config has to be loaded before the listeners are built because they cache stuff
        config = new VehiclesConfiguration(getConfig(), getDataFolder());
        saveConfig();

        cartman = new MinecartManager(this);

        // Register events
        registerEvents();

        languageManager = new LanguageManager(this);

        registerCommand(VehicleCommands.class);

        try {
            BukkitMetrics metrics = new BukkitMetrics(this);

            Graph graph = metrics.createGraph("Language");
            for (String lan : languageManager.getLanguages()) {
                graph.addPlotter(new BukkitMetrics.Plotter(lan) {

                    @Override
                    public int getValue() {

                        return 1;
                    }
                });
            }

            metrics.start();
        } catch (Exception e) {
            getLogger().severe(GeneralUtil.getStackTrace(e));
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

    public static VehiclesPlugin getInstance() {

        return instance;
    }

    /**
     * Preprocesses event data coming directly from bukkit and passes it off to appropriate logic in MinecartManager.
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

            if (!config.boatRemoveEntities && !config.minecartRemoveEntities && !config.minecartEnterOnImpact) return;

            if (config.minecartEnterOnImpact && vehicle instanceof Minecart) {
                if (!vehicle.isEmpty()) return;
		if (vehicle instanceof StorageMinecart) return;
		if (vehicle instanceof PoweredMinecart) return;
                if (!(event.getEntity() instanceof LivingEntity)) return;
                vehicle.setPassenger(event.getEntity());

                return;
            }

            if (config.boatRemoveEntities && vehicle instanceof Boat) {
                if (!config.boatRemoveEntitiesOtherBoats && entity instanceof Boat) return;

                if (entity instanceof LivingEntity) {
                    ((LivingEntity) entity).damage(5);
                    entity.setVelocity(vehicle.getVelocity().multiply(2));
                } else entity.remove();

                return;
            }

            if (config.minecartRemoveEntities && vehicle instanceof Minecart) {
                if (!config.minecartRemoveEntitiesOtherCarts && entity instanceof Minecart) return;

                if (entity instanceof LivingEntity) {
                    ((LivingEntity) entity).damage(5);
                    entity.setVelocity(vehicle.getVelocity().multiply(2));
                } else entity.remove();
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
            if (config.minecartOffRailSpeedModifier > 0)
                minecart.setDerailedVelocityMod(new Vector(config.minecartOffRailSpeedModifier,
                        config.minecartOffRailSpeedModifier,
                        config.minecartOffRailSpeedModifier));
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

            if (config.minecartConstantSpeed > 0 && RailUtil.isTrack(event.getTo().getBlock().getTypeId())
                    && event.getVehicle().getVelocity().lengthSquared() > 0) {
                Vector vel = event.getVehicle().getVelocity();
                event.getVehicle().setVelocity(vel.normalize().multiply(config.minecartConstantSpeed));
            }

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
                for (CartMechanism mech : cartman.getMechanisms().values()) {
                    if (mech.getApplicableSigns() == null) continue;
                    boolean found = false;
                    String lineFound = null;
                    int lineNum = 1;
                    for (String sign : mech.getApplicableSigns()) {
                        if (lines[1].equalsIgnoreCase("[" + sign + "]")) {
                            found = true;
                            lineFound = sign;
                            lineNum = 1;
                            break;
                        } else if (mech.getName().equalsIgnoreCase("messager") && lines[0].equalsIgnoreCase("[" +
                                sign + "]")) {
                            found = true;
                            lineFound = sign;
                            lineNum = 0;
                            break;
                        }
                    }
                    if (!found) continue;
                    if (!mech.verify(BukkitUtil.toChangedSign((Sign) event.getBlock().getState(), lines), player)) {
                        block.breakNaturally();
                        event.setCancelled(true);
                        return;
                    }
                    player.checkPermission("craftbook.vehicles." + mech.getName().toLowerCase());
                    event.setLine(lineNum, "[" + lineFound + "]");
                    player.print(mech.getName() + " Created!");
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

        reloadConfig();
        config = new VehiclesConfiguration(getConfig(), getDataFolder());
        cartman.reloadConfiguration(config);
        saveConfig();
    }

    /**
     * Sets a player's station, used by sorter mechanism
     *
     * @param player  Player name to set station for
     * @param station station name to set
     */
    public void setStation(String player, String station) {

        stationSelection.put(player, station);
    }

    /**
     * Get the station a player has chosen
     *
     * @param player player name to get station for
     *
     * @return name of station, or null if not set
     */
    public String getStation(String player) {

        return stationSelection.get(player);
    }

    static class Decay implements Runnable {

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