package com.sk89q.craftbook.gates.world;

import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.bukkit.CircuitsPlugin;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICUtil;
import com.sk89q.craftbook.ic.ICVerificationException;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.SignUtil;

/**
 * @author Silthus
 */
public class PlayerDetection extends AbstractIC {

    private Block center;
    private Set<Chunk> chunks;
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
        radius = ICUtil.parseRadius(sign);

        if (getSign().getLine(2).contains("=")) {
            getSign().setLine(2, radius + "=" + getSign().getLine(2).split("=")[1]);
            center = ICUtil.parseBlockLocation(getSign());
        } else {
            getSign().setLine(2, radius + "");
            center = SignUtil.getBackBlock(getSign().getBlock());
        }
        // parse the group or player name
        String line = sign.getLine(3).trim();
        try {
            if (line.contains("p:")) player = line.split(":")[1];
            else if (line.contains("g:")) group = line.split(":")[1];
        } catch (Exception e) {
            // do nothing and use the defaults
        }
        chunks = LocationUtil.getSurroundingChunks(center, radius);
        sign.update();
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

        for (Chunk chunk : chunks) {
            if (chunk.isLoaded()) {
                // get all entites from the chunks in the defined radius
                for (Entity entity : chunk.getEntities()) {
                    if (!entity.isDead() && entity.isValid()) {
                        if (entity instanceof Player) {
                            // at last check if the entity is within the radius
                            if (LocationUtil.isWithinRadius(center.getLocation(), entity.getLocation(), radius)) {
                                if (!player.equals("")) {
                                    return ((Player) entity).getName().equalsIgnoreCase(player);
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
