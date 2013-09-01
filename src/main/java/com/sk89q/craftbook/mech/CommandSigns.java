package com.sk89q.craftbook.mech;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.SourcedBlockRedstoneEvent;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.exceptions.InsufficientPermissionsException;
import com.sk89q.craftbook.util.exceptions.InvalidMechanismException;
import com.sk89q.craftbook.util.exceptions.ProcessedMechanismException;
import com.sk89q.worldedit.BlockWorldVector;

public class CommandSigns extends AbstractMechanic {

    private CraftBookPlugin plugin = CraftBookPlugin.inst();

    public static class Factory extends AbstractMechanicFactory<CommandSigns> {

        /**
         * Explore around the trigger to find a functional command sign; throw if things look funny.
         *
         * @param pt the trigger (should be a signpost)
         *
         * @return an Elevator if we could make a valid one, or null if this looked nothing like an elevator.
         *
         * @throws InvalidMechanismException if the area looked like it was intended to be an elevator, but it failed.
         */
        @Override
        public CommandSigns detect(BlockWorldVector pt) throws InvalidMechanismException {

            Block block = BukkitUtil.toBlock(pt);

            if (SignUtil.isSign(block)) {
                ChangedSign s = BukkitUtil.toChangedSign(block);
                if (s.getLine(1).equalsIgnoreCase("[Command]")) return new CommandSigns(block);
            }
            return null;
        }

        /**
         * Detect the mechanic at a placed sign.
         *
         * @throws ProcessedMechanismException
         */
        @Override
        public CommandSigns detect(BlockWorldVector pt, LocalPlayer player, ChangedSign sign) throws InvalidMechanismException, ProcessedMechanismException {

            if (!sign.getLine(1).equalsIgnoreCase("[Command]")) return null;
            if (!player.hasPermission("craftbook.mech.command")) throw new InsufficientPermissionsException();

            player.print("mech.command.create");
            sign.setLine(1, "[Command]");

            throw new ProcessedMechanismException();
        }
    }

    /**
     * @param trigger if you didn't already check if this is a wall sign with appropriate text,
     *                you're going on Santa's naughty list.
     *
     * @throws InvalidMechanismException
     */
    private CommandSigns(Block trigger) throws InvalidMechanismException {

        super();
        this.trigger = trigger;
    }

    private final Block trigger;

    @Override
    public void onRightClick(PlayerInteractEvent event) {

        if (!plugin.getConfiguration().commandSignEnabled) return;
        if (!BukkitUtil.toWorldVector(event.getClickedBlock()).equals(BukkitUtil.toWorldVector(trigger)))
            return; // wth? our manager is insane

        LocalPlayer localPlayer = plugin.wrapPlayer(event.getPlayer());

        if (!localPlayer.hasPermission("craftbook.mech.command.use")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                localPlayer.printError("mech.use-permission");
            return;
        }

        ChangedSign s = BukkitUtil.toChangedSign(event.getClickedBlock());

        String command = s.getLine(2).replace("/", "") + s.getLine(3);
        command = command.replace("@p", event.getPlayer().getName());

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

        event.setCancelled(true);
    }

    @Override
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {

        if (event.getNewCurrent() < event.getOldCurrent())
            return;
        if (!plugin.getConfiguration().commandSignEnabled) return;
        if (!BukkitUtil.toWorldVector(event.getBlock()).equals(BukkitUtil.toWorldVector(trigger)))
            return; // wth? our manager is insane

        ChangedSign s = BukkitUtil.toChangedSign(event.getBlock());

        String command = s.getLine(2).replace("/", "") + s.getLine(3);
        if (command.contains("@p")) return; // We don't work with player commands.

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }
}