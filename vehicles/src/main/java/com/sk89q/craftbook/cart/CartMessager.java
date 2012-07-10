package com.sk89q.craftbook.cart;

import java.lang.reflect.Field;

import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.RedstoneUtil.Power;
import com.sk89q.craftbook.VehiclesConfiguration;
import com.sk89q.craftbook.bukkit.VehiclesPlugin;

public class CartMessager extends CartMechanism{

    @Override
    public void impact(Minecart cart, CartMechanismBlocks blocks, boolean minor) {
        try
        {
            // validate
            if (cart == null) return;

            Field f = VehiclesPlugin.class.getDeclaredField("config");
            f.setAccessible(true);
            VehiclesConfiguration cfg = (VehiclesConfiguration) f.get(null);

            // care?
            if (cart.getPassenger() == null || !cfg.minecartTrackMessages) return;

            // enabled?
            if (Power.OFF == isActive(blocks.rail, blocks.base, blocks.sign)) return;

            // go
            if (blocks.sign == null) {

            } else {
                if (!blocks.matches("print")) {

                } else {
                    if(cart.getPassenger() instanceof Player) {
                        Player p = (Player) cart.getPassenger();
                        Sign s = (Sign) blocks.sign.getState();
                        if(s.getLine(1)!=null && !s.getLine(1).trim().equalsIgnoreCase(""))
                            p.chat(s.getLine(1));
                        if(s.getLine(2)!=null && !s.getLine(2).trim().equalsIgnoreCase(""))
                            p.chat(s.getLine(2));
                        if(s.getLine(3)!=null && !s.getLine(3).trim().equalsIgnoreCase(""))
                            p.chat(s.getLine(3));
                    }
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void enter(Minecart cart, Entity entity, CartMechanismBlocks blocks,
            boolean minor) {

    }

}
