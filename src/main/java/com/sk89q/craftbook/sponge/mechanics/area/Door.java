package com.sk89q.craftbook.sponge.mechanics.area;

import org.spongepowered.api.block.BlockLoc;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.data.Sign;
import org.spongepowered.api.entity.living.Human;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.command.CommandSource;

import com.sk89q.craftbook.sponge.util.SignUtil;

public class Door extends SimpleArea {

    public BlockLoc getOtherEnd(BlockLoc block, Direction back) {

        for(int i = 0; i < 16; i++) {

            block = block.getRelative(back);

            if(SignUtil.isSign(block)) {
                Sign sign = block.getData(Sign.class).get();

                if(SignUtil.getTextRaw(sign, 1).equals("[Door Up]") || SignUtil.getTextRaw(sign, 1).equals("[Door Down]") || SignUtil.getTextRaw(sign, 1).equals("[Door]")) {

                    return block;
                }
            }
        }

        return null;
    }

    @Override
    public boolean triggerMechanic (BlockLoc block, Sign sign, Human human) {

        if(SignUtil.getTextRaw(sign, 1).equals("[Door Up]") || SignUtil.getTextRaw(sign, 1).equals("[Door Down]")) {

            Direction back = SignUtil.getTextRaw(sign, 1).equals("[Door Up]") ? Direction.UP : Direction.DOWN;

            BlockLoc baseBlock = block.getRelative(back);

            BlockLoc left = baseBlock.getRelative(SignUtil.getLeft(block));
            BlockLoc right = baseBlock.getRelative(SignUtil.getRight(block));

            BlockLoc otherSide = getOtherEnd(block, back);
            if(otherSide == null) {
                if(human instanceof CommandSource)
                    ((CommandSource) human).sendMessage("Missing other end!");
                return true;
            }

            baseBlock = baseBlock.getRelative(back);

            left = baseBlock.getRelative(SignUtil.getLeft(block));
            right = baseBlock.getRelative(SignUtil.getRight(block));

            BlockType type = BlockTypes.PLANKS;
            if(baseBlock.getType() == type)
                type = BlockTypes.AIR;

            while(baseBlock.getY() != otherSide.getY() + (back == Direction.UP ? -1 : 1)) {

                baseBlock.replaceWith(type);
                left.replaceWith(type);
                right.replaceWith(type);

                baseBlock = baseBlock.getRelative(back);

                left = baseBlock.getRelative(SignUtil.getLeft(block));
                right = baseBlock.getRelative(SignUtil.getRight(block));
            }
        } else return false;

        return true;
    }
}
