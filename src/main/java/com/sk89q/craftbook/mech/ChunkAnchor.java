package com.sk89q.craftbook.mech;

import com.sk89q.craftbook.*;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.Arrays;
import java.util.List;

public class ChunkAnchor extends PersistentMechanic {

    public static class Factory extends AbstractMechanicFactory<ChunkAnchor> {

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
        public ChunkAnchor detect(BlockWorldVector pt) throws InvalidMechanismException {

            Block block = BukkitUtil.toBlock(pt);

            if (block.getState() instanceof Sign) {
                Sign s = (Sign) block.getState();
                if (s.getLine(1).equalsIgnoreCase("[Chunk]")) return new ChunkAnchor(block);
            }
            return null;
        }

        /**
         * Detect the mechanic at a placed sign.
         *
         * @throws ProcessedMechanismException
         */
        @Override
        public ChunkAnchor detect(BlockWorldVector pt, LocalPlayer player,
                                  ChangedSign sign) throws InvalidMechanismException,
                ProcessedMechanismException {

            if (!sign.getLine(1).equalsIgnoreCase("[Chunk]")) return null;
            if (!player.hasPermission("craftbook.mech.chunk")) throw new InsufficientPermissionsException();

            player.print("mech.anchor.create");
            sign.setLine(1, "[Chunk]");

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
    private ChunkAnchor(Block trigger) throws InvalidMechanismException {

        super();
        this.trigger = trigger;
    }

    private final Block trigger;

    @Override
    public void onRightClick(PlayerInteractEvent event) {

    }

    @Override
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {

        Block block = event.getBlock();
        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            try {
                sign.setLine(3, event.getNewCurrent() > event.getOldCurrent() ? "on" : "off");
                sign.update();
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public boolean isActive() {

        return true;
    }

    @Override
    public List<BlockWorldVector> getWatchedPositions() {

        return Arrays.asList(BukkitUtil.toWorldVector(trigger));
    }

    @Override
    public void unloadWithEvent(ChunkUnloadEvent event) {

        boolean isOn = true;
        if (trigger.getState() instanceof Sign) {
            Sign sign = (Sign) trigger.getState();
            isOn = !sign.getLine(3).equalsIgnoreCase("off");
        }

        if (event.getChunk().equals(trigger.getWorld().getChunkAt(trigger))) {
            event.setCancelled(isOn);
        }
    }
}