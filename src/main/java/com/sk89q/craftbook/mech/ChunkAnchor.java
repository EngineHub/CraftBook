package com.sk89q.craftbook.mech;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.events.SourcedBlockRedstoneEvent;

public class ChunkAnchor extends AbstractCraftBookMechanic {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        if(!event.getLine(1).equalsIgnoreCase("[chunk]")) return;
        LocalPlayer lplayer = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if(!lplayer.hasPermission("craftbook.mech.chunk")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                lplayer.printError("mech.create-permission");
            SignUtil.cancelSign(event);
            return;
        }

        if(CraftBookPlugin.inst().getConfiguration().chunkAnchorCheck) {
            for(BlockState state : event.getBlock().getChunk().getTileEntities()) {
                if(state instanceof Sign) {
                    Sign s = (Sign) state;
                    if(s.getLine(1).equalsIgnoreCase("[Chunk]")) {
                        lplayer.printError("mech.anchor.already-anchored");
                        SignUtil.cancelSign(event);
                        return;
                    }
                }
            }
        }

        event.setLine(1, "[Chunk]");
        lplayer.print("mech.anchor.create");
    }

    @EventHandler
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {

        if(!CraftBookPlugin.inst().getConfiguration().chunkAnchorRedstone) return;
        Block block = event.getBlock();
        if (SignUtil.isSign(block)) {
            ChangedSign sign = BukkitUtil.toChangedSign(block);

            if(!sign.getLine(1).equals("[Chunk]")) return;

            sign.setLine(3, event.getNewCurrent() > event.getOldCurrent() ? "on" : "off");
            sign.update(false);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onUnload(final ChunkUnloadEvent event) {

        boolean isOn = false;
        boolean foundSign = false;

        for(BlockState state : event.getChunk().getTileEntities()) {
            if(state instanceof Sign) {
                if(((Sign) state).getLine(1).equals("[Chunk]")) {
                    foundSign = true;
                    isOn = !((Sign) state).getLine(3).equalsIgnoreCase("off");
                    break;
                }
            }
        }

        if (!foundSign) return;
        if (!isOn && CraftBookPlugin.inst().getConfiguration().chunkAnchorRedstone) return;
        event.setCancelled(true);
        CraftBookPlugin.inst().getServer().getScheduler().runTaskLater(CraftBookPlugin.inst(), new Runnable() {

            @Override
            public void run () {
                event.getWorld().loadChunk(event.getChunk().getX(), event.getChunk().getZ(), true);
            }

        }, 2L);
    }
}