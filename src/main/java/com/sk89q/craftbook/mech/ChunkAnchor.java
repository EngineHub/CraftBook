package com.sk89q.craftbook.mech;

import java.util.Arrays;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.PersistentMechanic;
import com.sk89q.craftbook.SelfTriggeringMechanic;
import com.sk89q.craftbook.SourcedBlockRedstoneEvent;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.exceptions.InsufficientPermissionsException;
import com.sk89q.craftbook.util.exceptions.InvalidMechanismException;
import com.sk89q.craftbook.util.exceptions.ProcessedMechanismException;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.bukkit.BukkitUtil;

public class ChunkAnchor extends PersistentMechanic implements SelfTriggeringMechanic {

    public static class Factory extends AbstractMechanicFactory<ChunkAnchor> {

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

            if(CraftBookPlugin.inst().getConfiguration().chunkAnchorCheck) {

                for(BlockState entity : BukkitUtil.toBlock(pt).getChunk().getTileEntities()) {

                    if(entity instanceof Sign) {

                        Sign s = (Sign) entity;
                        if(s.getLine(1).equalsIgnoreCase("[Chunk]")) {

                            throw new InvalidMechanismException("mech.anchor.already-anchored");
                        }
                    }
                }
            }

            player.print("mech.anchor.create");
            sign.setLine(1, "[Chunk]");

            throw new ProcessedMechanismException();
        }
    }

    public boolean isOn = true;

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

        if (trigger.getState() instanceof Sign) {
            Sign sign = (Sign) trigger.getState();
            isOn = !sign.getLine(3).equalsIgnoreCase("off");
        }
    }

    private final Block trigger;

    @Override
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {

        if(!CraftBookPlugin.inst().getConfiguration().chunkAnchorRedstone) return;
        Block block = event.getBlock();
        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            sign.setLine(3, event.getNewCurrent() > event.getOldCurrent() ? "on" : "off");
            isOn = !sign.getLine(3).equalsIgnoreCase("off");
            sign.update();
        }
    }

    @Override
    public boolean isActive() {

        return isOn;
    }

    @Override
    public List<BlockWorldVector> getWatchedPositions() {

        return Arrays.asList(BukkitUtil.toWorldVector(trigger));
    }

    @Override
    public void unloadWithEvent(final ChunkUnloadEvent event) {

        if (!isOn && CraftBookPlugin.inst().getConfiguration().chunkAnchorRedstone) return;
        event.setCancelled(true);
        CraftBookPlugin.inst().getServer().getScheduler().runTaskLater(CraftBookPlugin.inst(), new Runnable() {

            @Override
            public void run () {
                event.getWorld().loadChunk(event.getChunk().getX(), event.getChunk().getZ(), true);
            }

        }, 2L);
    }

    @Override
    public void think () {
        //Do nothing. We are ST so it keeps running, to keep chunks loaded.
    }
}