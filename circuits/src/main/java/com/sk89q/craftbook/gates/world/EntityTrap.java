package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.SignUtil;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class EntityTrap extends AbstractIC {

    protected boolean risingEdge;

    public EntityTrap(Server server, Sign sign, boolean risingEdge) {

        super(server, sign);
        this.risingEdge = risingEdge;
    }

    @Override
    public String getTitle() {

        return "Entity Trap";
    }

    @Override
    public String getSignTitle() {

        return "ENTITY TRAP";
    }

    @Override
    public void trigger(ChipState chip) {

        if (risingEdge && chip.getInput(0) || (!risingEdge && !chip.getInput(0))) {
            chip.setOutput(0, hurt());
        }
    }

    /**
     * Returns true if the sign has water at the specified location.
     *
     * @return
     */
    protected boolean hurt() {

        Block b = SignUtil.getBackBlock(getSign().getBlock());

        int x = b.getX();
        int y = b.getY() + 1;
        int z = b.getZ();
        Block bl = getSign().getBlock().getWorld().getBlockAt(x, y, z);
        World w = getSign().getBlock().getWorld();
        for (LivingEntity p : w.getEntitiesByClass(LivingEntity.class)) {
            int ix = p.getLocation().getBlockX();
            int iy = p.getLocation().getBlockY();
            int iz = p.getLocation().getBlockZ();
            if (ix == x && iy == y && iz == z) {
                int damage = 2;
                try {
                    damage = Integer.parseInt(getSign().getLine(2));
                } catch (Exception e) {
                }
                p.damage(damage);
                return true;

            }
            if (ix == x && iy == y + 1 && iz == z) {
                int damage = 2;
                try {
                    damage = Integer.parseInt(getSign().getLine(2));
                } catch (Exception e) {
                }
                p.damage(damage);
                return true;

            }
        }
        return false;
    }

    public static class Factory extends AbstractICFactory implements
            RestrictedIC {

        protected boolean risingEdge;

        public Factory(Server server, boolean risingEdge) {

            super(server);
            this.risingEdge = risingEdge;
        }

        @Override
        public IC create(Sign sign) {

            return new EntityTrap(getServer(), sign, risingEdge);
        }
    }
}
