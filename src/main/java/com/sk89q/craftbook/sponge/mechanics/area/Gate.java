package com.sk89q.craftbook.sponge.mechanics.area;

import java.util.HashSet;
import java.util.Set;

import org.spongepowered.api.block.BlockLoc;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.data.Sign;
import org.spongepowered.api.entity.EntityInteractionType;
import org.spongepowered.api.entity.living.Human;
import org.spongepowered.api.event.entity.living.human.HumanInteractBlockEvent;
import org.spongepowered.api.event.entity.living.player.PlayerInteractBlockEvent;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.event.Subscribe;

import com.flowpowered.math.vector.Vector3i;
import com.sk89q.craftbook.core.util.CachePolicy;
import com.sk89q.craftbook.sponge.util.SignUtil;

public class Gate extends SimpleArea {

    @Override
    public String getName () {
        return "Gate";
    }

    @Subscribe
    public void onPlayerInteract(HumanInteractBlockEvent event) {

        if(event instanceof PlayerInteractBlockEvent && ((PlayerInteractBlockEvent) event).getInteractionType() != EntityInteractionType.RIGHT_CLICK) return;

        if(SignUtil.isSign(event.getBlock())) {

            Sign sign = event.getBlock().getData(Sign.class).get();

            if(sign.getLine(1).toLegacy().equals("[Gate]")) {

                activateGate(event.getHuman(), event.getBlock());
            }
        } else if(event.getBlock().getType() == BlockTypes.FENCE) {

            int x = event.getBlock().getX();
            int y = event.getBlock().getY();
            int z = event.getBlock().getZ();

            for (int x1 = x - searchRadius; x1 <= x + searchRadius; x1++) {
                for (int y1 = y - searchRadius; y1 <= y + searchRadius*2; y1++) {
                    for (int z1 = z - searchRadius; z1 <= z + searchRadius; z1++) {

                        if(SignUtil.isSign(event.getBlock().getExtent().getBlock(x1, y1, z1))) {

                            Sign sign = event.getBlock().getExtent().getBlock(x1, y1, z1).getData(Sign.class).get();

                            if(sign.getLine(1).toLegacy().equals("[Gate]")) {

                                activateGate(event.getHuman(), event.getBlock().getExtent().getBlock(x1, y1, z1));
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    public void activateGate(Human human, BlockLoc block) {

        Set<Vector3i> columns = new HashSet<Vector3i>();

        findColumns(block, columns);

        if(columns.size() > 0) {
            Boolean on = null;
            for(Vector3i vec : columns) {
                BlockLoc col = block.getExtent().getBlock(vec.toDouble());
                if(on == null) {
                    on = col.getRelative(Direction.DOWN).getType() != BlockTypes.FENCE;
                }
                toggleColumn(col, on.booleanValue());
            }
        } else {
            if(human instanceof CommandSource)
                ((CommandSource) human).sendMessage("Can't find a gate!");
        }
    }

    private static int searchRadius = 5;

    public void findColumns(BlockLoc block, Set<Vector3i> columns) {

        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        for (int x1 = x - searchRadius; x1 <= x + searchRadius; x1++) {
            for (int y1 = y - searchRadius; y1 <= y + searchRadius*2; y1++) {
                for (int z1 = z - searchRadius; z1 <= z + searchRadius; z1++) {

                    int y2 = y1;

                    if(block.getExtent().getBlock(x1, y2, z1).getType() == BlockTypes.FENCE) {
                        while(block.getExtent().getBlock(x1, y2, z1).getType() == BlockTypes.FENCE) {

                            y2++;
                        }

                        columns.add(new Vector3i(x1,y2-1,z1));
                    }
                }
            }
        }

    }

    public void toggleColumn(BlockLoc block, boolean on) {

        Direction dir = Direction.DOWN;

        block = block.getRelative(dir);

        if(on) {
            while(block.getType() == BlockTypes.AIR) {
                block.replaceWith(BlockTypes.FENCE);
                block = block.getRelative(dir);
            }
        } else {
            while(block.getType() == BlockTypes.FENCE) {
                block.replaceWith(BlockTypes.AIR);
                block = block.getRelative(dir);
            }
        }
    }

    @Override
    public CachePolicy getCachePolicy () {
        return null;
    }
}
