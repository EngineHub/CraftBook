package com.sk89q.craftbook.circuits.gates.world.miscellaneous;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.*;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.SignUtil;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;

public class TimedExplosion extends AbstractIC {

    private int ticks;
    private float yield;
    private boolean flamey;

    Block signBlock;
    Block infront;

    public TimedExplosion(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public void load() {

        try {
            ticks = Integer.parseInt(getSign().getLine(2));
        } catch (Exception e) {
            ticks = -1;
        }

        try {
            yield = Float.parseFloat(getSign().getLine(3).replace("!", ""));
        } catch (Exception e) {
            yield = -1;
        }

        try {
            flamey = getSign().getLine(3).endsWith("!");
        } catch (Exception ignored) {
        }

        try {
            signBlock = BukkitUtil.toSign(getSign()).getBlock();
            infront = signBlock.getRelative(SignUtil.getBack(signBlock).getOppositeFace());
        } catch (Exception ignored) {
        }
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
            TNTPrimed tnt = (TNTPrimed) signBlock.getWorld().spawnEntity(BlockUtil.getBlockCentre(infront),
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
        public String getDescription() {

            return "Spawn tnt with custom fuse and yield.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {"time in ticks", "explosion radius (ending with ! makes fire)"};
            return lines;
        }
    }
}
