package com.sk89q.craftbook.bukkit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import com.sk89q.craftbook.LocalComponent;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.commands.VehicleCommands;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.exceptions.InsufficientPermissionsException;
import com.sk89q.craftbook.vehicles.boat.BoatDrops;
import com.sk89q.craftbook.vehicles.boat.BoatExitRemover;
import com.sk89q.craftbook.vehicles.boat.BoatRemoveEntities;
import com.sk89q.craftbook.vehicles.boat.BoatSpeedModifiers;
import com.sk89q.craftbook.vehicles.boat.BoatUncrashable;
import com.sk89q.craftbook.vehicles.boat.LandBoats;
import com.sk89q.craftbook.vehicles.cart.CartBlockMechanism;
import com.sk89q.craftbook.vehicles.cart.CartBooster;
import com.sk89q.craftbook.vehicles.cart.CartDeposit;
import com.sk89q.craftbook.vehicles.cart.CartDispenser;
import com.sk89q.craftbook.vehicles.cart.CartEjector;
import com.sk89q.craftbook.vehicles.cart.CartExitRemover;
import com.sk89q.craftbook.vehicles.cart.CartLift;
import com.sk89q.craftbook.vehicles.cart.CartMaxSpeed;
import com.sk89q.craftbook.vehicles.cart.CartMessenger;
import com.sk89q.craftbook.vehicles.cart.CartRemoveEntities;
import com.sk89q.craftbook.vehicles.cart.CartReverser;
import com.sk89q.craftbook.vehicles.cart.CartSorter;
import com.sk89q.craftbook.vehicles.cart.CartSpeedModifiers;
import com.sk89q.craftbook.vehicles.cart.CartStation;
import com.sk89q.craftbook.vehicles.cart.CartTeleporter;
import com.sk89q.craftbook.vehicles.cart.CollisionEntry;
import com.sk89q.craftbook.vehicles.cart.ConstantSpeed;
import com.sk89q.craftbook.vehicles.cart.EmptyDecay;
import com.sk89q.craftbook.vehicles.cart.EmptySlowdown;
import com.sk89q.craftbook.vehicles.cart.FallModifier;
import com.sk89q.craftbook.vehicles.cart.ItemPickup;
import com.sk89q.craftbook.vehicles.cart.MobBlocker;
import com.sk89q.craftbook.vehicles.cart.MoreRails;
import com.sk89q.craftbook.vehicles.cart.NoCollide;
import com.sk89q.craftbook.vehicles.cart.RailPlacer;
import com.sk89q.craftbook.vehicles.cart.VisionSteering;

/**
 * Author: Turtle9598
 */
public class VehicleCore implements LocalComponent, Listener {

    private static VehicleCore instance;

    private CraftBookPlugin plugin = CraftBookPlugin.inst();

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

        // Register events
        registerEvents();
    }

    @Override
    public void disable() {

        stationSelection = null;
        instance = null;
    }

    private Set<CartBlockMechanism> cartBlockMechanisms = new HashSet<CartBlockMechanism>();

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
        if(plugin.getConfiguration().minecartMaxSpeedEnabled && plugin.getConfiguration().minecartMaxSpeedBlock.getId() > 0)
            cartBlockMechanisms.add(new CartMaxSpeed(plugin.getConfiguration().minecartMaxSpeedBlock));

        for(CartBlockMechanism mech : cartBlockMechanisms)
            plugin.getServer().getPluginManager().registerEvents(mech, plugin);

        if(plugin.getConfiguration().minecartMoreRailsEnabled)
            plugin.getServer().getPluginManager().registerEvents(new MoreRails(), plugin);
        if(plugin.getConfiguration().minecartRemoveEntitiesEnabled)
            plugin.getServer().getPluginManager().registerEvents(new CartRemoveEntities(), plugin);
        if(plugin.getConfiguration().minecartVisionSteeringEnabled)
            plugin.getServer().getPluginManager().registerEvents(new VisionSteering(), plugin);
        if(plugin.getConfiguration().minecartDecayEnabled)
            plugin.getServer().getPluginManager().registerEvents(new EmptyDecay(), plugin);
        if(plugin.getConfiguration().minecartBlockMobEntryEnabled)
            plugin.getServer().getPluginManager().registerEvents(new MobBlocker(), plugin);
        if(plugin.getConfiguration().minecartRemoveOnExitEnabled)
            plugin.getServer().getPluginManager().registerEvents(new CartExitRemover(), plugin);
        if(plugin.getConfiguration().minecartCollisionEntryEnabled)
            plugin.getServer().getPluginManager().registerEvents(new CollisionEntry(), plugin);
        if(plugin.getConfiguration().minecartItemPickupEnabled)
            plugin.getServer().getPluginManager().registerEvents(new ItemPickup(), plugin);
        if(plugin.getConfiguration().minecartFallModifierEnabled)
            plugin.getServer().getPluginManager().registerEvents(new FallModifier(), plugin);
        if(plugin.getConfiguration().minecartConstantSpeedEnable)
            plugin.getServer().getPluginManager().registerEvents(new ConstantSpeed(), plugin);
        if(plugin.getConfiguration().minecartRailPlacerEnable)
            plugin.getServer().getPluginManager().registerEvents(new RailPlacer(), plugin);
        if(plugin.getConfiguration().minecartSpeedModifierEnable)
            plugin.getServer().getPluginManager().registerEvents(new CartSpeedModifiers(), plugin);
        if(plugin.getConfiguration().minecartEmptySlowdownStopperEnable)
            plugin.getServer().getPluginManager().registerEvents(new EmptySlowdown(), plugin);
        if(plugin.getConfiguration().minecartNoCollideEnable)
            plugin.getServer().getPluginManager().registerEvents(new NoCollide(), plugin);

        if(plugin.getConfiguration().boatRemoveEntitiesEnabled)
            plugin.getServer().getPluginManager().registerEvents(new BoatRemoveEntities(), plugin);
        if(plugin.getConfiguration().boatNoCrash)
            plugin.getServer().getPluginManager().registerEvents(new BoatUncrashable(), plugin);
        if(plugin.getConfiguration().boatBreakReturn)
            plugin.getServer().getPluginManager().registerEvents(new BoatDrops(), plugin);
        if(plugin.getConfiguration().boatSpeedModifierEnable)
            plugin.getServer().getPluginManager().registerEvents(new BoatSpeedModifiers(), plugin);
        if(plugin.getConfiguration().boatLandBoatsEnable)
            plugin.getServer().getPluginManager().registerEvents(new LandBoats(), plugin);
        if(plugin.getConfiguration().boatRemoveOnExitEnabled)
            plugin.getServer().getPluginManager().registerEvents(new BoatExitRemover(), plugin);

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public String getStation(String playerName) {

        return stationSelection.get(playerName);
    }

    public void setStation(String playerName, String stationName) {

        stationSelection.put(playerName, stationName);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onSignChange(SignChangeEvent event) {

        Block block = event.getBlock();
        String[] lines = event.getLines();
        LocalPlayer player = plugin.wrapPlayer(event.getPlayer());

        try {
            for (CartBlockMechanism mech : cartBlockMechanisms) {
                if (mech.getApplicableSigns() == null || mech.getApplicableSigns().length == 0) continue;
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