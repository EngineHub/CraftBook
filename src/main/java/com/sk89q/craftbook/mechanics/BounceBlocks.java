package com.sk89q.craftbook.mechanics;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.util.BlockSyntax;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.blocks.Blocks;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.apache.commons.lang.StringUtils;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class BounceBlocks extends AbstractCraftBookMechanic {

    private List<BaseBlock> blocks;
    private double sensitivity;
    private Map<BaseBlock, Vector> autoBouncers = new HashMap<>();

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "blocks", "A list of blocks that can be jumped on.");
        blocks = BlockSyntax.getBlocks(config.getStringList(path + "blocks", Collections.singletonList(BlockTypes.DIAMOND_BLOCK.getId())), true);

        config.setComment(path + "sensitivity", "The sensitivity of jumping.");
        sensitivity = config.getDouble(path + "sensitivity", 0.1);

        if(config.getKeys(path + "auto-blocks") == null)
            config.addNode(path + "auto-blocks");

        config.setComment(path + "auto-blocks", "Blocks that automatically apply forces when jumped on.");
        for(String key : config.getKeys(path + "auto-blocks")) {

            double x = 0,y = 0,z = 0;

            String[] bits = RegexUtil.COMMA_PATTERN.split(config.getString(path + "auto-blocks." + key));
            if (bits.length == 0) {
                y = 0.5;
            } else if (bits.length == 1) {
                try {
                    y = Double.parseDouble(bits[0]);
                } catch (NumberFormatException e) {
                    y = 0.5;
                }
            } else {
                x = Double.parseDouble(bits[0]);
                y = Double.parseDouble(bits[1]);
                z = Double.parseDouble(bits[2]);
            }

            BaseBlock block = BlockSyntax.getBlock(key, true);

            autoBouncers.put(block, new Vector(x,y,z));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(final PlayerMoveEvent event) {

        if(Math.abs(event.getTo().getY() - event.getFrom().getY()) > sensitivity && event.getFrom().getY() - event.getFrom().getBlockY() < 0.25) { //Sensitivity setting for the jumping, may need tweaking

            if(!event.getPlayer().hasPermission("craftbook.mech.bounceblocks.use")) //Do this after the simple arithmatic, permission lookup is slower.
                return;

            Block block = event.getFrom().getBlock().getRelative(BlockFace.DOWN);

            if(Blocks.containsFuzzy(blocks, BukkitAdapter.adapt(block.getBlockData()))) {

                CraftBookPlugin.logDebugMessage("Player jumped on a block that is a BoucneBlock!", "bounce-blocks");

                //Boom, headshot.
                Block sign = block.getRelative(BlockFace.DOWN);

                if(SignUtil.isSign(sign)) {
                    final ChangedSign s = CraftBookBukkitUtil.toChangedSign(sign);

                    if(s.getLine(1).equals("[Jump]")) {

                        CraftBookPlugin.logDebugMessage("Jump sign found where player jumped!", "bounce-blocks");

                        double x = 0,y,z = 0;
                        boolean straight = s.getLine(2).startsWith("!");

                        String[] bits = RegexUtil.COMMA_PATTERN.split(StringUtils.replace(s.getLine(2), "!", ""));
                        if (bits.length == 0) {
                            y = 0.5;
                        } else if (bits.length == 1) {
                            try {
                                y = Double.parseDouble(bits[0]);
                            } catch (NumberFormatException e) {
                                y = 0.5;
                            }
                        } else {
                            x = Double.parseDouble(bits[0]);
                            y = Double.parseDouble(bits[1]);
                            z = Double.parseDouble(bits[2]);
                        }

                        if(!straight) {

                            Vector facing = event.getTo().getDirection();

                            //Find out the angle they are facing. This is completely to do with horizontals. No verticals are taken into account.
                            double angle = Math.atan2(facing.getX(), facing.getZ());

                            x = Math.sin(angle)*x;
                            z = Math.cos(angle)*z;
                        }

                        event.getPlayer().setVelocity(new Vector(x,y,z));
                        event.getPlayer().setFallDistance(-20f);
                    }
                    return;
                }
            }

            for(Entry<BaseBlock, Vector> entry : autoBouncers.entrySet()) {
                if(entry.getKey().equalsFuzzy(BukkitAdapter.adapt(block.getBlockData()))) {

                    CraftBookPlugin.logDebugMessage("Player jumped on a auto block that is a BoucneBlock!", "bounce-blocks");

                    CraftBookPlugin.logDebugMessage("Jump sign found where player jumped!", "bounce-blocks");

                    double x = entry.getValue().getX(), y = entry.getValue().getY(), z = entry.getValue().getZ();

                    Vector facing = event.getTo().getDirection();

                    //Find out the angle they are facing. This is completely to do with horizontals. No verticals are taken into account.
                    double angle = Math.atan2(facing.getX(), facing.getZ());

                    x = Math.sin(angle)*x;
                    z = Math.cos(angle)*z;

                    event.getPlayer().setVelocity(new Vector(x,y,z));
                    event.getPlayer().setFallDistance(-20f);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(!event.getLine(1).equalsIgnoreCase("[jump]")) return;
        CraftBookPlayer lplayer = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if(!lplayer.hasPermission("craftbook.mech.bounceblocks")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                lplayer.printError("mech.create-permission");
            SignUtil.cancelSign(event);
            return;
        }

        try {
            String[] bits = RegexUtil.COMMA_PATTERN.split(StringUtils.replace(event.getLine(2), "!", ""));
            if (bits.length == 1) {
                Double.parseDouble(bits[0]);
            } else if (bits.length > 1) {
                Double.parseDouble(bits[0]);
                Double.parseDouble(bits[1]);
                Double.parseDouble(bits[2]);
            }
        } catch(Exception e){
            lplayer.printError("mech.bounceblocks.invalid-velocity");
            SignUtil.cancelSign(event);
            return;
        }

        event.setLine(1, "[Jump]");
        lplayer.print("mech.bounceblocks.create");
    }
}