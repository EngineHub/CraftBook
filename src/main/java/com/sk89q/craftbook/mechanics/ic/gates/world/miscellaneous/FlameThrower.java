package com.sk89q.craftbook.mechanics.ic.gates.world.miscellaneous;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.mechanics.ic.AbstractIC;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.ConfigurableIC;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.ICVerificationException;
import com.sk89q.craftbook.mechanics.ic.RestrictedIC;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.util.yaml.YAMLProcessor;

public class FlameThrower extends AbstractIC {

    private int distance;
    private int delay;

    public FlameThrower(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public void load() {

        try {
            distance = Math.min(((Factory)getFactory()).maxRange, Integer.parseInt(getLine(2)));
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

        final Block block = CraftBookBukkitUtil.toSign(getSign()).getBlock();
        final BlockFace direction = SignUtil.getBack(block);

        if(delay <= 0) {

            Block fire = block.getRelative(direction, 2);
            for (int i = 0; i < distance; i++) {
                if (make) {
                    if (fire.getType() == Material.AIR || fire.getType() == Material.GRASS) {
                        fire.setType(Material.FIRE);
                    }
                } else if (fire.getType() == Material.FIRE) {
                    fire.setType(Material.AIR);
                }
                fire = fire.getRelative(direction);
            }
        } else {

            for (int i = 0; i < distance; i++) {

                final int fi = i;
                CraftBookPlugin.inst().getServer().getScheduler().runTaskLater(CraftBookPlugin.inst(), () -> {

                    Block fire = block.getRelative(direction, 2+fi);
                    if (make) {
                        if (fire.getType() == Material.AIR || fire.getType() == Material.GRASS) {
                            fire.setType(Material.FIRE);
                        }
                    } else if (fire.getType() == Material.FIRE) {
                        fire.setType(Material.AIR);
                    }
                }, delay*fi);
            }
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC, ConfigurableIC {

        public int maxRange;

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

        @Override public String[] getLongDescription() {
            return new String[] {
                    "The '''MC1252''' sets a certain length of blocks in fron of the IC block on fire (putting fire Block on top of them)."
            };
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            try {
                int distance = Integer.parseInt(sign.getLine(2));
                if (distance > maxRange) throw new ICVerificationException("Distance too great!");

            } catch (Exception ignored) {
                throw new ICVerificationException("Invalid distance!");
            }
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"distance", "delay"};
        }

        @Override
        public void addConfiguration(YAMLProcessor config, String path) {

            config.setComment(path + "max-fire-range", "The maximum range the Flamethrower IC can be set to.");
            maxRange = config.getInt(path + "max-fire-range", 20);
        }
    }
}