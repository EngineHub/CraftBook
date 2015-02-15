package com.sk89q.craftbook.sponge.mechanics.area;

import org.spongepowered.api.block.BlockLoc;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.data.Sign;
import org.spongepowered.api.entity.EntityInteractionType;
import org.spongepowered.api.event.entity.living.human.HumanInteractBlockEvent;
import org.spongepowered.api.event.entity.living.player.PlayerInteractBlockEvent;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.event.Subscribe;

import com.sk89q.craftbook.sponge.util.SignUtil;

public class Bridge extends SimpleArea {

    @Subscribe
    public void onPlayerInteract(HumanInteractBlockEvent event) {

        if(event instanceof PlayerInteractBlockEvent && ((PlayerInteractBlockEvent) event).getInteractionType() != EntityInteractionType.RIGHT_CLICK) return;

        if(SignUtil.isSign(event.getBlock())) {

            Sign sign = event.getBlock().getData(Sign.class).get();

            if(SignUtil.getTextRaw(sign, 1).equals("[Bridge]")) {

                Direction back = SignUtil.getBack(event.getBlock());

                BlockLoc baseBlock = event.getBlock().getRelative(Direction.DOWN);

                BlockLoc left = baseBlock.getRelative(SignUtil.getLeft(event.getBlock()));
                BlockLoc right = baseBlock.getRelative(SignUtil.getRight(event.getBlock()));

                BlockLoc otherSide = getOtherEnd(event.getBlock());
                if(otherSide == null) {
                    if(event.getHuman() instanceof CommandSource)
                        ((CommandSource) event.getHuman()).sendMessage("Missing other end!");
                    return;
                }

                baseBlock = baseBlock.getRelative(back);

                left = baseBlock.getRelative(SignUtil.getLeft(event.getBlock()));
                right = baseBlock.getRelative(SignUtil.getRight(event.getBlock()));

                BlockType type = BlockTypes.PLANKS;
                if(baseBlock.getType() == type)
                    type = BlockTypes.AIR;

                while(baseBlock.getX() != otherSide.getX() || baseBlock.getZ() != otherSide.getZ()) {

                    baseBlock.replaceWith(type);
                    left.replaceWith(type);
                    right.replaceWith(type);

                    baseBlock = baseBlock.getRelative(back);

                    left = baseBlock.getRelative(SignUtil.getLeft(event.getBlock()));
                    right = baseBlock.getRelative(SignUtil.getRight(event.getBlock()));
                }
            }
        }
    }

    public BlockLoc getOtherEnd(BlockLoc block) {

        Direction back = SignUtil.getBack(block);

        for(int i = 0; i < 16; i++) {

            block = block.getRelative(back);

            if(SignUtil.isSign(block)) {
                Sign sign = block.getData(Sign.class).get();

                if(SignUtil.getTextRaw(sign, 1).equals("[Bridge]")) {

                    return block;
                }
            }
        }

        return null;
    }
}