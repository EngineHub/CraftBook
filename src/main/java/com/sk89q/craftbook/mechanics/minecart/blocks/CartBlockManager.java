package com.sk89q.craftbook.mechanics.minecart.blocks;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.exceptions.InsufficientPermissionsException;

public class CartBlockManager extends AbstractCraftBookMechanic {

    private static CartBlockManager INSTANCE;
    private Set<CartBlockMechanism> cartBlockMechanisms;

    public CartBlockManager() {
        INSTANCE = this;
        cartBlockMechanisms = new HashSet<CartBlockMechanism>();
    }

    @Override
    public void disable() {
        cartBlockMechanisms = null;
        INSTANCE = null;
    }

    public static CartBlockManager inst() {
        return INSTANCE;
    }

    public void addMechanic(CartBlockMechanism mech) {
        if(mech.getMaterial().getId() > 0)
            cartBlockMechanisms.add(mech);
    }

    public Set<CartBlockMechanism> getMechanics() {
        return cartBlockMechanisms;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        Block block = event.getBlock();
        String[] lines = event.getLines();
        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

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
