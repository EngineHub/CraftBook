package com.sk89q.craftbook.gates.world;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.bukkit.CircuitsPlugin;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.util.LocationUtil;

/**
 * @author Me4502
 */
public class PlayerDetection extends AbstractIC {

    public PlayerDetection(Server server, Sign block, ICFactory factory) {

        super(server, block, factory);
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

        if (chip.getInput(0)) chip.setOutput(0, isDetected());
    }

    protected boolean isDetected() {

        int radius = 10; //Default Radius
        Location location = getSign().getLocation();
        try {
            radius = Integer.parseInt(getSign().getLine(2).split("=")[0]);
            if(getSign().getLine(2).contains("=")) {
                int x = Integer.parseInt(getSign().getLine(2).split("=")[1].split(":")[0]);
                int y = Integer.parseInt(getSign().getLine(2).split("=")[1].split(":")[1]);
                int z = Integer.parseInt(getSign().getLine(2).split("=")[1].split(":")[2]);
                location.add(x, y, z);
            }
        }
        catch(Exception e){}

        Boolean isGroup = getSign().getLine(3).startsWith("g:");
        if(getSign().getLine(3).length() == 0)
            isGroup = null;

        for(Player e : getServer().getOnlinePlayers()) {
            if(e == null) continue;
            if(!LocationUtil.isWithinRadius(getSign().getLocation(), e.getLocation(), radius))
                continue;
            if(e.isDead() || !e.isValid()) continue;
            if(isGroup == null)
                return true;
            if(!isGroup)//player
                return e.getName().startsWith(getSign().getLine(3).split(":")[1]);
            else
                return CircuitsPlugin.getInst().isInGroup(e.getName(), getSign().getLine(3).split(":")[1]);
        }

        return false;
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new PlayerDetection(getServer(), sign, this);
        }
    }
}