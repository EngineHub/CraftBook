package com.sk89q.craftbook.mech;

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class Command extends AbstractMechanic {

    public static class Factory extends AbstractMechanicFactory<Command> {

        public Factory(MechanismsPlugin plugin) {

            this.plugin = plugin;
        }

        private final MechanismsPlugin plugin;

        /**
         * Explore around the trigger to find a functional command sign; throw if
         * things look funny.
         *
         * @param pt the trigger (should be a signpost)
         *
         * @return an Elevator if we could make a valid one, or null if this
         *         looked nothing like an elevator.
         *
         * @throws InvalidMechanismException if the area looked like it was intended to be an
         *                                   elevator, but it failed.
         */
        @Override
        public Command detect(BlockWorldVector pt) throws InvalidMechanismException {

            Block block = BukkitUtil.toBlock(pt);

            if (block.getState() instanceof Sign) {
                Sign s = (Sign) block.getState();
                if (s.getLine(1).equalsIgnoreCase("[Command]")) {
                    return new Command(block, plugin);
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
        public Command detect(BlockWorldVector pt, LocalPlayer player, Sign sign)
                throws InvalidMechanismException, ProcessedMechanismException {

            if (!sign.getLine(1).equalsIgnoreCase("[Command]")) return null;
            if (!player.hasPermission("craftbook.mech.command")) {
                throw new InsufficientPermissionsException();
            }

            player.print("mech.command.create");
            sign.setLine(1, "[Command]");

            throw new ProcessedMechanismException();
        }
    }

    /**
     * @param trigger if you didn't already check if this is a wall sign with
     *                appropriate text, you're going on Santa's naughty list.
     * @param dir     the direction (UP or DOWN) in which we're looking for a destination
     *
     * @throws InvalidMechanismException
     */
    private Command(Block trigger, MechanismsPlugin plugin) throws InvalidMechanismException {

        super();
        this.trigger = trigger;
        this.plugin = plugin;
    }

    private final MechanismsPlugin plugin;

    private final Block trigger;

    @Override
    public void onRightClick(PlayerInteractEvent event) {

        if (!plugin.getLocalConfiguration().commandSettings.enable) return;
        if (!BukkitUtil.toWorldVector(event.getClickedBlock()).equals(BukkitUtil.toWorldVector(trigger)))
            return; //wth? our manager is insane

        LocalPlayer localPlayer = plugin.wrap(event.getPlayer());

        if (!localPlayer.hasPermission("craftbook.mech.command.use")) {
            localPlayer.printError("mech.use-permission");
            return;
        }

        Sign s = ((Sign) event.getClickedBlock().getState());

        if (s.getLine(2).startsWith("/")) event.getPlayer().chat(s.getLine(2) + s.getLine(3));
        else event.getPlayer().chat("/" + s.getLine(2) + s.getLine(3));

        event.setCancelled(true);
    }

    @Override
    public void onBlockRedstoneChange(BlockPhysicsEvent event) {
        /* we only affect players, so we don't care about redstone events */
    }

    @Override
    public void unload() {
        /* we're not persistent */
    }

    @Override
    public boolean isActive() {
        /* we're not persistent */
        return false;
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {

    }

    @Override
    public void unloadWithEvent(ChunkUnloadEvent event) {

    }
}
