package com.sk89q.craftbook.mech;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.player.PlayerInteractEvent;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.exceptions.InsufficientPermissionsException;
import com.sk89q.craftbook.util.exceptions.InvalidMechanismException;
import com.sk89q.craftbook.util.exceptions.ProcessedMechanismException;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;

/**
 * Payment Mech, takes payment. (Requires Vault.)
 *
 * @author Me4502
 */
public class Payment extends AbstractMechanic {

    /**
     * Raised when a block is right clicked.
     *
     * @param event
     */
    @Override
    public void onRightClick(PlayerInteractEvent event) {

        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (!player.hasPermission("craftbook.mech.pay.use")) {
            player.printError("mech.use-permission");
            return;
        }

        ChangedSign sign = BukkitUtil.toChangedSign(event.getClickedBlock());
        if (sign == null) return;

        double money = Double.parseDouble(sign.getLine(2));
        String reciever = sign.getLine(3);

        if (CraftBookPlugin.inst().getEconomy().withdrawPlayer(event.getPlayer().getName(), money).transactionSuccess())
            if (CraftBookPlugin.inst().getEconomy().depositPlayer(reciever, money).transactionSuccess()) {
                Block back = SignUtil.getBackBlock(event.getClickedBlock());
                BlockFace bface = SignUtil.getBack(event.getClickedBlock());
                Block redstoneItem = back.getRelative(bface);
                if (ICUtil.setState(redstoneItem, true, back))
                    CraftBookPlugin.inst().getServer().getScheduler().runTaskLater(CraftBookPlugin.inst(), new TurnOff(redstoneItem, back), 20L);
            } else
                CraftBookPlugin.inst().getEconomy().depositPlayer(event.getPlayer().getName(), money);

        event.setCancelled(true);
    }

    private static class TurnOff implements Runnable {

        final Block block;
        final Block source;

        public TurnOff(Block block, Block source) {

            this.block = block;
            this.source = source;
        }

        @Override
        public void run() {

            ICUtil.setState(block, false, source);
        }
    }

    public static class Factory extends AbstractMechanicFactory<Payment> {

        @Override
        public Payment detect(BlockWorldVector pt) {

            Block block = BukkitUtil.toWorld(pt).getBlockAt(BukkitUtil.toLocation(pt));
            if (block.getTypeId() == BlockID.WALL_SIGN) {
                BlockState state = block.getState();
                if (state instanceof Sign) {
                    Sign sign = (Sign) state;
                    if (sign.getLine(1).equalsIgnoreCase("[Pay]")) return new Payment();
                }
            }

            return null;
        }

        /**
         * Detect the mechanic at a placed sign.
         *
         * @throws ProcessedMechanismException
         */
        @Override
        public Payment detect(BlockWorldVector pt, LocalPlayer player, ChangedSign sign) throws InvalidMechanismException, ProcessedMechanismException {

            if (sign.getLine(1).equalsIgnoreCase("[Pay]")) {
                if (!player.hasPermission("craftbook.mech.pay")) throw new InsufficientPermissionsException();

                sign.setLine(1, "[Pay]");
                player.print("mech.pay.create");
            } else return null;

            throw new ProcessedMechanismException();
        }
    }
}