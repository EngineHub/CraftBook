package com.sk89q.craftbook.mech;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Lever;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.InsufficientPermissionsException;
import com.sk89q.craftbook.InvalidMechanismException;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.ProcessedMechanismException;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.bukkit.BukkitUtil;

public class Payment extends AbstractMechanic{

    MechanismsPlugin plugin;

    protected BlockWorldVector pt;


    public Payment(BlockWorldVector pt, MechanismsPlugin plugin) {
        this.pt = pt;
        this.plugin = plugin;
    }

    @Override
    public void unload() {
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {

    }


    /**
     * Raised when a block is right clicked.
     * 
     * @param event
     */
    @Override
    public void onRightClick(PlayerInteractEvent event) {
        LocalPlayer player = plugin.wrap(event.getPlayer());

        if (!player.hasPermission("craftbook.mech.pay.use")) {
            player.printError("mech.use-permission");
            return;
        }

        Block block = BukkitUtil.toWorld(pt).getBlockAt(
                BukkitUtil.toLocation(pt));

        Sign sign = null;

        if (block.getTypeId() == BlockID.WALL_SIGN) {
            BlockState state = block.getState();
            if (state instanceof Sign)
                sign = (Sign) state;
        }

        if(sign==null) return;

        double money = Double.parseDouble(sign.getLine(2));
        String reciever = sign.getLine(3);

        if(MechanismsPlugin.economy.withdrawPlayer(event.getPlayer().getName(), money).transactionSuccess()) {
            if(MechanismsPlugin.economy.depositPlayer(reciever, money).transactionSuccess()) {
                Block back = SignUtil.getBackBlock(sign.getBlock());
                BlockFace bface = sign.getBlock().getFace(back);
                Block redstoneItem = back.getRelative(bface);
                if(setState(sign.getBlock(), true))
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new turnOff(redstoneItem), 20L);
            }
            else
                MechanismsPlugin.economy.depositPlayer(event.getPlayer().getName(), money);
        }

        event.setCancelled(true);
    }

    private class turnOff implements Runnable {

        Block block;

        public turnOff(Block block) {
            this.block = block;
        }

        @Override
        public void run() {
            setState(block,false);
        }

    }

    public static boolean setState(Block block, boolean state) {
        if (block.getType() != Material.LEVER) return false;
        byte data = block.getData();
        int newData;

        Block sourceBlock = block.getRelative(((Lever) block.getState().getData()).getAttachedFace());

        if (!state)
            newData = data & 0x7;
        else
            newData = data | 0x8;

        if (newData != data) {
            block.setData((byte)newData, true);
            int oldS = state ? (0) : (1);
            int newS = state ? (1) : (0);
            BlockRedstoneEvent event = new BlockRedstoneEvent(sourceBlock, oldS, newS);
            Bukkit.getPluginManager().callEvent(event);
            return true;
        }
        return false;
    }

    public static class Factory extends AbstractMechanicFactory<Payment> {

        protected MechanismsPlugin plugin;

        public Factory(MechanismsPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public Payment detect(BlockWorldVector pt) {
            Block block = BukkitUtil.toWorld(pt).getBlockAt(
                    BukkitUtil.toLocation(pt));
            if (block.getTypeId() == BlockID.WALL_SIGN) {
                BlockState state = block.getState();
                if (state instanceof Sign) {
                    Sign sign = (Sign) state;
                    if (sign.getLine(1).equalsIgnoreCase("[Pay]")) {
                        return new Payment(pt, plugin);
                    }
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
        public Payment detect(BlockWorldVector pt, LocalPlayer player, Sign sign)
                throws InvalidMechanismException, ProcessedMechanismException {
            if (sign.getLine(1).equalsIgnoreCase("[Pay]")) {
                if (!player.hasPermission("craftbook.mech.pay")) {
                    throw new InsufficientPermissionsException();
                }

                sign.setLine(1, "[Pay]");
                player.print("mech.pay.create");
            } else {
                return null;
            }

            throw new ProcessedMechanismException();
        }

    }
}
