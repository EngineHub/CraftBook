package com.sk89q.craftbook.gates.world.miscellaneous;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.ICVerificationException;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.blocks.BlockID;

public class FlameThrower extends AbstractIC {

    int distance = 5;

    public FlameThrower(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public void load() {

        try {
            distance = Integer.parseInt(getSign().getLine(2));
        }
        catch(Exception ignored){}
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

    public void sendFlames(boolean make) {

        Block block = BukkitUtil.toSign(getSign()).getBlock();
        BlockFace direction = SignUtil.getBack(block);
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
    }

    public static class Factory extends AbstractICFactory implements
    RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new FlameThrower(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Makes a line of fire.";
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            try {
                int distance = Integer.parseInt(sign.getLine(2));
                if (distance > 20)
                    throw new ICVerificationException("Distance too great!");

            } catch (Exception ignored) {
            }
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "distance",
                    null
            };
            return lines;
        }
    }
}