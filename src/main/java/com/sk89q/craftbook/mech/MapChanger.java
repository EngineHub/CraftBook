package com.sk89q.craftbook.mech;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.player.PlayerInteractEvent;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.exceptions.InsufficientPermissionsException;
import com.sk89q.craftbook.util.exceptions.InvalidMechanismException;
import com.sk89q.craftbook.util.exceptions.ProcessedMechanismException;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.ItemID;
import com.sk89q.worldedit.bukkit.BukkitUtil;

public class MapChanger extends AbstractMechanic {

    public static class Factory extends AbstractMechanicFactory<MapChanger> {

        public Factory() {

        }

        /**
         * Explore around the trigger to find a functional chunk anchor sign; throw if things look funny.
         *
         * @param pt the trigger (should be a signpost)
         *
         * @return A chunk anchor if we could make a valid one
         *
         * @throws InvalidMechanismException if it failed to find the anchor, but it was similar to one
         */
        @Override
        public MapChanger detect(BlockWorldVector pt) throws InvalidMechanismException {

            Block block = BukkitUtil.toBlock(pt);

            if (block.getState() instanceof Sign) {
                Sign s = (Sign) block.getState();
                if (s.getLine(1).equalsIgnoreCase("[Map]")) return new MapChanger(block);
            }
            return null;
        }

        /**
         * Detect the mechanic at a placed sign.
         *
         * @throws ProcessedMechanismException
         */
        @Override
        public MapChanger detect(BlockWorldVector pt, LocalPlayer player,
                ChangedSign sign) throws InvalidMechanismException,
                ProcessedMechanismException {

            if (!sign.getLine(1).equalsIgnoreCase("[Map]")) return null;
            if (!player.hasPermission("craftbook.mech.map")) throw new InsufficientPermissionsException();

            player.print("mech.map.create");
            sign.setLine(1, "[Map]");

            throw new ProcessedMechanismException();
        }
    }

    /**
     * @param trigger if you didn't already check if this is a wall sign with appropriate text,
     *                you're going on Santa's naughty list.
     * @param plugin  the direction (UP or DOWN) in which we're looking for a destination
     *
     * @throws InvalidMechanismException
     */
    private MapChanger(Block trigger) throws InvalidMechanismException {

        super();
    }

    @Override
    public void onRightClick(PlayerInteractEvent event) {

        Block block = event.getClickedBlock();

        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if (!player.hasPermission("craftbook.mech.map.use")) {
            player.printError("mech.use-permission");
            return;
        }        if (event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getTypeId() == ItemID.MAP)
            if (block.getState() instanceof Sign) {
                Sign sign = (Sign) block.getState();
                byte id;
                try {
                    id = Byte.parseByte(sign.getLine(2));
                } catch (Exception e) {
                    id = -1;
                }
                if (id == -1) {
                    event.getPlayer().sendMessage("Invalid Map!");
                }
                event.getPlayer().getItemInHand().setDurability(id);
            }
    }
}