package com.sk89q.craftbook.vehicles.cart;

import static com.sk89q.craftbook.util.CartUtils.stop;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.CartBlockEnterEvent;
import com.sk89q.craftbook.CartBlockImpactEvent;
import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.craftbook.util.SignUtil;

public class CartStation extends CartBlockMechanism {

    public CartStation (ItemInfo material) {
        super(material);
    }

    @EventHandler
    public void onVehicleImpact(CartBlockImpactEvent event) {
        // validate
        if (!event.getBlocks().matches(getMaterial())) return;
        if (!event.getBlocks().matches("station")) return;

        // go
        switch (isActive(event.getBlocks())) {
            case ON:
                // standardize its speed and direction.
                launch(event.getMinecart(), event.getBlocks().sign);
                break;
            case OFF:
            case NA:
                // park it.
                stop(event.getMinecart());
                // recenter it
                Location l = event.getBlocks().rail.getLocation().add(0.5, 0.5, 0.5);
                if (!event.getMinecart().getLocation().equals(l)) {
                    event.getMinecart().teleport(l);
                }
                // recentering and parking almost completely prevents more than one cart from getting onto the same
                // station.
                break;
        }
    }

    private void launch(Minecart cart, Block director) {

        cart.setVelocity(propel(SignUtil.getFacing(director)));
    }

    /**
     * WorldEdit's Vector type collides with Bukkit's Vector type here. It's not pleasant.
     */
    public static Vector propel(BlockFace face) {

        return new Vector(face.getModX() * 0.2, face.getModY() * 0.2, face.getModZ() * 0.2);
    }

    @EventHandler
    public void onVehicleEnter(CartBlockEnterEvent event) {

        // validate
        if (!event.getBlocks().matches(getMaterial())) return;
        if (!event.getBlocks().matches("station")) return;

        if (!event.getBlocks().getSign().getLine(2).equalsIgnoreCase("AUTOSTART")) return;

        // go
        switch (isActive(event.getBlocks())) {
            case ON:
                // standardize its speed and direction.
                launch(event.getMinecart(), event.getBlocks().sign);
                break;
            case OFF:
            case NA:
                // park it.
                stop(event.getMinecart());
                // recenter it
                Location l = event.getBlocks().rail.getLocation().add(0.5, 0.5, 0.5);
                if (!event.getMinecart().getLocation().equals(l)) {
                    event.getMinecart().teleport(l);
                }
                // recentering and parking almost completely prevents more than one cart from getting onto the same
                // station.
                break;
        }
    }

    @Override
    public String getName() {

        return "Station";
    }

    @Override
    public String[] getApplicableSigns() {

        return new String[] {"station"};
    }
}