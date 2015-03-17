package com.sk89q.craftbook.sponge.mechanics.area;

import java.util.HashSet;
import java.util.Set;

import org.spongepowered.api.block.BlockLoc;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.data.Sign;
import org.spongepowered.api.entity.EntityInteractionTypes;
import org.spongepowered.api.entity.living.Human;
import org.spongepowered.api.event.entity.living.human.HumanInteractBlockEvent;
import org.spongepowered.api.event.entity.living.player.PlayerInteractBlockEvent;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.event.Subscribe;
import org.spongepowered.api.world.extent.Extent;

import com.sk89q.craftbook.sponge.util.SignUtil;

public class Gate extends SimpleArea {

    @Override
    @Subscribe
    public void onPlayerInteract(HumanInteractBlockEvent event) {

        if (event instanceof PlayerInteractBlockEvent && ((PlayerInteractBlockEvent) event).getInteractionType() != EntityInteractionTypes.USE) return;

        super.onPlayerInteract(event);

        if (event.getBlock().getType() == BlockTypes.FENCE) {

            int x = event.getBlock().getX();
            int y = event.getBlock().getY();
            int z = event.getBlock().getZ();

            for (int x1 = x - searchRadius; x1 <= x + searchRadius; x1++) {
                for (int y1 = y - searchRadius; y1 <= y + searchRadius * 2; y1++) {
                    for (int z1 = z - searchRadius; z1 <= z + searchRadius; z1++) {

                        if (SignUtil.isSign(event.getBlock().getExtent().getFullBlock(x1, y1, z1))) {

                            Sign sign = event.getBlock().getExtent().getFullBlock(x1, y1, z1).getData(Sign.class).get();

                            triggerMechanic(event.getBlock().getExtent().getFullBlock(x1, y1, z1), sign, event.getHuman(), null);
                        }
                    }
                }
            }
        }
    }

    private static int searchRadius = 5;

    public void findColumns(BlockLoc block, Set<GateColumn> columns) {

        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        for (int x1 = x - searchRadius; x1 <= x + searchRadius; x1++) {
            for (int y1 = y - searchRadius; y1 <= y + searchRadius * 2; y1++) {
                for (int z1 = z - searchRadius; z1 <= z + searchRadius; z1++) {

                    searchColumn(block.getExtent().getFullBlock(x1, y1, z1), columns);
                }
            }
        }
    }

    public void searchColumn(BlockLoc block, Set<GateColumn> columns) {

        int y = block.getY();

        if (block.getExtent().getBlock(block.getX(), y, block.getZ()).getType() == BlockTypes.FENCE) {

            GateColumn column = new GateColumn(block);

            columns.add(column);

            BlockLoc temp = column.topBlock;

            while (temp.getType() == BlockTypes.FENCE || temp.getType() == BlockTypes.AIR) {
                if (!columns.contains(new GateColumn(temp.getRelative(Direction.NORTH)))) searchColumn(temp.getRelative(Direction.NORTH), columns);
                if (!columns.contains(new GateColumn(temp.getRelative(Direction.SOUTH)))) searchColumn(temp.getRelative(Direction.SOUTH), columns);
                if (!columns.contains(new GateColumn(temp.getRelative(Direction.EAST)))) searchColumn(temp.getRelative(Direction.EAST), columns);
                if (!columns.contains(new GateColumn(temp.getRelative(Direction.WEST)))) searchColumn(temp.getRelative(Direction.WEST), columns);

                temp = temp.getRelative(Direction.DOWN);
            }
        }
    }

    public void toggleColumn(BlockLoc block, boolean on) {

        Direction dir = Direction.DOWN;

        block = block.getRelative(dir);

        if (on) {
            while (block.getType() == BlockTypes.AIR) {
                block.replaceWith(BlockTypes.FENCE);
                block = block.getRelative(dir);
            }
        } else {
            while (block.getType() == BlockTypes.FENCE) {
                block.replaceWith(BlockTypes.AIR);
                block = block.getRelative(dir);
            }
        }
    }

    @Override
    public boolean triggerMechanic(BlockLoc block, Sign sign, Human human, Boolean forceState) {

        if (SignUtil.getTextRaw(sign, 1).equals("[Gate]")) {

            Set<GateColumn> columns = new HashSet<GateColumn>();

            findColumns(block, columns);

            if (columns.size() > 0) {
                Boolean on = forceState;
                for (GateColumn vec : columns) {
                    BlockLoc col = vec.getBlock();
                    if (on == null) {
                        on = col.getRelative(Direction.DOWN).getType() != BlockTypes.FENCE;
                    }
                    toggleColumn(col, on.booleanValue());
                }
            } else {
                if (human instanceof CommandSource) ((CommandSource) human).sendMessage("Can't find a gate!");
            }
        } else return false;

        return true;
    }

    public class GateColumn {

        BlockLoc topBlock;

        public GateColumn(Extent extent, int x, int y, int z) {
            this(extent.getFullBlock(x, y, z));
        }

        public GateColumn(BlockLoc topBlock) {

            while (topBlock.getType() == BlockTypes.FENCE) {
                topBlock = topBlock.getRelative(Direction.UP);
            }

            topBlock = topBlock.getRelative(Direction.DOWN);

            this.topBlock = topBlock;
        }

        public BlockLoc getBlock() {
            return topBlock;
        }

        @Override
        public int hashCode() {
            return topBlock.getExtent().hashCode() + topBlock.getX() + topBlock.getZ();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof GateColumn) return ((GateColumn) o).topBlock.getX() == topBlock.getX() && ((GateColumn) o).topBlock.getZ() == topBlock.getZ();

            return false;
        }
    }
}
