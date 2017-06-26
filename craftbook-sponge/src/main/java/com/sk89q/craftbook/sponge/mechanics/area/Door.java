/*
 * CraftBook Copyright (C) 2010-2017 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2017 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package com.sk89q.craftbook.sponge.mechanics.area;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.PermissionNode;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.blockbags.BlockBag;
import com.sk89q.craftbook.sponge.mechanics.blockbags.EmbeddedBlockBag;
import com.sk89q.craftbook.sponge.mechanics.blockbags.MultiBlockBag;
import com.sk89q.craftbook.sponge.util.data.mutable.EmbeddedBlockBagData;
import com.sk89q.craftbook.sponge.util.BlockFilter;
import com.sk89q.craftbook.sponge.util.BlockUtil;
import com.sk89q.craftbook.sponge.util.SignUtil;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;

@Module(id = "door", name = "Door", onEnable="onInitialize", onDisable="onDisable")
public class Door extends SimpleArea implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private ConfigValue<Integer> maximumLength = new ConfigValue<>("maximum-length", "The maximum length the door can be.", 16);
    private ConfigValue<Integer> maximumWidth = new ConfigValue<>("maximum-width", "The maximum width each side of the door can be. The overall max width is this*2 + 1.", 5);

    @Override
    public void onInitialize() throws CraftBookException {
        super.loadCommonConfig(config);
        super.registerCommonPermissions();

        maximumLength.load(config);
        maximumWidth.load(config);
    }

    @Override
    public boolean triggerMechanic(Location<World> block, Sign sign, Humanoid human, Boolean forceState) {
        if (!"[Door]".equals(SignUtil.getTextRaw(sign, 1))) {

            Direction back = "[Door Up]".equals(SignUtil.getTextRaw(sign, 1)) ? Direction.UP : Direction.DOWN;

            Location<World> baseBlock = block.getRelative(back);

            if(!BlockUtil.doesStatePassFilters(allowedBlocks.getValue(), baseBlock.getBlock())) {
                if (human instanceof CommandSource) ((CommandSource) human).sendMessage(Text.builder("Can't use this material for a door!").build());
                return true;
            }

            Location<World> otherSide = BlockUtil.getNextMatchingSign(block, back, maximumLength.getValue(), this::isMechanicSign);
            if (otherSide == null) {
                if (human instanceof CommandSource) ((CommandSource) human).sendMessage(missingOtherEnd);
                return true;
            }

            Location<World> otherBase = otherSide.getRelative(back.getOpposite());

            if(!baseBlock.getBlock().equals(otherBase.getBlock())) {
                if (human instanceof CommandSource) ((CommandSource) human).sendMessage(Text.builder("Both ends must be the same material!").build());
                return true;
            }

            int leftBlocks = 0, rightBlocks = 0; //Default to 0. Single width bridge is the default.

            Location<World> left = baseBlock.getRelative(SignUtil.getLeft(block));
            Location<World> right = baseBlock.getRelative(SignUtil.getRight(block));

            //Calculate left distance
            Location<World> otherLeft = otherBase.getRelative(SignUtil.getLeft(block));

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
            Location<World> otherRight = otherBase.getRelative(SignUtil.getRight(block));

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

            ItemStack blockBagItem = ItemStack.builder().fromBlockState(otherBase.getBlock()).quantity(1 + leftBlocks + rightBlocks).build();
            BlockBag blockBag = getBlockBag(sign.getLocation());

            while (baseBlock.getBlockY() != otherSide.getBlockY() + (back == Direction.UP ? -1 : 1)) {
                if (type.getType() == BlockTypes.AIR || blockBag.has(Lists.newArrayList(blockBagItem.copy()))) {
                    if (type.getType() == BlockTypes.AIR && baseBlock.getBlock().equals(otherBase.getBlock())) {
                        for (ItemStack leftover : blockBag.add(Lists.newArrayList(blockBagItem.copy()))) {
                            Item item = (Item) block.getExtent().createEntity(EntityTypes.ITEM, sign.getLocation().getPosition());
                            item.offer(Keys.REPRESENTED_ITEM, leftover.createSnapshot());
                            block.getExtent().spawnEntity(item, CraftBookPlugin.spongeInst().getCause().build());
                        }
                    } else if (type.getType() != BlockTypes.AIR && !baseBlock.getBlock().equals(otherBase.getBlock())) {
                        if (!blockBag.remove(Lists.newArrayList(blockBagItem.copy())).isEmpty()) {
                            continue;
                        }
                    }
                    baseBlock.setBlock(type, Cause.of(NamedCause.source(CraftBookPlugin.spongeInst().getContainer())));

                    left = baseBlock.getRelative(SignUtil.getLeft(block));

                    for (int i = 0; i < leftBlocks; i++) {
                        if (type.getType() == BlockTypes.AIR && left.getBlock().equals(otherBase.getBlock())) {
                            for (ItemStack leftover : blockBag.add(Lists.newArrayList(blockBagItem.copy()))) {
                                Item item = (Item) block.getExtent().createEntity(EntityTypes.ITEM, sign.getLocation().getPosition());
                                item.offer(Keys.REPRESENTED_ITEM, leftover.createSnapshot());
                                block.getExtent().spawnEntity(item, CraftBookPlugin.spongeInst().getCause().build());
                            }
                        } else if (type.getType() != BlockTypes.AIR && !left.getBlock().equals(otherBase.getBlock())) {
                            if (!blockBag.remove(Lists.newArrayList(blockBagItem.copy())).isEmpty()) {
                                continue;
                            }
                        }
                        left.setBlock(type, Cause.of(NamedCause.source(CraftBookPlugin.spongeInst().getContainer())));
                        left = left.getRelative(SignUtil.getLeft(block));
                    }

                    right = baseBlock.getRelative(SignUtil.getRight(block));

                    for (int i = 0; i < rightBlocks; i++) {
                        if (type.getType() == BlockTypes.AIR && right.getBlock().equals(otherBase.getBlock())) {
                            for (ItemStack leftover : blockBag.add(Lists.newArrayList(blockBagItem.copy()))) {
                                Item item = (Item) block.getExtent().createEntity(EntityTypes.ITEM, sign.getLocation().getPosition());
                                item.offer(Keys.REPRESENTED_ITEM, leftover.createSnapshot());
                                block.getExtent().spawnEntity(item, CraftBookPlugin.spongeInst().getCause().build());
                            }
                        } else if (type.getType() != BlockTypes.AIR && !right.getBlock().equals(otherBase.getBlock())) {
                            if (!blockBag.remove(Lists.newArrayList(blockBagItem.copy())).isEmpty()) {
                                continue;
                            }
                        }
                        right.setBlock(type, Cause.of(NamedCause.source(CraftBookPlugin.spongeInst().getContainer())));
                        right = right.getRelative(SignUtil.getRight(block));
                    }

                    baseBlock = baseBlock.getRelative(back);
                } else {
                    if (human instanceof Player) {
                        ((Player) human).sendMessage(Text.of("Out of blocks!"));
                    }
                    break;
                }
            }

            if (blockBag instanceof EmbeddedBlockBag) {
                sign.getLocation().offer(new EmbeddedBlockBagData((EmbeddedBlockBag) blockBag));
            }
        } else {
            if (human instanceof CommandSource) ((CommandSource) human).sendMessage(Text.builder("Door not activatable from here!").build());
            return false;
        }

        return true;
    }

    @Override
    public BlockBag getBlockBag(Location<World> location) {
        BlockBag mainBlockBag = super.getBlockBag(location);
        if (SignUtil.isSign(location)) {
            Direction back = "[Door Up]".equals(SignUtil.getTextRaw((Sign) location.getTileEntity().get(), 1)) ? Direction.UP : Direction.DOWN;
            Location<World> next = BlockUtil.getNextMatchingSign(location, back, maximumLength.getValue() + 2, this::isMechanicSign);
            if (next != null) {
                BlockBag nextBlockBag = super.getBlockBag(next);
                if (nextBlockBag != null) {
                    return new MultiBlockBag(mainBlockBag, nextBlockBag);
                }
            }
        }

        return mainBlockBag;
    }

    @Override
    public String[] getValidSigns() {
        return new String[]{"[Door Up]", "[Door Down]", "[Door]"};
    }

    @Override
    public List<BlockFilter> getDefaultBlocks() {
        List<BlockFilter> states = Lists.newArrayList();
        states.add(new BlockFilter("PLANKS"));
        states.add(new BlockFilter("COBBLESTONE"));
        return states;
    }

    @Override
    public String getPath() {
        return "mechanics/door";
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue<?>[]{
                allowedBlocks,
                allowRedstone,
                maximumLength,
                maximumWidth
        };
    }

    @Override
    public PermissionNode[] getPermissionNodes() {
        return new PermissionNode[]{
                createPermissions,
                usePermissions
        };
    }
}
