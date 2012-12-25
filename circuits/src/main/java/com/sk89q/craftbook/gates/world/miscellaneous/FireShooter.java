package com.sk89q.craftbook.gates.world.miscellaneous;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.SmallFireball;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BaseBukkitPlugin;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.ICUtil;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.util.SignUtil;

/**
 * @author Me4502
 */
public class FireShooter extends AbstractIC {

    private float speed = 0.6F;
    private float spread = 4;
    private float vert = 0;

    public FireShooter (Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public void load () {

        String[] velocity = ICUtil.COLON_PATTERN.split(getSign().getLine(2).trim(), 2);
        try {
            speed = Float.parseFloat(velocity[0]);
            spread = Float.parseFloat(velocity[1]);
        } catch (Exception ignored) {
        }
        try {
            vert = Float.parseFloat(getSign().getLine(3).trim());
        } catch (Exception ignored) {
        }

        if (speed > 2.0) {
            speed = 2F;
        } else if (speed < 0.2) {
            speed = 0.2F;
        }

        if (spread > 50) {
            spread = 50;
        } else if (spread < 0) {
            spread = 0;
        }

        if (vert > 1) {
            vert = 1;
        } else if (vert < -1) {
            vert = -1;
        }
    }

    @Override
    public String getTitle () {

        return "Fire Shooter";
    }

    @Override
    public String getSignTitle () {

        return "FIRE SHOOTER";
    }

    @Override
    public void trigger (ChipState chip) {

        if (chip.getInput(0)) {
            shootFire(1);
        }
    }

    public void shootFire (int n) {

        Block signBlock = BukkitUtil.toSign(getSign()).getBlock();
        BlockFace face = SignUtil.getBack(signBlock);
        Block targetDir = signBlock.getRelative(face).getRelative(face);

        float x = targetDir.getX() - signBlock.getX();
        float z = targetDir.getZ() - signBlock.getZ();
        Vector velocity = new Vector(x, vert, z);
        Location shootLoc = new Location(BukkitUtil.toSign(getSign()).getWorld(), targetDir.getX() + 0.5, targetDir.getY() + 0.5,
                targetDir.getZ() + 0.5);

        if (n != 1) {
            for (short i = 0; i < n; i++) {
                velocity = new Vector(x + randomFloat(spread), vert + randomFloat(spread), z + randomFloat(spread));
                SmallFireball f = BukkitUtil.toSign(getSign()).getWorld().spawn(shootLoc, SmallFireball.class);
                f.setVelocity(velocity);
            }
        } else {
            SmallFireball f = BukkitUtil.toSign(getSign()).getWorld().spawn(shootLoc, SmallFireball.class);
            f.setVelocity(velocity);
        }
    }

    private static float randomFloat (float width) {
        return (BaseBukkitPlugin.random.nextFloat() - 0.5f) * width;
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory (Server server) {

            super(server);
        }

        @Override
        public IC create (ChangedSign sign) {

            return new FireShooter(getServer(), sign, this);
        }

        @Override
        public String getDescription () {

            return "Shoots a fireball.";
        }

        @Override
        public String[] getLineHelp () {

            String[] lines = new String[] { "speed:spread", "vertical gain" };
            return lines;
        }
    }
}
