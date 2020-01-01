package com.sk89q.craftbook.mechanics;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.events.SourcedBlockRedstoneEvent;
import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.world.ChunkLoadEvent;

public class ChunkAnchor extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(!event.getLine(1).equalsIgnoreCase("[chunk]")) return;
        CraftBookPlayer lplayer = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if(!lplayer.hasPermission("craftbook.mech.chunk")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                lplayer.printError("mech.create-permission");
            SignUtil.cancelSign(event);
            return;
        }

        if(checkChunks) {
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
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!EventUtil.passesFilter(event)) return;

        updateChunkTicket(event.getChunk());
    }

    @EventHandler
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(!allowRedstone) return;
        Block block = event.getBlock();
        if (SignUtil.isSign(block)) {
            ChangedSign sign = CraftBookBukkitUtil.toChangedSign(block);

            if(!sign.getLine(1).equals("[Chunk]")) return;

            sign.setLine(3, event.getNewCurrent() > event.getOldCurrent() ? "on" : "off");
            sign.update(false);

            updateChunkTicket(event.getBlock().getChunk());
        }
    }

    private void updateChunkTicket(Chunk chunk) {
        boolean shouldAnchor = false;

        for(BlockState state : chunk.getTileEntities()) {
            if(state == null) continue;
            if(state instanceof Sign) {
                if(((Sign) state).getLine(1).equals("[Chunk]")) {
                    if (!allowRedstone || !((Sign) state).getLine(3).equalsIgnoreCase("off")) {
                        shouldAnchor = true;
                        break;
                    }
                }
            }
        }

        if (shouldAnchor) {
            chunk.addPluginChunkTicket(CraftBookPlugin.inst());
        } else {
            chunk.removePluginChunkTicket(CraftBookPlugin.inst());
        }
    }

    private boolean allowRedstone;
    private boolean checkChunks;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "enable-redstone", "Enable toggling with redstone.");
        allowRedstone = config.getBoolean(path + "enable-redstone", true);

        config.setComment(path + "check-chunks", "On creation, check the chunk for already existing chunk anchors.");
        checkChunks = config.getBoolean(path + "check-chunks", true);
    }
}