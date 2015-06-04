package com.sk89q.craftbook.sponge.mechanics.area;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.entity.living.Human;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.world.Location;

import com.sk89q.craftbook.sponge.util.SignUtil;

public class Door extends SimpleArea {

    public Location getOtherEnd(Location block, Direction back) {

        for (int i = 0; i < 16; i++) {

            block = block.getRelative(back);

            if (SignUtil.isSign(block)) {
                Sign sign = (Sign) block.getTileEntity().get();

                if (isMechanicSign(sign)) {
                    return block;
                }
            }
        }

        return null;
    }

    @Override
    public boolean triggerMechanic(Location block, Sign sign, Human human, Boolean forceState) {

        if (!SignUtil.getTextRaw(sign, 1).equals("[Door]")) {

            Direction back = SignUtil.getTextRaw(sign, 1).equals("[Door Up]") ? Direction.UP : Direction.DOWN;

            Location baseBlock = block.getRelative(back);

            Location otherSide = getOtherEnd(block, back);
            if (otherSide == null) {
                if (human instanceof CommandSource) ((CommandSource) human).sendMessage(Texts.builder("Missing other end!").build());
                return true;
            }

            Location otherBase = otherSide.getRelative(back.getOpposite());

            if(!baseBlock.getState().equals(otherBase.getState())) {
                if (human instanceof CommandSource) ((CommandSource) human).sendMessage(Texts.builder("Both ends must be the same material!").build());
                return true;
            }

            int leftBlocks = 0, rightBlocks = 0; //Default to 0. Single width bridge is the default.

            Location left = baseBlock.getRelative(SignUtil.getLeft(block));
            Location right = baseBlock.getRelative(SignUtil.getRight(block));

            //Calculate left distance
            Location otherLeft = otherBase.getRelative(SignUtil.getLeft(block));

            while(true) {
                if(left.getState().equals(baseBlock.getState()) && otherLeft.getState().equals(baseBlock.getState())) {
                    leftBlocks ++;
                    left = left.getRelative(SignUtil.getLeft(block));
                    otherLeft = otherLeft.getRelative(SignUtil.getLeft(block));
                } else {
                    break;
                }
            }

            //Calculate right distance
            Location otherRight = otherBase.getRelative(SignUtil.getRight(block));

            while(true) {
                if(right.getState().equals(baseBlock.getState()) && otherRight.getState().equals(baseBlock.getState())) {
                    rightBlocks ++;
                    right = right.getRelative(SignUtil.getRight(block));
                    otherRight = otherRight.getRelative(SignUtil.getRight(block));
                } else {
                    break;
                }
            }

            baseBlock = baseBlock.getRelative(back);

            BlockState type = block.getRelative(back).getState();
            if (baseBlock.getState().equals(type) && (forceState == null || forceState == false)) type = BlockTypes.AIR.getDefaultState();

            while (baseBlock.getY() != otherSide.getY() + (back == Direction.UP ? -1 : 1)) {

                baseBlock.replaceWith(type);

                left = baseBlock.getRelative(SignUtil.getLeft(block));

                for(int i = 0; i < leftBlocks; i++) {
                    left.replaceWith(type);
                    left = left.getRelative(SignUtil.getLeft(block));
                }

                right = baseBlock.getRelative(SignUtil.getRight(block));

                for(int i = 0; i < rightBlocks; i++) {
                    right.replaceWith(type);
                    right = right.getRelative(SignUtil.getRight(block));
                }

                baseBlock = baseBlock.getRelative(back);
            }
        } else {
            if (human instanceof CommandSource) ((CommandSource) human).sendMessage(Texts.builder("Door not activatable from here!").build());
            return false;
        }

        return true;
    }

    @Override
    public boolean isMechanicSign(Sign sign) {
        return SignUtil.getTextRaw(sign, 1).equalsIgnoreCase("[Door Up]") || SignUtil.getTextRaw(sign, 1).equalsIgnoreCase("[Door Down]") || SignUtil.getTextRaw(sign, 1).equalsIgnoreCase("[Door]");
    }

    @Override
    public String[] getValidSigns() {
        return new String[]{"[Door Up]", "[Door Down]", "[Door]"};
    }
}
