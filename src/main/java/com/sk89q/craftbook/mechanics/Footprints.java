package com.sk89q.craftbook.mechanics;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.BlockSyntax;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.blocks.Blocks;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class Footprints extends AbstractCraftBookMechanic {

    private Set<String> footsteps;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(final PlayerMoveEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(event.getFrom().getX() == event.getTo().getX() && event.getFrom().getZ() == event.getTo().getZ())
            return;

        if(!event.getPlayer().hasPermission("craftbook.mech.footprints.use"))
            return;

        Block below = event.getPlayer().getLocation().subtract(0, 1, 0).getBlock(); //Gets the block they're standing on
        double yOffset = 0.07D;

        if(event.getPlayer().getLocation().getBlock().getType() == Material.SNOW || Tag.CARPETS.isTagged(event.getPlayer().getLocation().getBlock().getType()) || event.getPlayer().getLocation().getBlock().getType() == Material.SOUL_SAND) {
            below = event.getPlayer().getLocation().getBlock();
            yOffset = 0.15D;
            if(event.getPlayer().getLocation().getBlock().getType() == Material.SNOW && event.getPlayer().getLocation().getBlock().getData() == 0 || Tag.CARPETS.isTagged(event.getPlayer().getLocation().getBlock().getType())) {
                yOffset = below.getY() - event.getPlayer().getLocation().getY();
                yOffset += 0.15D;
            }
        } else if (event.getPlayer().getLocation().getY() != below.getY() + 1)
            return;

        if(Blocks.containsFuzzy(blocks, BukkitAdapter.adapt(below.getBlockData()))) {

            if(footsteps.contains(event.getPlayer().getName()))
                return;

            try {
                for (Player play : CraftBookPlugin.inst().getServer().getOnlinePlayers()) {
                    if(!play.canSee(event.getPlayer()))
                        continue;
                    if(!play.hasPermission("craftbook.mech.footprints.see"))
                        continue;
                    if (play.getWorld().equals(event.getPlayer().getPlayer().getWorld())) {
                        // TODO :'(
//                        play.getWorld().spigot().playEffect(event.getPlayer().getLocation().add(0, yOffset, 0), Effect.FOOTSTEP);
                    }
                }

                footsteps.add(event.getPlayer().getName());
                CraftBookPlugin.inst().getServer().getScheduler().runTaskLater(CraftBookPlugin.inst(),
                        () -> footsteps.remove(event.getPlayer().getName()), event.getPlayer().isSprinting() ? 7 : 10);
            } catch (Throwable e) {
                CraftBookPlugin.logger().log(Level.WARNING, "Failed to send footprints for " + event.getPlayer().getName(), e);
            }
        }
    }

    @Override
    public boolean enable () {

        footsteps = new HashSet<>();
        return true;
    }

    @Override
    public void disable () {
        footsteps = null;
    }

    private List<BaseBlock> blocks;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "blocks", "The list of blocks that footprints appear on.");
        blocks = BlockSyntax.getBlocks(config.getStringList(path + "blocks", Arrays.asList(
                BlockTypes.DIRT.getId(), BlockTypes.SAND.getId(), BlockTypes.SNOW.getId(), BlockTypes.SNOW_BLOCK.getId(), BlockTypes.ICE.getId(),
                BlockTypes.RED_SAND.getId()
        )), true);
    }
}