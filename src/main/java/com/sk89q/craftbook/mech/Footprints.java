package com.sk89q.craftbook.mech;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.worldedit.blocks.BlockID;

public class Footprints implements Listener {

    private boolean disabled = false;

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {

        if(event.getFrom().distanceSquared(event.getTo()) == 0)
            return;

        if (disabled) return;
        if(!CraftBookPlugin.inst().getConfiguration().footprintsEnabled)
            return;
        Block below = event.getPlayer().getLocation().subtract(0, 1, 0).getBlock(); //Gets the block they're standing on
        double yOffset = 0.03D;

        if(event.getPlayer().getLocation().getBlock().getTypeId() == BlockID.SNOW) {
            below = event.getPlayer().getLocation().getBlock();
            yOffset = 0.13D;
        }

        if(CraftBookPlugin.inst().getConfiguration().footprintsBlocks.contains(below.getTypeId())) {

            try {
                PacketContainer entitymeta = ProtocolLibrary.getProtocolManager().createPacket(63);
                entitymeta.getSpecificModifier(String.class).write(0, "footstep");
                entitymeta.getSpecificModifier(float.class).write(0, (float) event.getPlayer().getLocation().getX());
                entitymeta.getSpecificModifier(float.class).write(1, (float) ((float) event.getPlayer().getLocation().getY() + yOffset));
                entitymeta.getSpecificModifier(float.class).write(2, (float) event.getPlayer().getLocation().getZ());
                entitymeta.getSpecificModifier(float.class).write(3, 0F);
                entitymeta.getSpecificModifier(float.class).write(4, 0F);
                entitymeta.getSpecificModifier(float.class).write(5, 0F);
                entitymeta.getSpecificModifier(float.class).write(6, 0F);
                entitymeta.getSpecificModifier(int.class).write(0, 1);
                for (Player play : CraftBookPlugin.inst().getServer().getOnlinePlayers()) {
                    if (play.getWorld().equals(event.getPlayer().getPlayer().getWorld())) {
                        try {
                            ProtocolLibrary.getProtocolManager().sendServerPacket(play, entitymeta);
                        } catch (InvocationTargetException e) {
                            BukkitUtil.printStacktrace(e);
                        }
                    }
                }
            } catch (Error e) {
                CraftBookPlugin.logger().warning("Footprints do not work without ProtocolLib!");
                disabled = true;
                return;
            }
        }
    }
}