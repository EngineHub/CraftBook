package com.sk89q.craftbook.mechanics.minecart.blocks;

import static com.sk89q.craftbook.util.CartUtil.stop;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.mechanics.minecart.events.CartBlockEnterEvent;
import com.sk89q.craftbook.mechanics.minecart.events.CartBlockImpactEvent;
import com.sk89q.craftbook.mechanics.minecart.events.CartBlockRedstoneEvent;
import com.sk89q.craftbook.util.BlockSyntax;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class CartStation extends CartBlockMechanism {

    @Override
    public boolean enable() {

        CraftBookPlugin.inst().registerCommands(StationCommands.class);
        return true;
    }

    @EventHandler
    public void onVehicleImpact(CartBlockImpactEvent event) {

        stationInteraction(event.getMinecart(), event.getBlocks());
    }

    @EventHandler
    public void onBlockPower(CartBlockRedstoneEvent event) {

        stationInteraction(event.getMinecart(), event.getBlocks());
    }

    public void stationInteraction(Minecart cart, CartMechanismBlocks blocks) {

        // validate
        if (!blocks.matches(getMaterial())) return;
        if (!blocks.matches("station")) return;

        if(cart == null)
            return;

        // go
        switch (isActive(blocks)) {
            case ON:
                // standardize its speed and direction.
                launch(cart, blocks.sign);
                break;
            case OFF:
            case NA:
                // park it.
                stop(cart);
                // recenter it
                Location l = blocks.rail.getLocation().add(0.5, 0.5, 0.5);
                if (!cart.getLocation().equals(l)) {
                    cart.teleport(l);
                }
                // recentering and parking almost completely prevents more than one cart from getting onto the same
                // station.
                break;
        }
    }

    private static void launch(Minecart cart, Block director) {
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

        if(!event.getBlocks().getSign().getLine(3).isEmpty() && event.getEntered() instanceof Player) {

            ItemStack testItem = ItemSyntax.getItem(event.getBlocks().getSign().getLine(3));
            if(!ItemUtil.areItemsIdentical(testItem, ((Player) event.getEntered()).getItemInHand()))
                return;
        }

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

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "block", "Sets the block that is the base of the station mechanic.");
        material = BlockSyntax.getBlock(config.getString(path + "block", BlockTypes.OBSIDIAN.getId()), true);
    }
}