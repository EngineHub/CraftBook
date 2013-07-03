package com.sk89q.craftbook.bukkit;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Attachable;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.LocalComponent;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.commands.VehicleCommands;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.RailUtil;
import com.sk89q.craftbook.util.exceptions.InsufficientPermissionsException;
import com.sk89q.craftbook.vehicles.cart.CartBlockMechanism;
import com.sk89q.craftbook.vehicles.cart.CartBooster;
import com.sk89q.craftbook.vehicles.cart.CartDeposit;
import com.sk89q.craftbook.vehicles.cart.CartDispenser;
import com.sk89q.craftbook.vehicles.cart.CartEjector;
import com.sk89q.craftbook.vehicles.cart.CartLift;
import com.sk89q.craftbook.vehicles.cart.CartMessenger;
import com.sk89q.craftbook.vehicles.cart.CartReverser;
import com.sk89q.craftbook.vehicles.cart.CartSorter;
import com.sk89q.craftbook.vehicles.cart.CartStation;
import com.sk89q.craftbook.vehicles.cart.CartTeleporter;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.blocks.ItemID;

/**
 * Author: Turtle9598
 */
public class VehicleCore implements LocalComponent {

    private static VehicleCore instance;

    private CraftBookPlugin plugin = CraftBookPlugin.inst();

    private Map<String, String> stationSelection;

    protected Vector minecartFallSpeed;

    public static boolean isEnabled() {

        return instance != null;
    }

    public VehicleCore() {

        instance = this;
    }

    public static VehicleCore inst() {

        return instance;
    }

    @Override
    public void enable() {

        plugin.registerCommands(VehicleCommands.class);

        stationSelection = new HashMap<String, String>();

        // Register events
        registerEvents();

        if(plugin.getConfiguration().minecartFallModifierEnabled)
            minecartFallSpeed = new Vector(plugin.getConfiguration().minecartFallHorizontalSpeed, plugin.getConfiguration().minecartFallVerticalSpeed, plugin.getConfiguration().minecartFallHorizontalSpeed);
    }

    @Override
    public void disable() {

        instance = null;
    }

    private HashSet<CartBlockMechanism> cartBlockMechanisms = new HashSet<CartBlockMechanism>();

    protected void registerEvents() {

        if(plugin.getConfiguration().minecartSpeedModEnabled) {
            if(plugin.getConfiguration().minecartSpeedModMaxBoostBlock.getId() > 0)
                cartBlockMechanisms.add(new CartBooster(plugin.getConfiguration().minecartSpeedModMaxBoostBlock, 100));
            if(plugin.getConfiguration().minecartSpeedMod25xBoostBlock.getId() > 0)
                cartBlockMechanisms.add(new CartBooster(plugin.getConfiguration().minecartSpeedMod25xBoostBlock, 1.25));
            if(plugin.getConfiguration().minecartSpeedMod20xSlowBlock.getId() > 0)
                cartBlockMechanisms.add(new CartBooster(plugin.getConfiguration().minecartSpeedMod20xSlowBlock, 0.8));
            if(plugin.getConfiguration().minecartSpeedMod50xSlowBlock.getId() > 0)
                cartBlockMechanisms.add(new CartBooster(plugin.getConfiguration().minecartSpeedMod50xSlowBlock, 0.5));
        }
        if(plugin.getConfiguration().minecartReverseEnabled && plugin.getConfiguration().minecartReverseBlock.getId() > 0)
            cartBlockMechanisms.add(new CartReverser(plugin.getConfiguration().minecartReverseBlock));
        if(plugin.getConfiguration().minecartSorterEnabled && plugin.getConfiguration().minecartSorterBlock.getId() > 0)
            cartBlockMechanisms.add(new CartSorter(plugin.getConfiguration().minecartSorterBlock));
        if(plugin.getConfiguration().minecartStationEnabled && plugin.getConfiguration().minecartStationBlock.getId() > 0)
            cartBlockMechanisms.add(new CartStation(plugin.getConfiguration().minecartStationBlock));
        if(plugin.getConfiguration().minecartEjectorEnabled && plugin.getConfiguration().minecartEjectorBlock.getId() > 0)
            cartBlockMechanisms.add(new CartEjector(plugin.getConfiguration().minecartEjectorBlock));
        if(plugin.getConfiguration().minecartDepositEnabled && plugin.getConfiguration().minecartDepositBlock.getId() > 0)
            cartBlockMechanisms.add(new CartDeposit(plugin.getConfiguration().minecartDepositBlock));
        if(plugin.getConfiguration().minecartTeleportEnabled && plugin.getConfiguration().minecartTeleportBlock.getId() > 0)
            cartBlockMechanisms.add(new CartTeleporter(plugin.getConfiguration().minecartTeleportBlock));
        if(plugin.getConfiguration().minecartElevatorEnabled && plugin.getConfiguration().minecartElevatorBlock.getId() > 0)
            cartBlockMechanisms.add(new CartLift(plugin.getConfiguration().minecartElevatorBlock));
        if(plugin.getConfiguration().minecartDispenserEnabled && plugin.getConfiguration().minecartDispenserBlock.getId() > 0)
            cartBlockMechanisms.add(new CartDispenser(plugin.getConfiguration().minecartDispenserBlock));
        if(plugin.getConfiguration().minecartMessagerEnabled && plugin.getConfiguration().minecartMessagerBlock.getId() > 0)
            cartBlockMechanisms.add(new CartMessenger(plugin.getConfiguration().minecartMessagerBlock));

        for(CartBlockMechanism mech : cartBlockMechanisms)
            plugin.getServer().getPluginManager().registerEvents(mech, plugin);

        plugin.getServer().getPluginManager().registerEvents(new CraftBookVehicleListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new CraftBookVehicleBlockListener(), plugin);
    }

    public String getStation(String playerName) {

        return stationSelection.get(playerName);
    }

    public void setStation(String playerName, String stationName) {

        stationSelection.put(playerName, stationName);
    }

    class CraftBookVehicleListener implements Listener {

        /**
         * Called when a vehicle hits an entity
         */
        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {

            Vehicle vehicle = event.getVehicle();
            Entity entity = event.getEntity();

            enterOnImpact: {
                if (plugin.getConfiguration().minecartEnterOnImpact && vehicle instanceof RideableMinecart) {
                    if (!vehicle.isEmpty()) break enterOnImpact;
                    if (!(event.getEntity() instanceof LivingEntity)) break enterOnImpact;
                    vehicle.setPassenger(event.getEntity());

                    event.setCollisionCancelled(true);
                    return;
                }
            }

            if (plugin.getConfiguration().minecartPickupItemsOnCollision && vehicle instanceof StorageMinecart && event.getEntity() instanceof Item) {

                StorageMinecart cart = (StorageMinecart) vehicle;
                Collection<ItemStack> leftovers = cart.getInventory().addItem(((Item) entity).getItemStack()).values();
                if(leftovers.isEmpty())
                    entity.remove();
                else
                    ((Item) entity).setItemStack(leftovers.toArray(new ItemStack[1])[0]);

                event.setCollisionCancelled(true);
                return;
            }

            boatRemoveEntities: {
                if (plugin.getConfiguration().boatRemoveEntities && vehicle instanceof Boat) {
                    if (!plugin.getConfiguration().boatRemoveEntitiesOtherBoats && (entity instanceof Boat || entity.isInsideVehicle())) break boatRemoveEntities;

                    if(vehicle.isEmpty())
                        break boatRemoveEntities;

                    if (entity instanceof LivingEntity) {
                        if(entity.isInsideVehicle())
                            break boatRemoveEntities;
                        ((LivingEntity) entity).damage(10);
                        entity.setVelocity(vehicle.getVelocity().normalize().multiply(1.8).add(new Vector(0,0.5,0)));
                    } else if (entity instanceof Vehicle) {

                        if(!entity.isEmpty())
                            break boatRemoveEntities;
                        else
                            entity.remove();
                    } else
                        entity.remove();

                    event.setCancelled(true);
                    event.setPickupCancelled(true);
                    event.setCollisionCancelled(true);
                    return;
                }
            }

            minecartRemoveEntities: {
                if (plugin.getConfiguration().minecartRemoveEntities && vehicle instanceof Minecart) {
                    if (!plugin.getConfiguration().minecartRemoveEntitiesOtherCarts && (entity instanceof Minecart || entity.isInsideVehicle())) break minecartRemoveEntities;

                    if(vehicle instanceof RideableMinecart && vehicle.isEmpty())
                        break minecartRemoveEntities;

                    if (entity instanceof LivingEntity) {
                        if(entity.isInsideVehicle())
                            break minecartRemoveEntities;
                        ((LivingEntity) entity).damage(10);
                        entity.setVelocity(vehicle.getVelocity().normalize().multiply(1.8).add(new Vector(0,0.5,0)));
                    } else if (entity instanceof Vehicle) {

                        if(!entity.isEmpty())
                            break minecartRemoveEntities;
                        else
                            entity.remove();
                    } else
                        entity.remove();

                    event.setCancelled(true);
                    event.setPickupCancelled(true);
                    event.setCollisionCancelled(true);
                    return;
                }
            }
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onPlayerMove(PlayerMoveEvent event) {

            if(!event.getPlayer().isInsideVehicle())
                return;

            if(!(event.getPlayer().getVehicle() instanceof Minecart))
                return;

            if(!plugin.getConfiguration().minecartLookDirection)
                return;

            if(Math.abs(event.getFrom().getYaw() - event.getTo().getYaw()) < 3)
                return;

            if(RailUtil.isTrack(event.getPlayer().getVehicle().getLocation().getBlock().getTypeId()))
                return;

            Vector direction = event.getPlayer().getLocation().getDirection();
            direction = direction.normalize();
            direction.setY(0);
            direction = direction.multiply(event.getPlayer().getVehicle().getVelocity().length());
            direction.setY(event.getPlayer().getVehicle().getVelocity().getY());
            event.getPlayer().getVehicle().setVelocity(direction);
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
            Minecart minecart = (Minecart) vehicle;
            minecart.setSlowWhenEmpty(plugin.getConfiguration().minecartSlowWhenEmpty);
            if (plugin.getConfiguration().minecartOffRailSpeedModifier > 0)
                minecart.setDerailedVelocityMod(new Vector(plugin.getConfiguration().minecartOffRailSpeedModifier, plugin.getConfiguration().minecartOffRailSpeedModifier, plugin.getConfiguration().minecartOffRailSpeedModifier));
            minecart.setMaxSpeed(minecart.getMaxSpeed() * plugin.getConfiguration().minecartMaxSpeedModifier);
            if (plugin.getConfiguration().minecartFallModifierEnabled && minecartFallSpeed != null)
                minecart.setFlyingVelocityMod(minecartFallSpeed);
        }

        /**
         * Called when a vehicle is exited
         */

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onVehicleEnter(VehicleEnterEvent event) {

            if(!event.getVehicle().getWorld().isChunkLoaded(event.getVehicle().getLocation().getBlockX() >> 4, event.getVehicle().getLocation().getBlockZ() >> 4))
                return;

            Vehicle vehicle = event.getVehicle();

            if (!(vehicle instanceof Minecart)) return;

            if(plugin.getConfiguration().minecartBlockAnimalEntry && !(event.getEntered() instanceof Player)) {
                event.setCancelled(true);
                return;
            }
        }

        /**
         * Called when a vehicle is exited
         */

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onVehicleExit(VehicleExitEvent event) {

            Vehicle vehicle = event.getVehicle();

            if (!(vehicle instanceof Minecart)) return;

            if (plugin.getConfiguration().minecartRemoveOnExit) {
                vehicle.remove();
            } else if (plugin.getConfiguration().minecartDecayWhenEmpty) {
                plugin.getServer().getScheduler().runTaskLater(plugin, new Decay((Minecart) vehicle), plugin.getConfiguration().minecartDecayTime);
            }
        }

        /**
         * Called when an vehicle moves.
         */
        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onVehicleMove(VehicleMoveEvent event) {
            // Ignore events not relating to minecarts.
            if (!(event.getVehicle() instanceof Minecart)) return;

            if (plugin.getConfiguration().minecartFallModifierEnabled && minecartFallSpeed != null)
                ((Minecart) event.getVehicle()).setFlyingVelocityMod(minecartFallSpeed);

            if (plugin.getConfiguration().minecartStoragePlaceRails && event.getVehicle() instanceof StorageMinecart) {

                if(event.getTo().getBlock().getTypeId() == 0 && !BlockType.canPassThrough(event.getTo().getBlock().getRelative(0, -1, 0).getTypeId()) && ((StorageMinecart)event.getVehicle()).getInventory().contains(BlockID.MINECART_TRACKS)) {

                    ((StorageMinecart)event.getVehicle()).getInventory().remove(new ItemStack(BlockID.MINECART_TRACKS, 1));
                    event.getTo().getBlock().setTypeId(BlockID.MINECART_TRACKS);
                }
            }

            if (plugin.getConfiguration().minecartPressurePlateIntersection)
                if (event.getTo().getBlock().getTypeId() == BlockID.STONE_PRESSURE_PLATE || event.getTo().getBlock().getTypeId() == BlockID.WOODEN_PRESSURE_PLATE || event.getTo().getBlock().getTypeId() == BlockID.PRESSURE_PLATE_HEAVY || event.getTo().getBlock().getTypeId() == BlockID.PRESSURE_PLATE_LIGHT)
                    event.getVehicle().setVelocity(event.getVehicle().getVelocity().normalize().multiply(4));

            if (plugin.getConfiguration().minecartVerticalRail)
                if (event.getTo().getBlock().getTypeId() == BlockID.LADDER)
                    event.getVehicle().setVelocity(event.getVehicle().getVelocity().add(new Vector(((Attachable) event.getTo().getBlock().getState().getData()).getAttachedFace().getModX(),0.5,((Attachable) event.getTo().getBlock().getState().getData()).getAttachedFace().getModY())));

            if (plugin.getConfiguration().minecartConstantSpeed > 0 && RailUtil.isTrack(event.getTo().getBlock()
                    .getTypeId())
                    && event.getVehicle().getVelocity().lengthSquared() > 0) {
                Vector vel = event.getVehicle().getVelocity();
                event.getVehicle().setVelocity(vel.normalize().multiply(plugin.getConfiguration()
                        .minecartConstantSpeed));
            }
        }

        /**
         * Called when a vehicle is destroied.
         */
        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onVehicleDestroy(VehicleDestroyEvent event) {

            if (!(event.getVehicle() instanceof Boat)) return;

            if (plugin.getConfiguration().boatNoCrash && event.getAttacker() == null) {
                event.getVehicle().setVelocity(new Vector(0, 0, 0));
                event.setCancelled(true);
            } else if (plugin.getConfiguration().boatBreakReturn && event.getAttacker() == null) {
                Boat boat = (Boat) event.getVehicle();
                boat.getLocation().getWorld().dropItemNaturally(boat.getLocation(), new ItemStack(ItemID.WOOD_BOAT));
                boat.remove();
                event.setCancelled(true);
            }
        }
    }

    class CraftBookVehicleBlockListener implements Listener {

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onChunkLoad(ChunkLoadEvent event) {

            if (plugin.getConfiguration().minecartDecayWhenEmpty) {
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
                    plugin.getServer().getScheduler().runTaskLater(plugin, new Decay((Minecart) ent), plugin.getConfiguration().minecartDecayTime);
                }
            }
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onSignChange(SignChangeEvent event) {

            Block block = event.getBlock();
            String[] lines = event.getLines();
            LocalPlayer player = plugin.wrapPlayer(event.getPlayer());

            try {
                for (CartBlockMechanism mech : cartBlockMechanisms) {
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
                        } else if (mech.getName().equalsIgnoreCase("messager") && lines[0].equalsIgnoreCase("[" + sign + "]")) {
                            found = true;
                            lineFound = sign;
                            lineNum = 0;
                            break;
                        }
                    }
                    if (!found) continue;
                    if (!mech.verify(BukkitUtil.toChangedSign((Sign) event.getBlock().getState(), lines, player), player)) {
                        block.breakNaturally();
                        event.setCancelled(true);
                        return;
                    }
                    player.checkPermission("craftbook.vehicles." + mech.getName().toLowerCase(Locale.ENGLISH));
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

    static class Decay implements Runnable {

        Minecart cart;

        public Decay(Minecart cart) {

            this.cart = cart;
        }

        @Override
        public void run() {

            if (cart.isEmpty())
                cart.remove();
        }
    }
}