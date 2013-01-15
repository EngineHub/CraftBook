package com.sk89q.craftbook.bukkit;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.PoweredMinecart;
import org.bukkit.entity.StorageMinecart;
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

import com.sk89q.craftbook.LocalComponent;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.SourcedBlockRedstoneEvent;
import com.sk89q.craftbook.bukkit.commands.VehicleCommands;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.cart.CartMechanism;
import com.sk89q.craftbook.cart.MinecartManager;
import com.sk89q.craftbook.util.RailUtil;
import com.sk89q.craftbook.util.exceptions.InsufficientPermissionsException;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.blocks.ItemID;

/**
 * Author: Turtle9598
 */
public class VehicleCore implements LocalComponent {

    private static VehicleCore instance;

    private CraftBookPlugin plugin = CraftBookPlugin.inst();

    private MinecartManager cartman;

    private Map<String, String> stationSelection;

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

        cartman = new MinecartManager();

        // Register events
        registerEvents();
    }

    @Override
    public void disable() {

        // Nothing to do at the current time
    }

    protected void registerEvents() {

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

        public CraftBookVehicleListener() {

        }

        /**
         * Called when a vehicle hits an entity
         */
        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {

            Vehicle vehicle = event.getVehicle();
            Entity entity = event.getEntity();

            enterOnImpact: {
                if (plugin.getConfiguration().minecartEnterOnImpact && vehicle instanceof Minecart) {
                    if (!vehicle.isEmpty() || vehicle instanceof StorageMinecart || vehicle instanceof PoweredMinecart) break enterOnImpact;
                    if (!(event.getEntity() instanceof LivingEntity)) break enterOnImpact;
                    vehicle.setPassenger(event.getEntity());

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

                return;
            }

            boatRemoveEntities: {
                if (plugin.getConfiguration().boatRemoveEntities && vehicle instanceof Boat) {
                    if (!plugin.getConfiguration().boatRemoveEntitiesOtherBoats && entity instanceof Boat) break boatRemoveEntities;

                    if (entity instanceof LivingEntity) {
                        ((LivingEntity) entity).damage(5);
                        entity.setVelocity(vehicle.getVelocity().multiply(2));
                    } else entity.remove();

                    return;
                }
            }

            minecartRemoveEntities: {
                if (plugin.getConfiguration().minecartRemoveEntities && vehicle instanceof Minecart) {
                    if (!plugin.getConfiguration().minecartRemoveEntitiesOtherCarts && entity instanceof Minecart) break minecartRemoveEntities;

                    if(!(vehicle instanceof StorageMinecart) && !(vehicle instanceof PoweredMinecart) && vehicle.isEmpty())
                        break minecartRemoveEntities;

                    if (entity instanceof LivingEntity) {
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
                minecart.setDerailedVelocityMod(new Vector(plugin.getConfiguration().minecartOffRailSpeedModifier,
                        plugin.getConfiguration().minecartOffRailSpeedModifier,
                        plugin.getConfiguration().minecartOffRailSpeedModifier));
            minecart.setMaxSpeed(minecart.getMaxSpeed() * plugin.getConfiguration().minecartMaxSpeedModifier);
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

            if (plugin.getConfiguration().minecartRemoveOnExit) {
                vehicle.remove();
            } else if (plugin.getConfiguration().minecartDecayWhenEmpty) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Decay((Minecart) vehicle),
                        plugin.getConfiguration().minecartDecayTime);
            }
        }

        /**
         * Called when an vehicle moves.
         */
        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onVehicleMove(VehicleMoveEvent event) {
            // Ignore events not relating to minecarts.
            if (!(event.getVehicle() instanceof Minecart)) return;

            if (plugin.getConfiguration().minecartPoweredRailModifier > 0) {

                if (event.getTo().getBlock().getTypeId() == BlockID.POWERED_RAIL) {

                    event.getVehicle().setVelocity(event.getVehicle().getVelocity().multiply(plugin.getConfiguration().minecartPoweredRailModifier));
                }
            }

            if (plugin.getConfiguration().minecartStoragePlaceRails && event.getVehicle() instanceof StorageMinecart) {

                if(event.getTo().getBlock().getTypeId() == 0 && !BlockType.canPassThrough(event.getTo().getBlock().getRelative(0, -1, 0).getTypeId()) && ((StorageMinecart)event.getVehicle()).getInventory().contains(BlockID.MINECART_TRACKS)) {

                    ((StorageMinecart)event.getVehicle()).getInventory().remove(new ItemStack(BlockID.MINECART_TRACKS, 1));
                    event.getTo().getBlock().setTypeId(BlockID.MINECART_TRACKS);
                }
            }

            if (plugin.getConfiguration().minecartPressurePlateIntersection) {

                if (event.getTo().getBlock().getTypeId() == BlockID.STONE_PRESSURE_PLATE || event.getTo().getBlock().getTypeId() == BlockID.WOODEN_PRESSURE_PLATE) {

                    event.getVehicle().setVelocity(event.getVehicle().getVelocity().normalize().multiply(4));
                }

            }

            if (plugin.getConfiguration().minecartConstantSpeed > 0 && RailUtil.isTrack(event.getTo().getBlock()
                    .getTypeId())
                    && event.getVehicle().getVelocity().lengthSquared() > 0) {
                Vector vel = event.getVehicle().getVelocity();
                event.getVehicle().setVelocity(vel.normalize().multiply(plugin.getConfiguration()
                        .minecartConstantSpeed));
            }

            cartman.impact(event);
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

        public CraftBookVehicleBlockListener() {

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
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Decay((Minecart) ent),
                            plugin.getConfiguration().minecartDecayTime);
                }
            }
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onSignChange(SignChangeEvent event) {

            Block block = event.getBlock();
            String[] lines = event.getLines();
            LocalPlayer player = plugin.wrapPlayer(event.getPlayer());

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