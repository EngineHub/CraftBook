package com.sk89q.craftbook.mechanics.minecart.blocks;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.minecart.events.CartBlockImpactEvent;
import com.sk89q.craftbook.util.BlockSyntax;
import com.sk89q.craftbook.util.RedstoneUtil.Power;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.ArrayList;

public class CartMessenger extends CartBlockMechanism {

    @EventHandler
    public void onVehicleImpact(CartBlockImpactEvent event) {

        // validate
        if (event.isMinor()) return;
        if (!event.getBlocks().matches(getMaterial())) return;

        // care?
        if (event.getMinecart().getPassenger() == null) return;
        if (!event.getBlocks().hasSign()) return;

        // enabled?
        if (Power.OFF == isActive(event.getBlocks())) return;

        // go
        if (event.getMinecart().getPassenger() instanceof Player) {
            Player p = (Player) event.getMinecart().getPassenger();
            ChangedSign s = event.getBlocks().getSign();
            if (!s.getLine(0).equalsIgnoreCase("[print]") && !s.getLine(1).equalsIgnoreCase("[print]")) return;

            ArrayList<String> messages = new ArrayList<>();

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
                if (stack) mes = StringUtils.replace(mes, "+", "");
                p.sendMessage(mes);
            }
        }
    }

    @Override
    public String getName() {

        return "Messager";
    }

    @Override
    public String[] getApplicableSigns() {

        return new String[] {"Print"};
    }

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "block", "Sets the block that is the base of the messager mechanic.");
        material = BlockSyntax.getBlock(config.getString(path + "block", BlockTypes.END_STONE.getId()), true);
    }
}