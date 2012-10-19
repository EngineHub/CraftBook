package com.sk89q.craftbook.cart;

import com.sk89q.craftbook.RedstoneUtil.Power;
import com.sk89q.craftbook.bukkit.VehiclesPlugin;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

public class CartMessager extends CartMechanism {

    VehiclesPlugin plugin;

    public CartMessager(VehiclesPlugin plugin) {

        this.plugin = plugin;
    }

    @Override
    public void impact(Minecart cart, CartMechanismBlocks blocks, boolean minor) {
        // validate
        if (cart == null || minor) return;

        // care?
        if (cart.getPassenger() == null) return;
        if (!(blocks.sign != null) && !(blocks.sign.getState() instanceof Sign)) return;

        if (!plugin.getLocalConfiguration().minecartTrackMessages) return;

        // enabled?
        if (Power.OFF == isActive(blocks.rail, blocks.base, blocks.sign)) return;

        // go
        if (cart.getPassenger() instanceof Player) {
            Player p = (Player) cart.getPassenger();
            Sign s = (Sign) blocks.sign.getState();
            if (!s.getLine(0).equalsIgnoreCase("[print]") && !s.getLine(1).equalsIgnoreCase("[print]")) return;
            if (s.getLine(1) != null && !s.getLine(1).trim().equalsIgnoreCase("") && !s.getLine(1).equalsIgnoreCase
                    ("[print]")) {
                p.sendMessage(s.getLine(1).trim());
            }
            if (s.getLine(2) != null && !s.getLine(2).trim().equalsIgnoreCase("")) {
                p.sendMessage(s.getLine(2).trim());
            }
            if (s.getLine(3) != null && !s.getLine(3).trim().equalsIgnoreCase("")) {
                p.sendMessage(s.getLine(3).trim());
            }
        }
    }

    @Override
    public void enter(Minecart cart, Entity entity, CartMechanismBlocks blocks,
                      boolean minor) {

    }
}