/*
 * CraftBook Copyright (C) 2010-2018 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2018 me4502 <http://www.me4502.com>
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
import com.sk89q.craftbook.sponge.mechanics.blockbags.BlockBag;
import com.sk89q.craftbook.sponge.mechanics.blockbags.EmbeddedBlockBag;
import com.sk89q.craftbook.sponge.mechanics.blockbags.MultiBlockBag;
import com.sk89q.craftbook.sponge.util.SpongeBlockFilter;
import com.sk89q.craftbook.sponge.util.BlockUtil;
import com.sk89q.craftbook.sponge.util.SignUtil;
import com.sk89q.craftbook.sponge.util.data.mutable.EmbeddedBlockBagData;
import com.sk89q.craftbook.sponge.util.locale.TranslationsManager;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.TreeTypes;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TranslatableText;
import org.spongepowered.api.text.translation.ResourceBundleTranslation;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;

@Module(id = "bridge", name = "Bridge", onEnable="onInitialize", onDisable="onDisable")
public class Bridge extends SimpleArea implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private ConfigValue<Integer> maximumLength = new ConfigValue<>("maximum-length", "The maximum length the bridge can be.", 16);
    private ConfigValue<Integer> maximumWidth = new ConfigValue<>("maximum-width", "The maximum width each side of the bridge can be. The overall max width is this*2 + 1.", 5);

    private TranslatableText notAllowedMaterial = TranslatableText.of(new ResourceBundleTranslation("bridge.cant-use-material", TranslationsManager.getResourceBundleFunction()));

    @Override
    public void onInitialize() throws CraftBookException {
        super.loadCommonConfig(config);
        super.registerCommonPermissions();

        maximumLength.load(config);
        maximumWidth.load(config);
    }

    @Override
    public boolean triggerMechanic(Location<World> block, Sign sign, Humanoid human, Boolean forceState) {
        if (!"[Bridge End]".equals(SignUtil.getTextRaw(sign, 1))) {
            Direction back = SignUtil.getBack(block);

            Direction bridgeDirection = Direction.DOWN;

            Location<World> baseBlock = block.getRelative(bridgeDirection);
            if (block.getBlockType() == BlockTypes.WALL_SIGN) {
                baseBlock = block.getRelative(SignUtil.getBack(block));
            }

            if(!BlockUtil.doesStatePassFilters(allowedBlocks.getValue(), baseBlock.getBlock())) {
                if (block.getBlock() != BlockTypes.WALL_SIGN) {
                    bridgeDirection = Direction.UP;
                    baseBlock = block.getRelative(bridgeDirection);
                    if(!BlockUtil.doesStatePassFilters(allowedBlocks.getValue(), baseBlock.getBlock())) {
                        if (human instanceof CommandSource) ((CommandSource) human).sendMessage(notAllowedMaterial);
                        return true;
                    }
                } else {
                    if (human instanceof CommandSource) ((CommandSource) human).sendMessage(notAllowedMaterial);
                    return true;
                }
            }

            Location<World> otherSide = BlockUtil.getNextMatchingSign(block, SignUtil.getBack(block), maximumLength.getValue(), this::isMechanicSign);
            if (otherSide == null) {
                if (human instanceof CommandSource) ((CommandSource) human).sendMessage(missingOtherEnd);
                return true;
            }
            Location<World> otherBase = otherSide.getRelative(bridgeDirection);
            if (otherSide.getBlockType() == BlockTypes.WALL_SIGN) {
                otherBase = otherSide.getRelative(SignUtil.getBack(otherSide));
            }

            if(!baseBlock.getBlock().equals(otherBase.getBlock())) {
                if (block.getBlockType() != BlockTypes.WALL_SIGN && bridgeDirection == Direction.DOWN) {
                    bridgeDirection = Direction.UP;
                    otherBase = otherSide.getRelative(bridgeDirection);
                    baseBlock = block.getRelative(bridgeDirection);
                    if(!baseBlock.getBlock().equals(otherBase.getBlock())) {
                        if (human instanceof CommandSource)
                            ((CommandSource) human).sendMessage(Text.builder("Both ends must be the same material!").build());
                        return true;
                    }
                } else {
                    if (human instanceof CommandSource)
                        ((CommandSource) human).sendMessage(Text.builder("Both ends must be the same material!").build());
                    return true;
                }
            }

            int leftBlocks, rightBlocks;

            Location<World> left = baseBlock.getRelative(SignUtil.getLeft(block));
            Location<World> right = baseBlock.getRelative(SignUtil.getRight(block));

            //Calculate left distance
            Location<World> otherLeft = otherBase.getRelative(SignUtil.getLeft(block));

            leftBlocks = BlockUtil.getMinimumLength(left, otherLeft, baseBlock.getBlock(), SignUtil.getLeft(block), maximumWidth.getValue());

            //Calculate right distance
            Location<World> otherRight = otherBase.getRelative(SignUtil.getRight(block));

            rightBlocks = BlockUtil.getMinimumLength(right, otherRight, baseBlock.getBlock(), SignUtil.getRight(block), maximumWidth.getValue());

            baseBlock = baseBlock.getRelative(back);

            BlockState type = block.getRelative(bridgeDirection).getBlock();
            if (block.getBlockType() == BlockTypes.WALL_SIGN) {
                type = block.getRelative(SignUtil.getBack(block)).getBlock();
            }
            if (baseBlock.getBlock().equals(type) || (forceState != null && !forceState)) type = BlockTypes.AIR.getDefaultState();

            ItemStack blockBagItem = ItemStack.builder().fromBlockState(otherBase.getBlock()).quantity(1).build();
            BlockBag blockBag = getBlockBag(sign.getLocation());

            while (baseBlock.getBlockX() != otherBase.getBlockX() || baseBlock.getBlockZ() != otherBase.getBlockZ()) {
                if (type.getType() == BlockTypes.AIR || blockBag.has(Lists.newArrayList(blockBagItem.copy()))) {
                    if (type.getType() == BlockTypes.AIR && baseBlock.getBlock().equals(otherBase.getBlock())) {
                        for (ItemStack leftover : blockBag.add(Lists.newArrayList(blockBagItem.copy()))) {
                            Item item = (Item) block.getExtent().createEntity(EntityTypes.ITEM, sign.getLocation().getPosition());
                            item.offer(Keys.REPRESENTED_ITEM, leftover.createSnapshot());
                            block.getExtent().spawnEntity(item);
                        }
                    } else if (type.getType() != BlockTypes.AIR && !baseBlock.getBlock().equals(otherBase.getBlock())) {
                        if (!blockBag.remove(Lists.newArrayList(blockBagItem.copy())).isEmpty()) {
                            continue;
                        }
                    }
                    baseBlock.setBlock(type);

                    left = baseBlock.getRelative(SignUtil.getLeft(block));

                    for(int i = 0; i < leftBlocks; i++) {
                        if (type.getType() == BlockTypes.AIR && left.getBlock().equals(otherBase.getBlock())) {
                            for (ItemStack leftover : blockBag.add(Lists.newArrayList(blockBagItem.copy()))) {
                                Item item = (Item) block.getExtent().createEntity(EntityTypes.ITEM, sign.getLocation().getPosition());
                                item.offer(Keys.REPRESENTED_ITEM, leftover.createSnapshot());
                                block.getExtent().spawnEntity(item);
                            }
                        } else if (type.getType() != BlockTypes.AIR && !left.getBlock().equals(otherBase.getBlock())) {
                            if (!blockBag.remove(Lists.newArrayList(blockBagItem.copy())).isEmpty()) {
                                continue;
                            }
                        }
                        left.setBlock(type);
                        left = left.getRelative(SignUtil.getLeft(block));
                    }

                    right = baseBlock.getRelative(SignUtil.getRight(block));

                    for(int i = 0; i < rightBlocks; i++) {
                        if (type.getType() == BlockTypes.AIR && right.getBlock().equals(otherBase.getBlock())) {
                            for (ItemStack leftover : blockBag.add(Lists.newArrayList(blockBagItem.copy()))) {
                                Item item = (Item) block.getExtent().createEntity(EntityTypes.ITEM, sign.getLocation().getPosition());
                                item.offer(Keys.REPRESENTED_ITEM, leftover.createSnapshot());
                                block.getExtent().spawnEntity(item);
                            }
                        } else if (type.getType() != BlockTypes.AIR && !right.getBlock().equals(otherBase.getBlock())) {
                            if (!blockBag.remove(Lists.newArrayList(blockBagItem.copy())).isEmpty()) {
                                continue;
                            }
                        }
                        right.setBlock(type);
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
            if (human instanceof CommandSource) ((CommandSource) human).sendMessage(Text.builder("Bridge not activatable from here!").build());
            return false;
        }

        return true;
    }

    @Override
    public BlockBag getBlockBag(Location<World> location) {
        BlockBag mainBlockBag = super.getBlockBag(location);
        Location<World> next = BlockUtil.getNextMatchingSign(location, SignUtil.getBack(location), maximumLength.getValue() + 2, this::isMechanicSign);
        if (next != null) {
            BlockBag nextBlockBag = super.getBlockBag(next);
            if (nextBlockBag != null) {
                return new MultiBlockBag(mainBlockBag, nextBlockBag);
            }
        }

        return mainBlockBag;
    }

    @Override
    public String[] getValidSigns() {
        return new String[]{"[Bridge]", "[Bridge End]"};
    }

    @Override
    public List<SpongeBlockFilter> getDefaultBlocks() {
        List<SpongeBlockFilter> states = Lists.newArrayList();
        states.add(new SpongeBlockFilter(BlockTypes.PLANKS));
        states.add(new SpongeBlockFilter(BlockTypes.STONEBRICK));
        states.add(new SpongeBlockFilter(BlockTypes.COBBLESTONE));
        return states;
    }

    @Override
    public String getPath() {
        return "mechanics/bridge";
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
