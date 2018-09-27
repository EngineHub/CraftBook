package com.sk89q.craftbook.mechanics.minecart.blocks;
import static com.sk89q.craftbook.util.CartUtil.reverse;

import com.sk89q.craftbook.mechanics.minecart.events.CartBlockImpactEvent;
import com.sk89q.craftbook.util.BlockSyntax;
import com.sk89q.craftbook.util.RedstoneUtil.Power;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

public class CartReverser extends CartBlockMechanism {

    @EventHandler
    public void onVehicleImpact(CartBlockImpactEvent event) {

        // validate
        if (!event.getBlocks().matches(getMaterial())) return;
        if (event.isMinor()) return;

        // enabled?
        if (Power.OFF == isActive(event.getBlocks())) return;

        if (!event.getBlocks().hasSign() || !event.getBlocks().matches("reverse")) {
            reverse(event.getMinecart());
            return;
        }

        BlockFace dir = SignUtil.getFacing(event.getBlocks().sign);

        Vector normalVelocity = event.getMinecart().getVelocity().normalize();

        switch (dir) {
            case NORTH:
                if (normalVelocity.getBlockZ() != -1) {
                    reverse(event.getMinecart());
                }
                break;
            case SOUTH:
                if (normalVelocity.getBlockZ() != 1) {
                    reverse(event.getMinecart());
                }
                break;
            case EAST:
                if (normalVelocity.getBlockX() != 1) {
                    reverse(event.getMinecart());
                }
                break;
            case WEST:
                if (normalVelocity.getBlockX() != -1) {
                    reverse(event.getMinecart());
                }
                break;
            default:
                reverse(event.getMinecart());
        }
    }

    @Override
    public String getName() {

        return "Reverser";
    }

    @Override
    public String[] getApplicableSigns() {

        return new String[] {"reverse"};
    }

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "block", "Sets the block that is the base of the reverse mechanic.");
        material = BlockSyntax.getBlock(config.getString(path + "block", BlockTypes.WHITE_WOOL.getId()), true);
    }
}