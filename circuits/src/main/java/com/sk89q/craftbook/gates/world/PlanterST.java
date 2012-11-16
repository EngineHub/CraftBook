package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.Vector;
import org.bukkit.Server;
import org.bukkit.World;

public class PlanterST extends Planter implements SelfTriggeredIC {

    public PlanterST(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public boolean isActive() {

        return true;
    }

    @Override
    public String getTitle() {

        return "Self-Triggered Planter";
    }

    @Override
    public String getSignTitle() {

        return "PLANTER ST";
    }


    @Override
    public void think(ChipState state) {

        World world = BukkitUtil.toSign(getSign()).getWorld();
        Vector onBlock = BukkitUtil.toVector(SignUtil.getBackBlock(
                BukkitUtil.toSign(getSign()).getBlock()).getLocation());
        Vector target;
        int[] info = null;
        int yOffset;

        if (!getSign().getLine(2).isEmpty()) {
            String[] lineParts = ICUtil.COLON_PATTERN.split(getSign().getLine(2));
            info = new int[] {Integer.parseInt(lineParts[0]),
                    Integer.parseInt(lineParts[1])};
        }

        if (info == null || !plantableItem(info[0])) return;

        try {
            yOffset = Integer.parseInt(getSign().getLine(3));
        } catch (NumberFormatException e) {
            return;
        }
        if (yOffset < 1) return;

        target = onBlock.add(0, yOffset, 0);

        if (world.getBlockTypeIdAt(target.getBlockX(), target.getBlockY(),
                target.getBlockZ()) == 0
                && itemPlantableOnBlock(
                        info[0],
                        world.getBlockTypeIdAt(target.getBlockX(),
                                target.getBlockY() - 1, target.getBlockZ()))) {

            BlockPlanter sp = new BlockPlanter(world, target, info[0],
                    info[1]);
            sp.run();
        }
    }

    public static class Factory extends Planter.Factory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new PlanterST(getServer(), sign, this);
        }
    }
}
