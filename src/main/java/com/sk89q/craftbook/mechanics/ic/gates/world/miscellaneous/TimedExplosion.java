package com.sk89q.craftbook.mechanics.ic.gates.world.miscellaneous;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractIC;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.RestrictedIC;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.RegexUtil;

public class TimedExplosion extends AbstractIC {

    private int ticks;
    private float yield;
    private boolean flamey;

    private Block center;

    public TimedExplosion(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public void load() {

        if(getLine(3).length() > 0 && !getLine(3).contains(":")) {
            getSign().setLine(2, getSign().getLine(2) + ":" + getSign().getLine(3));
            getSign().update(false);
        }
        try {
            ticks = Integer.parseInt(RegexUtil.COLON_PATTERN.split(getSign().getLine(2).replace("!", ""))[0]);
        } catch (Exception e) {
            ticks = -1;
        }

        try {
            yield = Float.parseFloat(RegexUtil.COLON_PATTERN.split(getSign().getLine(2).replace("!", ""))[1]);
        } catch (Exception e) {
            yield = -1;
        }

        try {
            flamey = getSign().getLine(2).endsWith("!");
        } catch (Exception ignored) {
        }

        center = ICUtil.parseBlockLocation(getSign(), 3);
    }

    @Override
    public String getTitle() {

        return "Timed Explosive";
    }

    @Override
    public String getSignTitle() {

        return "TIME BOMB";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            Location loc = center.getLocation();

            if(!loc.getChunk().isLoaded())
                return;

            while(loc.getBlock().getType().isSolid())
                loc = loc.add(0, 1, 0);
            TNTPrimed tnt = (TNTPrimed) loc.getWorld().spawnEntity(BlockUtil.getBlockCentre(loc.getBlock()),
                    EntityType.PRIMED_TNT);
            tnt.setIsIncendiary(flamey);
            if (ticks > 0) {
                tnt.setFuseTicks(ticks);
            }
            if (yield > 0) {
                tnt.setYield(yield);
            }
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new TimedExplosion(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Spawn tnt with custom fuse and yield.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"time in ticks:radius (ending with ! makes fire)", "x:y:z offset"};
        }
    }
}
