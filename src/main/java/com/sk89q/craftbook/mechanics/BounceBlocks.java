package com.sk89q.craftbook.mechanics;

import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.util.yaml.YAMLProcessor;

public class BounceBlocks extends AbstractCraftBookMechanic {

    List<ItemInfo> blocks;
    double sensitivity;
    Map<ItemInfo, Vector> autoBouncers = new HashMap<ItemInfo, Vector>();

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "blocks", "A list of blocks that can be jumped on.");
        blocks = ItemInfo.parseListFromString(config.getStringList(path + "blocks", Collections.singletonList("DIAMOND_BLOCK")));

        config.setComment(path + "sensitivity", "The sensitivity of jumping.");
        sensitivity = config.getDouble(path + "sensitivity", 0.1);

        if(config.getKeys(path + "auto-blocks") == null)
            config.addNode(path + "auto-blocks");

        config.setComment(path + "auto-blocks", "Blocks that automatically apply forces when jumped on.");
        for(String key : config.getKeys(path + "auto-blocks")) {

            double x = 0,y = 0,z = 0;

            String[] bits = RegexUtil.COMMA_PATTERN.split(config.getString(path + "auto-blocks." + key));
            if(bits.length == 0)
                y = 0.5;
            if(bits.length == 1)
                y = Double.parseDouble(bits[0]);
            else {
                x = Double.parseDouble(bits[0]);
                y = Double.parseDouble(bits[1]);
                z = Double.parseDouble(bits[2]);
            }

            ItemInfo block = new ItemInfo(key);

            autoBouncers.put(block, new Vector(x,y,z));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(final PlayerMoveEvent event) {

        if(Math.abs(event.getTo().getY() - event.getFrom().getY()) > sensitivity && event.getFrom().getY() - event.getFrom().getBlockY() < 0.25) { //Sensitivity setting for the jumping, may need tweaking

            if(!event.getPlayer().hasPermission("craftbook.mech.bounceblocks.use")) //Do this after the simple arithmatic, permission lookup is slower.
                return;

            Block block = event.getFrom().getBlock().getRelative(BlockFace.DOWN);

            for(ItemInfo check : blocks) {
                if(check.isSame(block)) {

                    CraftBookPlugin.logDebugMessage("Player jumped on a block that is a BoucneBlock!", "bounce-blocks");

                    //Boom, headshot.
                    Block sign = block.getRelative(BlockFace.DOWN);

                    if(SignUtil.isSign(sign)) {
                        final ChangedSign s = BukkitUtil.toChangedSign(sign);

                        if(s.getLine(1).equals("[Jump]")) {

                            CraftBookPlugin.logDebugMessage("Jump sign found where player jumped!", "bounce-blocks");

                            double x = 0,y = 0,z = 0;
                            boolean straight = s.getLine(2).startsWith("!");

                            String[] bits = RegexUtil.COMMA_PATTERN.split(StringUtils.replace(s.getLine(2), "!", ""));
                            if(bits.length == 0)
                                y = 0.5;
                            if(bits.length == 1)
                                y = Double.parseDouble(bits[0]);
                            else {
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
            }

            for(Entry<ItemInfo, Vector> entry : autoBouncers.entrySet()) {
                if(entry.getKey().isSame(block)) {

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
        LocalPlayer lplayer = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if(!lplayer.hasPermission("craftbook.mech.bounceblocks")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                lplayer.printError("mech.create-permission");
            SignUtil.cancelSign(event);
            return;
        }

        try {
            String[] bits = RegexUtil.COMMA_PATTERN.split(StringUtils.replace(event.getLine(2), "!", ""));
            if(bits.length == 0)
                if(bits.length == 1)
                    Double.parseDouble(bits[0]);
                else {
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