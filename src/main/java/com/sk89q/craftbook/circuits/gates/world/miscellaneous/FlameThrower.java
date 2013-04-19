package com.sk89q.craftbook.circuits.gates.world.miscellaneous;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractIC;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.ICVerificationException;
import com.sk89q.craftbook.circuits.ic.RestrictedIC;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.blocks.BlockID;

public class FlameThrower extends AbstractIC {

    int distance;
    int delay;

    public FlameThrower(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public void load() {

        try {
            distance = Integer.parseInt(getLine(2));
        } catch (Exception ignored) {
            distance = 10;
        }

        try {
            delay = Integer.parseInt(getLine(3));
        } catch (Exception ignored) {
            delay = 0;
        }
    }

    @Override
    public String getTitle() {

        return "Flame Thrower";
    }

    @Override
    public String getSignTitle() {

        return "FLAME THROWER";
    }

    @Override
    public void trigger(ChipState chip) {

        sendFlames(chip.getInput(0));
    }

    public void sendFlames(final boolean make) {

        final Block block = BukkitUtil.toSign(getSign()).getBlock();
        final BlockFace direction = SignUtil.getBack(block);

        if(delay <= 0) {

            Block fire = block.getRelative(direction, 2);
            for (int i = 0; i < distance; i++) {
                if (make) {
                    if (fire.getTypeId() == 0 || fire.getTypeId() == BlockID.LONG_GRASS) {
                        fire.setTypeId(BlockID.FIRE);
                    }
                } else if (fire.getTypeId() == BlockID.FIRE) {
                    fire.setTypeId(BlockID.AIR);
                }
                fire = fire.getRelative(direction);
            }
        } else {

            for (int i = 0; i < distance; i++) {

                final int fi = i;
                CraftBookPlugin.inst().getServer().getScheduler().runTaskLater(CraftBookPlugin.inst(), new Runnable() {

                    @Override
                    public void run () {

                        Block fire = block.getRelative(direction, 2+fi);
                        if (make) {
                            if (fire.getTypeId() == 0 || fire.getTypeId() == BlockID.LONG_GRASS) {
                                fire.setTypeId(BlockID.FIRE);
                            }
                        } else if (fire.getTypeId() == BlockID.FIRE) {
                            fire.setTypeId(BlockID.AIR);
                        }
                    }

                }, delay*fi);
            }
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new FlameThrower(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Makes a line of fire.";
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            try {
                int distance = Integer.parseInt(sign.getLine(2));
                if (distance > 20) throw new ICVerificationException("Distance too great!");

            } catch (Exception ignored) {
                throw new ICVerificationException("Invalid distance!");
            }
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {"distance", "delay"};
            return lines;
        }
    }
}