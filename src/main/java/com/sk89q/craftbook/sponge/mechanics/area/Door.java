package com.sk89q.craftbook.sponge.mechanics.area;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.sponge.util.SignUtil;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;

import java.util.Set;

@Module(moduleName = "Door", onEnable="onInitialize", onDisable="onDisable")
public class Door extends SimpleArea {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private ConfigValue<Integer> maximumLength = new ConfigValue<>("maximum-length", "The maximum length the door can be.", 16);
    private ConfigValue<Integer> maximumWidth = new ConfigValue<>("maximum-width", "The maximum width each side of the door can be. The overall max width is this*2 + 1.", 5);

    @Override
    public void onInitialize() throws CraftBookException {
        super.loadCommonConfig(config);

        maximumLength.load(config);
        maximumWidth.load(config);
    }

    @Override
    public void onDisable() {
        super.saveCommonConfig(config);

        maximumLength.save(config);
        maximumWidth.save(config);
    }

    @Override
    public boolean triggerMechanic(Location block, Sign sign, Humanoid human, Boolean forceState) {

        if (!SignUtil.getTextRaw(sign, 1).equals("[Door]")) {

            Direction back = SignUtil.getTextRaw(sign, 1).equals("[Door Up]") ? Direction.UP : Direction.DOWN;

            Location baseBlock = block.getRelative(back);

            Location otherSide = getOtherEnd(block, back, maximumLength.getValue());
            if (otherSide == null) {
                if (human instanceof CommandSource) ((CommandSource) human).sendMessage(Texts.builder("Missing other end!").build());
                return true;
            }

            Location otherBase = otherSide.getRelative(back.getOpposite());

            if(!baseBlock.getBlock().equals(otherBase.getBlock())) {
                if (human instanceof CommandSource) ((CommandSource) human).sendMessage(Texts.builder("Both ends must be the same material!").build());
                return true;
            }

            int leftBlocks = 0, rightBlocks = 0; //Default to 0. Single width bridge is the default.

            Location left = baseBlock.getRelative(SignUtil.getLeft(block));
            Location right = baseBlock.getRelative(SignUtil.getRight(block));

            //Calculate left distance
            Location otherLeft = otherBase.getRelative(SignUtil.getLeft(block));

            while(true) {
                if(leftBlocks >= maximumWidth.getValue()) break;
                if(left.getBlock().equals(baseBlock.getBlock()) && otherLeft.getBlock().equals(baseBlock.getBlock())) {
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
                if(rightBlocks >= maximumWidth.getValue()) break;
                if(right.getBlock().equals(baseBlock.getBlock()) && otherRight.getBlock().equals(baseBlock.getBlock())) {
                    rightBlocks ++;
                    right = right.getRelative(SignUtil.getRight(block));
                    otherRight = otherRight.getRelative(SignUtil.getRight(block));
                } else {
                    break;
                }
            }

            baseBlock = baseBlock.getRelative(back);

            BlockState type = block.getRelative(back).getBlock();
            if (baseBlock.getBlock().equals(type) || (forceState != null && !forceState)) type = BlockTypes.AIR.getDefaultState();

            while (baseBlock.getBlockY() != otherSide.getBlockY() + (back == Direction.UP ? -1 : 1)) {

                baseBlock.setBlock(type);

                left = baseBlock.getRelative(SignUtil.getLeft(block));

                for(int i = 0; i < leftBlocks; i++) {
                    left.setBlock(type);
                    left = left.getRelative(SignUtil.getLeft(block));
                }

                right = baseBlock.getRelative(SignUtil.getRight(block));

                for(int i = 0; i < rightBlocks; i++) {
                    right.setBlock(type);
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

    @Override
    public Set<BlockState> getDefaultBlocks() {
        Set<BlockState> states = Sets.newHashSet();
        states.add(BlockTypes.PLANKS.getDefaultState());
        states.add(BlockTypes.COBBLESTONE.getDefaultState());
        return states;
    }
}
