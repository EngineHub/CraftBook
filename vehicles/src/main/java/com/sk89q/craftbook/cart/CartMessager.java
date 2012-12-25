package com.sk89q.craftbook.cart;

import java.util.ArrayList;

import org.bukkit.block.Sign;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.RedstoneUtil.Power;
import com.sk89q.craftbook.bukkit.VehiclesPlugin;

public class CartMessager extends CartMechanism {

    VehiclesPlugin plugin;

    public CartMessager (VehiclesPlugin plugin) {

        this.plugin = plugin;
    }

    @Override
    public void impact (Minecart cart, CartMechanismBlocks blocks, boolean minor) {
        // validate
        if (cart == null || minor) return;

        // care?
        if (cart.getPassenger() == null) return;
        if (blocks.sign == null || !(blocks.sign.getState() instanceof Sign)) return;

        if (!plugin.getLocalConfiguration().minecartTrackMessages) return;

        // enabled?
        if (Power.OFF == isActive(blocks.rail, blocks.base, blocks.sign)) return;

        // go
        if (cart.getPassenger() instanceof Player) {
            Player p = (Player) cart.getPassenger();
            Sign s = (Sign) blocks.sign.getState();
            if (!s.getLine(0).equalsIgnoreCase("[print]") && !s.getLine(1).equalsIgnoreCase("[print]")) return;

            ArrayList<String> messages = new ArrayList<String>();

            boolean stack = false;

            if (s.getLine(1) != null && !s.getLine(1).isEmpty() && !s.getLine(1).equalsIgnoreCase("[print]")) {
                messages.add(s.getLine(1));
                stack = s.getLine(1).endsWith("+") || s.getLine(1).endsWith(" ");
            }
            if (s.getLine(2) != null && !s.getLine(2).isEmpty()) {
                if (stack) {
                    messages.set(messages.size() - 1, messages.get(messages.size() - 1) + s.getLine(2));
                    stack = s.getLine(2).endsWith("+") || s.getLine(2).endsWith(" ");
                } else {
                    messages.add(s.getLine(2));
                    stack = s.getLine(2).endsWith("+") || s.getLine(2).endsWith(" ");
                }
            }
            if (s.getLine(3) != null && !s.getLine(3).isEmpty()) {
                if (stack) {
                    messages.set(messages.size() - 1, messages.get(messages.size() - 1) + s.getLine(3));
                } else {
                    messages.add(s.getLine(3));
                }
            }

            for (String mes : messages) {
                if (stack) mes = mes.replace("+", "");
                p.sendMessage(mes);
            }
        }
    }

    @Override
    public String getName () {
        return "Messager";
    }

    @Override
    public String[] getApplicableSigns () {
        return new String[] { "Print" };
    }
}