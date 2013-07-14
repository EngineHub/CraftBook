package com.sk89q.craftbook.mech;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;

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

    public HashSet<String> footsteps = new HashSet<String>();

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(final PlayerMoveEvent event) {

        if(event.getFrom().getX() == event.getTo().getX() && event.getFrom().getZ() == event.getTo().getZ())
            return;

        if (disabled) return;
        if(!CraftBookPlugin.inst().getConfiguration().footprintsEnabled)
            return;
        Block below = event.getPlayer().getLocation().subtract(0, 1, 0).getBlock(); //Gets the block they're standing on
        double yOffset = 0.07D;

        if(event.getPlayer().getLocation().getBlock().getTypeId() == BlockID.SNOW || event.getPlayer().getLocation().getBlock().getTypeId() == BlockID.CARPET || event.getPlayer().getLocation().getBlock().getTypeId() == BlockID.SLOW_SAND) {
            below = event.getPlayer().getLocation().getBlock();
            yOffset = 0.15D;
            if(event.getPlayer().getLocation().getBlock().getTypeId() == BlockID.SNOW && event.getPlayer().getLocation().getBlock().getData() == 0 || event.getPlayer().getLocation().getBlock().getTypeId() == BlockID.CARPET) {
                yOffset = below.getY() - event.getPlayer().getLocation().getY();
                yOffset += 0.15D;
            }
        } else if (event.getPlayer().getLocation().getY() != below.getY() + 1)
            return;

        if(CraftBookPlugin.inst().getConfiguration().footprintsBlocks.contains(Integer.valueOf(below.getTypeId()))) {

            if(footsteps.contains(event.getPlayer().getName()))
                return;

            if(!event.getPlayer().hasPermission("craftbook.mech.footprints.use"))
                return;

            try {
                PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(63);
                packet.getStrings().write(0, "footstep");
                packet.getFloat().write(0, (float) event.getPlayer().getLocation().getX())
                .write(1, (float) (event.getPlayer().getLocation().getY() + yOffset))
                .write(2, (float) event.getPlayer().getLocation().getZ())
                .write(3, 0F)
                .write(4, 0F)
                .write(5, 0F)
                .write(6, 0F);
                packet.getIntegers().write(0, 1);
                for (Player play : CraftBookPlugin.inst().getServer().getOnlinePlayers()) {
                    if(!play.canSee(event.getPlayer()))
                        continue;
                    if(!play.hasPermission("craftbook.mech.footprints.see"))
                        continue;
                    if (play.getWorld().equals(event.getPlayer().getPlayer().getWorld())) {
                        try {
                            ProtocolLibrary.getProtocolManager().sendServerPacket(play, packet);
                        } catch (InvocationTargetException e) {
                            BukkitUtil.printStacktrace(e);
                        }
                    }
                }

                footsteps.add(event.getPlayer().getName());
                CraftBookPlugin.inst().getServer().getScheduler().runTaskLater(CraftBookPlugin.inst(), new Runnable() {

                    @Override
                    public void run () {
                        footsteps.remove(event.getPlayer().getName());
                    }
                }, event.getPlayer().isSprinting() ? 7 : 10);
            } catch (Throwable e) {
                CraftBookPlugin.logger().warning("Footprints do not work without ProtocolLib!");
                disabled = true;
                return;
            }
        }
    }
}