/*
 * CraftBook Copyright (C) 2010-2016 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2016 me4502 <http://www.me4502.com>
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
import com.sk89q.craftbook.sponge.util.BlockFilter;
import com.sk89q.craftbook.sponge.util.BlockUtil;
import com.sk89q.craftbook.sponge.util.SignUtil;
import com.sk89q.craftbook.sponge.util.locale.TranslationsManager;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TranslatableText;
import org.spongepowered.api.text.translation.ResourceBundleTranslation;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;

import java.util.List;

@Module(moduleName = "Bridge", onEnable="onInitialize", onDisable="onDisable")
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
    public boolean triggerMechanic(Location block, Sign sign, Humanoid human, Boolean forceState) {
        if (!"[Bridge End]".equals(SignUtil.getTextRaw(sign, 1))) {
            Direction back = SignUtil.getBack(block);

            Location baseBlock = block.getRelative(Direction.DOWN);

            if(!BlockUtil.doesStatePassFilters(allowedBlocks.getValue(), baseBlock.getBlock())) {
                if (human instanceof CommandSource) ((CommandSource) human).sendMessage(notAllowedMaterial);
                return true;
            }

            Location otherSide = BlockUtil.getNextMatchingSign(block, SignUtil.getBack(block), maximumLength.getValue(), this::isMechanicSign);
            if (otherSide == null) {
                if (human instanceof CommandSource) ((CommandSource) human).sendMessage(missingOtherEnd);
                return true;
            }
            Location otherBase = otherSide.getRelative(Direction.DOWN);

            if(!baseBlock.getBlock().equals(otherBase.getBlock())) {
                if (human instanceof CommandSource) ((CommandSource) human).sendMessage(Text.builder("Both ends must be the same material!").build());
                return true;
            }

            int leftBlocks, rightBlocks;

            Location left = baseBlock.getRelative(SignUtil.getLeft(block));
            Location right = baseBlock.getRelative(SignUtil.getRight(block));

            //Calculate left distance
            Location otherLeft = otherBase.getRelative(SignUtil.getLeft(block));

            leftBlocks = BlockUtil.getMinimumLength(left, otherLeft, baseBlock.getBlock(), SignUtil.getLeft(block), maximumWidth.getValue());

            //Calculate right distance
            Location otherRight = otherBase.getRelative(SignUtil.getRight(block));

            rightBlocks = BlockUtil.getMinimumLength(right, otherRight, baseBlock.getBlock(), SignUtil.getRight(block), maximumWidth.getValue());

            baseBlock = baseBlock.getRelative(back);

            BlockState type = block.getRelative(Direction.DOWN).getBlock();
            if (baseBlock.getBlock().equals(type) || (forceState != null && !forceState)) type = BlockTypes.AIR.getDefaultState();

            while (baseBlock.getBlockX() != otherSide.getBlockX() || baseBlock.getBlockZ() != otherSide.getBlockZ()) {
                baseBlock.setBlock(type, Cause.of(NamedCause.source(CraftBookPlugin.<CraftBookPlugin>inst().getContainer())));

                left = baseBlock.getRelative(SignUtil.getLeft(block));

                for(int i = 0; i < leftBlocks; i++) {
                    left.setBlock(type, Cause.of(NamedCause.source(CraftBookPlugin.<CraftBookPlugin>inst().getContainer())));
                    left = left.getRelative(SignUtil.getLeft(block));
                }

                right = baseBlock.getRelative(SignUtil.getRight(block));

                for(int i = 0; i < rightBlocks; i++) {
                    right.setBlock(type, Cause.of(NamedCause.source(CraftBookPlugin.<CraftBookPlugin>inst().getContainer())));
                    right = right.getRelative(SignUtil.getRight(block));
                }

                baseBlock = baseBlock.getRelative(back);
            }
        } else {
            if (human instanceof CommandSource) ((CommandSource) human).sendMessage(Text.builder("Bridge not activatable from here!").build());
            return false;
        }

        return true;
    }

    @Override
    public String[] getValidSigns() {
        return new String[]{"[Bridge]", "[Bridge End]"};
    }

    @Override
    public List<BlockFilter> getDefaultBlocks() {
        List<BlockFilter> states = Lists.newArrayList();
        states.add(new BlockFilter("PLANKS"));
        states.add(new BlockFilter("BOOKSHELF"));
        states.add(new BlockFilter("COBBLESTONE"));
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
