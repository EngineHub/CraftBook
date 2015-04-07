package com.sk89q.craftbook.mechanics.minecart;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.RailUtil;
import com.sk89q.util.yaml.YAMLProcessor;

public class TemporaryCart extends AbstractCraftBookMechanic {

    private Set<RideableMinecart> minecarts = new HashSet<RideableMinecart>();

    public Set<RideableMinecart> getMinecarts() {

        return minecarts;
    }

    @Override
    public void disable() {

        for(RideableMinecart minecart : minecarts) {
            minecart.remove();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if(!RailUtil.isTrack(event.getClickedBlock().getType()))
            return;

        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if(player.isHoldingBlock() || player.isInsideVehicle() || player.isSneaking()) return;
        if(player.getHeldItemInfo().getType().name().contains("MINECART")) return;

        if(!EventUtil.passesFilter(event))
            return;

        if(!player.hasPermission("craftbook.vehicles.temporary-cart.use")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.create-permission");
            return;
        }

        RideableMinecart cart = event.getClickedBlock().getWorld().spawn(BlockUtil.getBlockCentre(event.getClickedBlock()), RideableMinecart.class);
        minecarts.add(cart);
        cart.setPassenger(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDismount(final VehicleExitEvent event) {

        if(!(event.getVehicle() instanceof RideableMinecart)) return;

        if(!EventUtil.passesFilter(event))
            return;

        if(!minecarts.contains(event.getVehicle())) return;

        Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new Runnable() {
            @Override
            public void run () {
                event.getVehicle().remove();
            }
        }, 2L);
    }

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

    }
}