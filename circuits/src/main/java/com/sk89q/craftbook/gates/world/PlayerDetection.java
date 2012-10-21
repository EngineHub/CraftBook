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

        if (chip.getInput(0)) {
            chip.setOutput(0, isDetected());
        }
    }

    protected boolean isDetected() {

        int radius = 10; //Default Radius
        Location location = getSign().getLocation();
        try {
            radius = Integer.parseInt(getSign().getLine(2).split("=")[0]);
            if (getSign().getLine(2).contains("=")) {
                int x = Integer.parseInt(getSign().getLine(2).split("=")[1].split(":")[0]);
                int y = Integer.parseInt(getSign().getLine(2).split("=")[1].split(":")[1]);
                int z = Integer.parseInt(getSign().getLine(2).split("=")[1].split(":")[2]);
                location.add(x, y, z);
            }
        } catch (Exception ignored) {
        }

        for (Player e : getServer().getOnlinePlayers()) {
            if (e == null || !e.isValid()
                    || !LocationUtil.isWithinRadius(getSign().getLocation(), e.getLocation(), radius)) {
                continue;
            }

            String nameLine = getSign().getLine(3);
            if (nameLine.length() > 0) {
                nameLine = nameLine.replace("g:", "").replace("p:", "");
                if(e.getName().toLowerCase().startsWith(nameLine.toLowerCase())
                        || CircuitsPlugin.getInst().isInGroup(e.getName(), nameLine))
                    return true;
                else
                    continue;
            }
            continue;
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