package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.bukkit.CircuitsPlugin;
import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.SignUtil;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

/**
 * @author Silthus
 */
public class PlayerDetection extends AbstractIC {

    private Block center;
    //private Set<Chunk> chunks;
    private int radius;
    private String player = "";
    private String group = "";

    public PlayerDetection(Server server, Sign block) {

        super(server, block);
        // lets set some defaults
        radius = 0;
        load();
    }

    private void load() {

        Sign sign = getSign();
        // now check the third line for the radius and offset
        // if the line contains a = the offset is given
        // the given string should look something like that:
        // radius=x:y:z or radius, e.g. 1=-2:5:11
        if (getSign().getLine(2).contains("=")) {
            center = ICUtil.parseBlockLocation(getSign());
        } else {
            center = SignUtil.getBackBlock(getSign().getBlock());
        }
        this.radius = ICUtil.parseRadius(sign);
        // parse the group or player name
        String line = sign.getLine(3).trim();
        try {
            if (line.contains("p:")) player = line.split(":")[1];
            else if (line.contains("g:")) group = line.split(":")[1];
        } catch (Exception e) {
            // do nothing and use the defaults
        }
        //this.chunks = LocationUtil.getSurroundingChunks(center, radius);
    }

    @Override
    public String getTitle() {

        return "Player Detection";
    }

    @Override
    public String getSignTitle() {

        return "P-DETECTION";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            chip.setOutput(0, isDetected());
        }
    }

    protected boolean isDetected() {

        /*
        for (Chunk chunk : this.chunks) {
            if (chunk.isLoaded()) {
                // get all entites from the chunks in the defined radius
                for (Entity entity : chunk.getEntities()) {
                    if (!entity.isDead() && entity.isValid()) {
                        if (entity instanceof Player) {
                            // at last check if the entity is within the radius
                            if (LocationUtil.getGreatestDistance(entity.getLocation(),
                                    center.getLocation()) <= radius) {
                                if (!player.equals("")) {
                                    return ((Player) entity).getName().equals(player);
                                } else if (!group.equals("")) {
                                    return CircuitsPlugin.getInst().isInGroup(((Player) entity).getName(), group);
                                } else {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        */

        // Bukkit doesn't currently list players in the Chunk Entity list
        for (Player aPlayer : center.getWorld().getEntitiesByClass(Player.class)) {
            if (!aPlayer.isDead() && aPlayer.isValid()
                    && LocationUtil.getGreatestDistance(aPlayer.getLocation(), center.getLocation()) <= radius) {
                if (!player.equals("")) {
                    return aPlayer.getName().equals(player);
                } else if (!group.equals("")) {
                    return CircuitsPlugin.getInst().isInGroup(aPlayer.getName(), group);
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new PlayerDetection(getServer(), sign);
        }

        @Override
        public void verify(Sign sign) throws ICVerificationException {

            ICUtil.verifySignSyntax(sign);
        }
    }
}
