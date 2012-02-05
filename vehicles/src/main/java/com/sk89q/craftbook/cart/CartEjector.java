package com.sk89q.craftbook.cart;

import com.sk89q.craftbook.RedstoneUtil.Power;
import com.sk89q.craftbook.util.SignUtil;

import com.sk89q.worldedit.bukkit.BukkitUtil;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;

public class CartEjector extends CartMechanism {
    public void impact(Minecart cart, CartMechanismBlocks blocks, boolean minor) {
        // validate
        if (cart == null) return;
        
        // care?
        if (cart.getPassenger() == null) return;
        
        // enabled?
        if (Power.OFF == isActive(blocks.rail, blocks.base, blocks.sign)) return;
        
        // go
        Block ejectTarget;
        if (blocks.sign == null) {
            ejectTarget = blocks.rail;
        } else {
            if (!blocks.matches("eject")) {
                ejectTarget = blocks.rail;
            } else {
                ejectTarget = blocks.rail.getRelative(SignUtil.getFront(blocks.sign));
            }
        }
        // if you use just
        //     cart.getPassenger().teleport(ejectTarget.getLocation());
        //   the client tweaks as bukkit tries to teleport you, then changes its mind and leaves you in the cart.
        //   the cart also comes to a dead halt at the time of writing, and i have no idea why.
        Entity ent = cart.getPassenger();
        cart.eject();
        ent.teleport(BukkitUtil.center(ejectTarget.getLocation()));
        
        // notice!
        //  if a client tries to board a cart immediately before it crosses an ejector,
        //  it may appear to them that they crossed the ejector and it failed to activate.
        //  what's actually happening is that the server didn't see them enter the cart
        //  until -after- it had triggered the ejector... it's just client anticipating.
    }
}
