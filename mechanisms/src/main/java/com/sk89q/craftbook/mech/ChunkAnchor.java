package com.sk89q.craftbook.mech;

import java.util.Arrays;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.InsufficientPermissionsException;
import com.sk89q.craftbook.InvalidMechanismException;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.PersistentMechanic;
import com.sk89q.craftbook.ProcessedMechanismException;
import com.sk89q.craftbook.SourcedBlockRedstoneEvent;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.bukkit.BukkitUtil;

public class ChunkAnchor extends PersistentMechanic {

    public static class Factory extends AbstractMechanicFactory<ChunkAnchor> {

        public Factory(MechanismsPlugin plugin) {

            this.plugin = plugin;
        }

        private final MechanismsPlugin plugin;

        /**
         * Explore around the trigger to find a functional chunk anchor sign; throw if
         * things look funny.
         *
         * @param pt the trigger (should be a signpost)
         *
         * @return A chunk anchor if we could make a valid one
         *
         * @throws InvalidMechanismException if it failed to find the anchor, but it was similar to one
         */
        @Override
        public ChunkAnchor detect(BlockWorldVector pt) throws InvalidMechanismException {

            Block block = BukkitUtil.toBlock(pt);

            if (block.getState() instanceof Sign) {
                Sign s = (Sign) block.getState();
                if (s.getLine(1).equalsIgnoreCase("[Chunk]")) return new ChunkAnchor(block, plugin);
            }
            return null;
        }

        /**
         * Detect the mechanic at a placed sign.
         *
         * @throws ProcessedMechanismException
         */
        @Override
        public ChunkAnchor detect(BlockWorldVector pt, LocalPlayer player, Sign sign)
                throws InvalidMechanismException, ProcessedMechanismException {

            if (!sign.getLine(1).equalsIgnoreCase("[Chunk]")) return null;
            if (!player.hasPermission("craftbook.mech.chunk")) throw new InsufficientPermissionsException();

            player.print("Chunk Anchor created.");
            sign.setLine(1, "[Chunk]");

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
    private ChunkAnchor(Block trigger, MechanismsPlugin plugin) throws InvalidMechanismException {

        super();
        this.trigger = trigger;
    }

    private final Block trigger;

    @Override
    public void onRightClick(PlayerInteractEvent event) {

    }

    @Override
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {

    }

    @Override
    public void unload() {

    }

    @Override
    public boolean isActive() {

        return true;
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {

    }

    @Override
    public List<BlockWorldVector> getWatchedPositions() {

        return Arrays.asList(BukkitUtil.toWorldVector(trigger));
    }

    @Override
    public void unloadWithEvent(ChunkUnloadEvent event) {

        if (event.getChunk().equals(trigger.getWorld().getChunkAt(trigger))) event.setCancelled(true);
    }
}