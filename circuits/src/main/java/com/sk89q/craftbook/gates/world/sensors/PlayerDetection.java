package com.sk89q.craftbook.gates.world.sensors;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.bukkit.CircuitsPlugin;
import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.GeneralUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;

/**
 * @author Me4502
 */
public class PlayerDetection extends AbstractIC {

    public PlayerDetection(Server server, ChangedSign block, ICFactory factory) {

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

    int radius;

    int x,y,z;

    Location location = null;
    ProtectedRegion reg = null;

    @Override
    public void load() {
        radius = 10; //Default Radius
        location = BukkitUtil.toSign(getSign()).getLocation();
        try {
            if(getLine(2).startsWith("r:") && CircuitsPlugin.getInst().getWorldGuard() != null) {

                String region = getLine(2).replace("r:", "");
                reg = CircuitsPlugin.getInst().getWorldGuard().getRegionManager(BukkitUtil.toSign(getSign()).getWorld()).getRegion(region);
                if(reg == null) {
                    String[] splitEquals = ICUtil.EQUALS_PATTERN.split(getSign().getLine(2), 2);
                    radius = Integer.parseInt(splitEquals[0]);
                    if (getSign().getLine(2).contains("=")) {
                        String[] splitCoords = ICUtil.COLON_PATTERN.split(splitEquals[1]);
                        x = Integer.parseInt(splitCoords[0]);
                        y = Integer.parseInt(splitCoords[1]);
                        z = Integer.parseInt(splitCoords[2]);
                        location.add(x, y, z);
                    }
                }
            }
            else {
                String[] splitEquals = ICUtil.EQUALS_PATTERN.split(getSign().getLine(2), 2);
                radius = Integer.parseInt(splitEquals[0]);
                if (getSign().getLine(2).contains("=")) {
                    String[] splitCoords = ICUtil.COLON_PATTERN.split(splitEquals[1]);
                    x = Integer.parseInt(splitCoords[0]);
                    y = Integer.parseInt(splitCoords[1]);
                    z = Integer.parseInt(splitCoords[2]);
                    location.add(x, y, z);
                }
            }
        } catch (NullPointerException e) {
            Bukkit.getLogger().severe(GeneralUtil.getStackTrace(e));
        } catch (Exception ignored) {
        }
    }

    protected boolean isDetected() {

        Type type = Type.PLAYER;
        if(getLine(3).contains(":")) {
            type = Type.getFromChar(getLine(3).toCharArray()[0]);
        }
        String nameLine = getSign().getLine(3).replace("g:", "").replace("p:", "");

        if(reg != null) {

            for(Player p : BukkitUtil.toSign(getSign()).getWorld().getPlayers()) {
                if(reg.contains(p.getLocation().getBlockX(),p.getLocation().getBlockY(),p.getLocation().getBlockZ())) {
                    return true;
                }
            }
        }
        else if(location != null) {
            if(type == Type.PLAYER) {
                Player p = Bukkit.getPlayer(nameLine);
                if (p != null && LocationUtil.isWithinRadius(location, p.getLocation(), radius))
                    return true;
            }
            for (Player e : getServer().getOnlinePlayers()) {
                if (e == null || !e.isValid()
                        || !LocationUtil.isWithinRadius(location, e.getLocation(), radius)) {
                    continue;
                }

                if (nameLine.isEmpty()) {
                    return true;
                } else if (type == Type.PLAYER && e.getName().toLowerCase().startsWith(nameLine.toLowerCase())) {
                    return true;
                } else if (type == Type.GROUP && CircuitsPlugin.getInst().isInGroup(e.getName(), nameLine)) {
                    return true;
                }
            }
        }

        return false;
    }

    private enum Type {

        PLAYER('p'),
        GROUP('g');

        private Type(char prefix) {

            this.prefix = prefix;
        }

        char prefix;

        public static Type getFromChar(char c) {
            c = Character.toLowerCase(c);
            for(Type t : values())
                if(t.prefix == c)
                    return t;
            return PLAYER;
        }
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new PlayerDetection(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Detects players within a radius.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "radius=x:y:z offset, or r:regionname for WorldGuard regions",
                    "p:playername or g:permissiongroup"
            };
            return lines;
        }
    }
}