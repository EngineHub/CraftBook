package com.sk89q.craftbook.mechanics.ic.gates.world.miscellaneous;

import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.SmallFireball;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.mechanics.ic.AbstractIC;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.RestrictedIC;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SignUtil;

/**
 * @author Me4502
 */
public class FireShooter extends AbstractIC {

    private double speed;
    private double spread;
    private double vert;

    public FireShooter(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public void load() {

        try {
            String[] velocity = RegexUtil.COLON_PATTERN.split(getSign().getLine(2).trim());
            speed = Double.parseDouble(velocity[0]);
            spread = Double.parseDouble(velocity[1]);
            vert = Double.parseDouble(getSign().getLine(3).trim());
        } catch (Exception e) {
            speed = 1.6f;
            spread = 12;
            vert = 0.2f;
            getSign().setLine(2, speed + ":" + spread);
            getSign().setLine(3, String.valueOf(vert));
            getSign().update(false);
        }

        if (speed > 10.0) {
            speed = 10F;
        } else if (speed < 0.1) {
            speed = 0.1F;
        }
        if (spread > 5000) {
            spread = 5000;
        } else if (spread < 0) {
            spread = 0;
        }
        if (vert > 100) {
            vert = 100;
        } else if (vert < -100) {
            vert = -100;
        }
    }

    @Override
    public String getTitle() {

        return "Fire Shooter";
    }

    @Override
    public String getSignTitle() {

        return "FIRE SHOOTER";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            shootFire(1);
        }
    }

    public void shootFire(int n) {

        Block signBlock = CraftBookBukkitUtil.toSign(getSign()).getBlock();
        BlockFace face = SignUtil.getBack(signBlock);
        Block targetDir = signBlock.getRelative(face).getRelative(face);

        double x = targetDir.getX() - signBlock.getX();
        double z = targetDir.getZ() - signBlock.getZ();
        Location shootLoc = new Location(
                CraftBookBukkitUtil.toSign(getSign()).getWorld(), targetDir.getX() + 0.5,targetDir.getY() + 0.5,targetDir.getZ() + 0.5);

        if(!shootLoc.getChunk().isLoaded())
            return;

        for (short i = 0; i < n; i++) {

            double f2 = Math.sqrt(x * x + vert * vert + z * z);

            double nx = x/f2;
            double ny = vert/f2;
            double nz = z/f2;
            nx += CraftBookPlugin.inst().getRandom().nextGaussian() * 0.007499999832361937D * spread;
            ny += CraftBookPlugin.inst().getRandom().nextGaussian() * 0.007499999832361937D * spread;
            nz += CraftBookPlugin.inst().getRandom().nextGaussian() * 0.007499999832361937D * spread;
            nx *= speed;
            ny *= speed;
            nz *= speed;
            float f3 = (float) Math.sqrt(nx * nx + nz * nz);

            SmallFireball f = CraftBookBukkitUtil.toSign(getSign()).getWorld().spawn(shootLoc, SmallFireball.class);
            f.setVelocity(new Vector(nx,ny,nz));
            f.getLocation().setYaw((float) (Math.atan2(nx, nz) * 180.0D / 3.1415927410125732D));
            f.getLocation().setPitch((float) (Math.atan2(ny, f3) * 180.0D / 3.1415927410125732D));
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new FireShooter(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Shoots a fireball.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"speed:spread", "vertical gain"};
        }
    }
}