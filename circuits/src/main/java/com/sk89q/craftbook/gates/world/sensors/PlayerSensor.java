package com.sk89q.craftbook.gates.world.sensors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.bukkit.CircuitsPlugin;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.ICUtil;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.util.GeneralUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * @author Me4502
 */
public class PlayerSensor extends AbstractIC {

    public PlayerSensor(Server server, ChangedSign block, ICFactory factory) {

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

    Location location;
    ProtectedRegion reg;
    Type type;
    String nameLine;

    @Override
    public void load() {

        if (getLine(3).contains(":")) {
            type = Type.getFromChar(getLine(3).trim().toCharArray()[0]);
        }
        if (type == null) type = Type.PLAYER;

        nameLine = getSign().getLine(3).replace("g:", "").replace("p:", "").trim();

        try {
            String locInfo = getLine(2);
            if (locInfo.startsWith("r:") && CircuitsPlugin.getInst().getWorldGuard() != null) {

                locInfo = locInfo.replace("r:", "");
                reg = CircuitsPlugin.getInst().getWorldGuard().getRegionManager(BukkitUtil.toSign(getSign()).getWorld
                        ()).getRegion(locInfo);
                if (reg != null) return;
            }
            radius = ICUtil.parseRadius(getSign());
            if (locInfo.contains("=")) {
                location = ICUtil.parseBlockLocation(getSign()).getLocation();
            } else {
                location = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock()).getLocation();
            }
        } catch (Exception e) {
            location = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock()).getLocation();
            Bukkit.getLogger().severe(GeneralUtil.getStackTrace(e));
        }
    }

    protected boolean isDetected() {

        if (reg != null) {

            for (Player p : BukkitUtil.toSign(getSign()).getWorld().getPlayers()) {
                if (reg.contains(p.getLocation().getBlockX(), p.getLocation().getBlockY(),
                        p.getLocation().getBlockZ())) {
                    return true;
                }
            }
        }

        if (location != null) {
            if (!nameLine.isEmpty() && type == Type.PLAYER) {
                Player p = Bukkit.getPlayer(nameLine);
                if (p != null && LocationUtil.isWithinRadius(location, p.getLocation(), radius)) return true;
            }
            for (Player e : getServer().getOnlinePlayers()) {
                if (e == null || !e.isValid() || !LocationUtil.isWithinRadius(location, e.getLocation(), radius)) {
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

        PLAYER('p'), GROUP('g');

        private Type(char prefix) {

            this.prefix = prefix;
        }

        char prefix;

        public static Type getFromChar(char c) {

            c = Character.toLowerCase(c);
            for (Type t : values()) { if (t.prefix == c) return t; }
            return null;
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new PlayerSensor(getServer(), sign, this);
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