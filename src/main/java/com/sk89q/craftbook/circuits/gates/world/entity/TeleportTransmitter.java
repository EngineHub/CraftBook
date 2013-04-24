package com.sk89q.craftbook.circuits.gates.world.entity;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.util.HistoryHashMap;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.Tuple2;
import com.sk89q.worldedit.Vector;

public class TeleportTransmitter extends AbstractSelfTriggeredIC {

    public TeleportTransmitter(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    protected static final HistoryHashMap<String, Tuple2<Long, String>> memory = new HistoryHashMap<String, Tuple2<Long, String>>(50);

    protected String band;

    @Override
    public String getTitle() {

        return "Teleport Transmitter";
    }

    @Override
    public String getSignTitle() {

        return "TELEPORT OUT";
    }

    Vector radius;
    Location offset;

    @Override
    public void load() {

        band = getLine(2);
        offset = ICUtil.parseBlockLocation(getSign(), 3).getLocation();
        radius = ICUtil.parseRadius(getSign(), 3);
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0))
            sendPlayer();
    }

    @Override
    public void think (ChipState chip) {

        if (chip.getInput(0))
            sendPlayer();
    }

    public void sendPlayer() {
        Player closest = null;

        for (Player e : offset.getWorld().getPlayers()) {
            if (e == null || !e.isValid() || !LocationUtil.isWithinRadius(offset, e.getLocation(), radius) || e.isDead())
                continue;

            if (closest == null) closest = e;
            else if (LocationUtil.getDistanceSquared(closest.getLocation(), offset) > LocationUtil.getDistanceSquared(e.getLocation(), offset)) closest = e;
        }
        if (closest != null && !setValue(band, new Tuple2<Long, String>(System.currentTimeMillis(), closest.getName())))
            closest.sendMessage(ChatColor.RED + "This Teleporter Frequency is currently busy! Try again soon!");
    }

    public static Tuple2<Long, String> getValue(String band) {

        if (memory.containsKey(band)) {
            long time = System.currentTimeMillis() - memory.get(band).a;
            int seconds = (int) (time / 1000) % 60;
            if (seconds > 5) { // Expired.
                memory.remove(band);
                return null;
            }
        }
        Tuple2<Long, String> val = memory.get(band);
        memory.remove(band); // Remove on teleport.
        return val;
    }

    public static boolean setValue(String band, Tuple2<Long, String> val) {

        if (memory.containsKey(band)) {
            long time = System.currentTimeMillis() - memory.get(band).a;
            int seconds = (int) (time / 1000) % 60;
            if (seconds > 5) { // Expired.
                memory.remove(band);
            } else return false;
        }
        memory.put(band, val);
        return true;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new TeleportTransmitter(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Transmitter for the teleportation network.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"frequency name", "radius=x:y:z offset"};
        }
    }
}