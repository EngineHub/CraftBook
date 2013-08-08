package com.sk89q.craftbook.mech;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.player.PlayerInteractEvent;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.exceptions.InsufficientPermissionsException;
import com.sk89q.craftbook.util.exceptions.InvalidMechanismException;
import com.sk89q.craftbook.util.exceptions.ProcessedMechanismException;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.ItemID;

public class MapChanger extends AbstractMechanic {

    public static class Factory extends AbstractMechanicFactory<MapChanger> {

        /**
         * Explore around the trigger to find a functional chunk anchor sign; throw if things look funny.
         *
         * @param pt the trigger (should be a signpost)
         *
         * @return A map switcher if we could make a valid one
         *
         * @throws InvalidMechanismException if it failed to find the map switcher, but it was similar to one
         */
        @Override
        public MapChanger detect(BlockWorldVector pt) throws InvalidMechanismException {

            Block block = BukkitUtil.toBlock(pt);

            if (SignUtil.isSign(block)) {
                ChangedSign s = BukkitUtil.toChangedSign(block);
                if (s.getLine(1).equalsIgnoreCase("[Map]")) return new MapChanger();
            }
            return null;
        }

        /**
         * Detect the mechanic at a placed sign.
         *
         * @throws ProcessedMechanismException
         */
        @Override
        public MapChanger detect(BlockWorldVector pt, LocalPlayer player, ChangedSign sign) throws InvalidMechanismException, ProcessedMechanismException {

            if (!sign.getLine(1).equalsIgnoreCase("[Map]")) return null;
            if (!player.hasPermission("craftbook.mech.map")) throw new InsufficientPermissionsException();

            player.print("mech.map.create");
            sign.setLine(1, "[Map]");
            sign.update(false);

            throw new ProcessedMechanismException();
        }
    }

    @Override
    public void onRightClick(PlayerInteractEvent event) {

        Block block = event.getClickedBlock();

        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if (!player.hasPermission("craftbook.mech.map.use")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.use-permission");
            return;
        }
        if (event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getTypeId() == ItemID.MAP) {
            if (block.getState() instanceof Sign) {
                Sign sign = (Sign) block.getState();
                byte id;
                try {
                    id = Byte.parseByte(sign.getLine(2));
                } catch (Exception e) {
                    id = -1;
                }
                if (id <= -1)
                    event.getPlayer().sendMessage("Invalid Map!");
                event.getPlayer().getItemInHand().setDurability(id);
            }
        }
    }
}