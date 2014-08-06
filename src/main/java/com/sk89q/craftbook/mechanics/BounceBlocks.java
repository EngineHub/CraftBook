package com.sk89q.craftbook.mechanics;

import java.util.Arrays;
import java.util.List;

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

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "blocks", "A list of blocks that can be sat on.");
        blocks = ItemInfo.parseListFromString(config.getStringList(path + "blocks", Arrays.asList("DIAMOND_BLOCK")));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {

        if(event.getTo().getY() - event.getFrom().getY() > 0.2) { //Sensitivity setting for the jumping, may need tweaking

            if(event.getPlayer().hasPermission("craftbook.mech.bounceblocks.use")) //Do this after the simple arithmatic, permission lookup is slower.
                return;

            Block block = event.getFrom().getBlock().getRelative(BlockFace.DOWN);

            for(ItemInfo check : blocks) {
                if(check.isSame(block)) {

                    //Boom, headshot.
                    Block sign = block.getRelative(BlockFace.DOWN);

                    if(SignUtil.isSign(sign)) {
                        ChangedSign s = BukkitUtil.toChangedSign(sign);

                        if(s.getLine(1).equals("[Jump]")) {

                            double x = 0,y = 0,z = 0;

                            String[] bits = RegexUtil.COMMA_PATTERN.split(s.getLine(2));
                            if(bits.length == 0) return;
                            if(bits.length == 1)
                                y = Double.parseDouble(bits[0]);
                            else {
                                x = Double.parseDouble(bits[0]);
                                y = Double.parseDouble(bits[1]);
                                z = Double.parseDouble(bits[2]);
                            }

                            event.getPlayer().setVelocity(event.getPlayer().getVelocity().add(new Vector(x,y,z)));
                        }
                        break;
                    }
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

        event.setLine(1, "[Jump]");
        lplayer.print("mech.bounceblocks.create");
    }
}